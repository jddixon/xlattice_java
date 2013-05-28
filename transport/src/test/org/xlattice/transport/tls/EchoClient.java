/* EchoClient.java */
package org.xlattice.transport.tls;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.net.ssl.SSLSocket;

import org.xlattice.EndPoint;
import org.xlattice.Transport;
import org.xlattice.crypto.tls.TlsConst;
import org.xlattice.transport.IPAddress;
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
 * This has been derived from SimpleTlsClient.  Both could and 
 * should subclass the same superclass.
 *
 * @author Jim Dixon
 */

public class EchoClient extends Thread implements TlsConst {

    private volatile boolean        running;

    private       InetAddress       clientAddr;
    private       int               clientPort;
    private final TlsConnector      ctr;
    private final InetAddress       serverAddr;
    private final int               serverPort;
    private final EndPoint          nearEnd;
    private final EndPoint          farEnd;
    private final Transport         myTls;

    private Thread myThread;

    // CRYPTO STUFF ///////////////////////////////////////
    public  final int               level;
    public  final String            myKeyStoreName;
    public  final char[]            myPassphrase;
    public  final String            serverKeyStoreName;
    public  final char[]            serverPassphrase;

    public        KeyStore     myKeyStore;
    public        KeyStore     serverKeyStore;

    // LOGGING ////////////////////////////////////////////
    protected static NonBlockingLog debugLog 
                            = NonBlockingLog.getInstance("simple.log");
    protected void DEBUG_MSG(String s) {
        debugLog.message("EchoClient: " + s);
    }
    // CONSTRUCTORS /////////////////////////////////////////////////
    public EchoClient (InetAddress cA,
                InetAddress sA, int sP, int level,
                String serverKeyStoreName, String serverPasswd,
                String clientKeyStoreName, String clientPasswd ) 
                            throws GeneralSecurityException, IOException {
        clientAddr = cA;
        clientPort = 0;     // meaning "please assign an ephemeral port"
        serverAddr = sA;
        serverPort = sP;
        this.level = level;

        if (level == ANONYMOUS_TLS) {
            myKeyStoreName = null;
            myKeyStore     = null;
            myPassphrase   = null;
            this.serverKeyStoreName = null;
            serverPassphrase        = null;
        } else {
            if ( (level & SERVER_MASK) == 0) {
                myKeyStoreName = null;
                myKeyStore     = null;
                myPassphrase   = null;
            } else {
                myKeyStoreName = clientKeyStoreName;
                myPassphrase   = clientPasswd.toCharArray();
                myKeyStore     = KeyStore.getInstance("JKS");
                myKeyStore.load(
                    new FileInputStream(myKeyStoreName), myPassphrase);
            }
            this.serverKeyStoreName = serverKeyStoreName;
            serverPassphrase        = serverPasswd.toCharArray();
            serverKeyStore = KeyStore.getInstance("JKS");
            serverKeyStore.load(
                new FileInputStream(serverKeyStoreName), serverPassphrase);
        }

        myTls  = new Tls();
        nearEnd = new EndPoint ( myTls, 
                    new TlsAddress( 
                        new IPAddress(clientAddr, clientPort), level, 
                            myKeyStore, myPassphrase, false));
        farEnd = new EndPoint ( myTls, 
                    new TlsAddress( 
                        new IPAddress(serverAddr, serverPort), level, 
                            serverKeyStore, serverPassphrase, false));
        ctr = new TlsConnector(farEnd);
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
            messages.notifyAll();   // was just notify()
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
        TlsConnection cnx = (TlsConnection)ctr.connect(nearEnd, true);
        Socket socket = cnx.socket();
        // XXX messages are taking over 1000ms
        socket.setSoTimeout(3000);
        cnx.getOutputStream().write(message);
        byte[] response = new byte[2 * message.length];
        try {
            int count = cnx.getInputStream().read(response);
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
            cnx.close();
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
