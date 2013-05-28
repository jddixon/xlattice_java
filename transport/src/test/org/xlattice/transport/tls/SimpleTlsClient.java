/* SimpleTlsClient.java */
package org.xlattice.transport.tls;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.xlattice.crypto.tls.TlsConst;
import org.xlattice.transport.tls.Tls;
import org.xlattice.util.NonBlockingLog;
import org.xlattice.util.Queue;

/**
 * This is a one-shot echo client with outbound and inbound queues.
 * For each message, it opens a connection, sends the message, waits
 * for it to be echoed back, and then closes the connection.  The
 * client keeps on doing this until it is closed, so long as there is
 * something in its input queue.  Responses from the server are also
 * queued up.
 *
 * @author Jim Dixon
 */

public class SimpleTlsClient extends Thread implements TlsConst{

    private volatile boolean        running;

    private       InetAddress       clientAddr;
    private       int               clientPort;
    private       SSLSocket         clientSocket;
    private final InetAddress       serverAddr;
    private final int               serverPort;

    private Thread myThread;

    // CRYPTO STUFF ///////////////////////////////////////
    public  final int               level;
    public  final String            myKeyStoreName;
    public  final char[]            myPassphrase;
    public  final String            serverKeyStoreName;
    public  final char[]            serverPassphrase;

    public  final SecureRandom rng;
    public        KeyStore     myKeyStore;
    public        KeyStore     serverKeyStore;

    // LOGGING ////////////////////////////////////////////
    protected static NonBlockingLog debugLog 
                            = NonBlockingLog.getInstance("simple.log");
    protected void DEBUG_MSG(String s) {
        debugLog.message("SimpleTlsClient: " + s);
    }
    // CONSTRUCTORS /////////////////////////////////////////////////
    public SimpleTlsClient (InetAddress cA,
                InetAddress sA, int sP, int level,
                String serverKeyStoreName, String serverPasswd,
                String clientKeyStoreName, String clientPasswd ) {
        clientAddr = cA;
        clientPort = 0;     // meaning "please assign an ephemeral port"
        serverAddr = sA;
        serverPort = sP;
        this.level = level;

        if (level == ANONYMOUS_TLS) {
            myKeyStoreName = null;
            myPassphrase   = null;
            this.serverKeyStoreName = null;
            serverPassphrase        = null;
        } else {
            if ( (level & SERVER_MASK) == 0) {
                myKeyStoreName = null;
                myPassphrase   = null;
            } else {
                myKeyStoreName = clientKeyStoreName;
                myPassphrase   = clientPasswd.toCharArray();
            }
            this.serverKeyStoreName = serverKeyStoreName;
            serverPassphrase        = serverPasswd.toCharArray();
        }
        rng = new SecureRandom();
        int junk = rng.nextInt();

        start();
    }
    // ACCESS METHODS ///////////////////////////////////////////////
    public InetAddress getClientAddr() {
        return clientAddr;
    }
    public int getClientPort() {
        return clientPort;
    }
    public InetAddress getServerAddr() {
        return serverAddr;
    }
    public int getServerPort() {
        return serverPort;
    }
    // INTERFACE Runnable ///////////////////////////////////////////
    public void run() {
        myThread = Thread.currentThread();

        // main loop //////////////////////////////////////
        running  = true;
        byte[] message;
        while (running) {
            message = null;
            synchronized (messages) {
                while (running && messages.size() == 0) {
                    try {
                        messages.wait();
                    } catch (InterruptedException ie) {
                        // running = false;
                    }
                }
                // in synch block restarted by notify()
                if (running && messages.size() > 0)
                    message = (byte[]) messages.dequeue();
            }
            if (message != null && running)
                try {
                    sendIt(message, level);        // blocks
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    running = false;
                }
        }
    }
    // MESSAGE QUEUE ////////////////////////////////////////////////
    private Queue messages = new Queue();

    public void enqueueMessage (byte [] msg) {
        synchronized (messages) {
            messages.enqueue(msg);
            messages.notify();
        }
    }
    private Queue responses = new Queue();

    public byte[] dequeueResponse() {
        synchronized (responses) {
            return (byte[]) responses.dequeue();
        }
    }
    public int sizeResponses() {
        synchronized (responses) {
            return responses.size();
        }
    }
    private void sendIt (byte[] message, int level)
                                                throws IOException {
        // set up TLS /////////////////////////////////////
        KeyManagerFactory kmf;
        TrustManager[] tmArray;
        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("TLS");
            if (level == ANONYMOUS_TLS) {
                sslContext.init(null, null, rng);
            } else {
                serverKeyStore = KeyStore.getInstance("JKS");
                serverKeyStore.load(
                    new FileInputStream(serverKeyStoreName), serverPassphrase);
                
                // configure km: what we offer the server
                if ( (level & CLIENT_MASK) == 0) {
                    // server expects nothing from us
                    myKeyStore = null;
                    kmf = null;
                } else {
                    myKeyStore     = KeyStore.getInstance("JKS");
                    myKeyStore.load(
                        new FileInputStream(myKeyStoreName), myPassphrase);
                    kmf = KeyManagerFactory.getInstance("SunX509");
                    kmf.init (myKeyStore, myPassphrase);
                }
                
                // configure tm: what we expect from the server
                int serverFlags = level & SERVER_MASK;
                if (serverFlags == 0 || serverFlags == ANY_SERVER_CERT) {
                    tmArray = new TrustManager[] {TRUST_ANYONE};
                } else if (serverFlags == CA_SIGNED_SERVER_CERT) {
                    tmArray = null;      // default is OK
                } else {
                    String msg = "unknown/unsupported server flags: " 
                        + serverFlags;
                    DEBUG_MSG(msg);
                    throw new IOException(msg);
                }
                if (kmf == null) 
                    sslContext.init(null, tmArray, rng);
                else
                    sslContext.init( kmf.getKeyManagers(), tmArray, rng);
            }
        } catch (GeneralSecurityException gse) {
            gse.printStackTrace();
            return;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return;
        } catch (IllegalStateException ise) {
            ise.printStackTrace();
            return;
        } // FOO
        SSLSocketFactory sf = sslContext.getSocketFactory();
        clientSocket = null;
        try {
            clientSocket = (SSLSocket)sf.createSocket(
                                serverAddr, serverPort);
            clientSocket.setSoTimeout(100); // ms
            clientSocket.setUseClientMode(true);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return;
        }
        if (level == ANONYMOUS_TLS)
            clientSocket.setEnabledCipherSuites(TLS_ANONYMOUS_CIPHERS);
        clientSocket.getOutputStream().write(message);
        byte[] response = new byte[2 * message.length];
        try {
            int count = clientSocket.getInputStream().read(response);
            // just to be neat ...
            byte[] copy = new byte[count];
            System.arraycopy (response, 0, copy, 0, count);
            synchronized (responses) {
                responses.enqueue(copy);
            }
        } catch (SocketTimeoutException ste) {
            /* give up */
            DEBUG_MSG("timed out waiting for reply");
        } finally {
            clientSocket.close();
        }
    }
    // OTHER METHODS ////////////////////////////////////////////////
    public void close() {
        running = false;
        synchronized (messages) {
            messages.notifyAll();
        }
        if (Thread.currentThread() != myThread) {
            try {
                myThread.join();
            } catch (InterruptedException ie) { /* oh java ... */ }
        }
    }
    public Thread getThread() {
        return myThread;
    }
    public boolean isRunning() {
        return running;
    }
}
