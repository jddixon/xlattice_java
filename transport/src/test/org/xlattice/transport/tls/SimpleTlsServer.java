/* SimpleTlsServer.java */
package org.xlattice.transport.tls;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManager;

import org.xlattice.crypto.tls.TlsConst;
import org.xlattice.util.NonBlockingLog;

/**
 * A simple TLS echo server using JSSE and no XLattice abstractions.
 * The server loops until a message beginning with "quit" is 
 * received.
 */

public class SimpleTlsServer extends Thread implements TlsConst {

    // INSTANCE VARIABLES ///////////////////////////////////////////
    // SERVER-RELATED /////////////////////////////////////
    private volatile boolean        running;

    private final InetAddress       serverAddr;
    private final int               serverPort;
    private       SSLServerSocket   serverSocket;

    private Thread myThread;

    // CRYPTO STUFF ///////////////////////////////////////
    public  final int               level;
    public  final String            myKeyStoreName;
    public  final char[]            myPassphrase;
    public  final String            clientKeyStoreName;
    public  final char[]            clientPassphrase;

    public  final SecureRandom rng;
    public        KeyStore     myKeyStore;
    public        KeyStore     clientKeyStore;
    
    // LOGGING ////////////////////////////////////////////
    protected static NonBlockingLog debugLog 
                            = NonBlockingLog.getInstance("simple.log");
    protected void DEBUG_MSG(String s) {
        debugLog.message("SimpleTlsServer: " + s);
    }
    // CONSTRUCTORS /////////////////////////////////////////////////
    /**
     * Simple iterative echo server using TLS.
     * 
     * @param sA     server host address
     * @param sP     server port 
     * @param level  TLS level as defined in transport/tls/TLS
     * @param serverKeyStoreName  name of server private KeyStore file
     * @param serverPasswd        its passphrase
     * @param clientKeyStoreName  name of client public KeyStore file
     * @param clientPasswd        its passphrase
     */
    public SimpleTlsServer (InetAddress sA, int sP, int level, 
                String serverKeyStoreName, String serverPasswd,
                String clientKeyStoreName, String clientPasswd ) {
        serverAddr = sA;
        serverPort = sP;
        this.level = level;

        if (level == ANONYMOUS_TLS) {
            this.clientKeyStoreName = null;
            clientPassphrase        = null;
            myKeyStoreName          = null;
            myPassphrase            = null;
        } else {
            if ( (level & SERVER_MASK) == 0) {
                this.clientKeyStoreName = null;
                clientPassphrase        = null;
            } else {
                this.clientKeyStoreName = clientKeyStoreName;
                clientPassphrase        = clientPasswd.toCharArray();
            } 
            myKeyStoreName = serverKeyStoreName;
            myPassphrase   = serverPasswd.toCharArray();
        }  
        rng = new SecureRandom();
        int junk = rng.nextInt();

        start();
    }

    public void run() {
        myThread = Thread.currentThread();
        KeyManagerFactory kmf;
        TrustManager[] tmArray;
        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("TLS");
            if (level == ANONYMOUS_TLS) {
                sslContext.init(null, null, rng);
            } else {
                int clientFlags = level & CLIENT_MASK;
                if ( clientFlags == 0) {
                    clientKeyStore = null;
                    tmArray = null;
                } else { 
                    clientKeyStore = KeyStore.getInstance("JKS");
                    clientKeyStore.load( 
                            new FileInputStream(clientKeyStoreName), 
                                                    clientPassphrase);
                    if (clientFlags == ANY_CLIENT_CERT) {
                        tmArray = new TrustManager[] {TRUST_ANYONE};
                    } else if (clientFlags == CA_SIGNED_CLIENT_CERT) {
                        tmArray = null;     // default is OK
                    } else {
                        // XXX NEED TO CATCH AND PERHAPS RETHROW
                        throw new IllegalStateException(/* NAME */);
                    }
                }
                // server end /////////////////////////////
                myKeyStore     = KeyStore.getInstance("JKS");
                myKeyStore.load(
                    new FileInputStream(myKeyStoreName), myPassphrase);
                kmf = KeyManagerFactory.getInstance("SunX509");
                kmf.init (myKeyStore, myPassphrase);
                sslContext.init( kmf.getKeyManagers(), tmArray, rng);
            }
        } catch (GeneralSecurityException gse) {
            gse.printStackTrace();
            return;
        } catch (IOException ioe) { 
            ioe.printStackTrace();
            return;
        }
        SSLServerSocketFactory ssf = sslContext.getServerSocketFactory();
        try {
//          serverSocket = (SSLServerSocket)ssf.createServerSocket(
//                              serverPort, 128, serverAddr);
            serverSocket = (SSLServerSocket)ssf.createServerSocket();
            serverSocket.setReuseAddress(true); 
            serverSocket.bind (new InetSocketAddress(serverAddr, serverPort),
                    128 );                  // backlog
        } catch (IOException ioe) {
            StackTraceElement[] ste = ioe.getStackTrace();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < ste.length; i++)
                sb.append(ste[i].toString())
                  .append('\n');
            DEBUG_MSG("createServerSocket exception: " + ioe.toString() 
                    + "\n" + sb.toString());
            try { serverSocket.close(); } catch (Throwable t) {}
            // DOESN'T WORK - just returning results in hangs XXX
            return;
        } 
        if (level == ANONYMOUS_TLS)
            serverSocket.setEnabledCipherSuites(TLS_ANONYMOUS_CIPHERS);
        try {
            handleConnections();
        } catch (InterruptedException ie) {
            /* STUB ? */
            System.out.println("caught InterruptedException");
        } 
    }
    /**
     * Echo connections until a message beginning with "quit" 
     * is received.
     */
    private void handleConnections()        throws InterruptedException {
        byte[] buffer = new byte[512];
        running = true;
        Socket socket = null;
        byte[] endMarker = "quit".getBytes();
        while (running) {
            try {
                socket = serverSocket.accept();
                int count = socket.getInputStream()
                                .read(buffer, 0, buffer.length);
                socket.getOutputStream()
                                .write(buffer, 0, count);
                running = false;
                for (int i = 0; i < endMarker.length; i++) 
                    if (buffer[i] != endMarker[i]) {
                        running = true;
                        break;
                    }
                // we rely on client to close connection    
            } catch (InterruptedIOException ie) {
                running = false;
                interrupt();
            } catch (IOException ioe) {
                /* ignore */
            } 
        }
    }
    /** we override Thread.interrupt() */
    public void interrupt () {
        super.interrupt();
        try {
            serverSocket.close();
        } catch (IOException ioe) {}

    }
    public void close() {
        running = false;
        if (myThread != null && myThread.isAlive()) {
            Thread thread = Thread.currentThread();
            if (thread != myThread) {
                myThread.interrupt();
                try {
                    myThread.join(1000);    // don't wait forever
                } catch (InterruptedException ie) { /* ignore */ }
            }
        }
        if (!serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException ioe) {}
        }
    }
    public Thread getThread() {
        return myThread;
    }
    public boolean isRunning() {
        return running;
    }
    // ACCESS METHODS ///////////////////////////////////////////////
    public InetAddress getServerAddr() {
        return serverAddr;
    }
    public int getServerPort() {
        return serverPort;
    }
    public SSLServerSocket getServerSocket() {
        return serverSocket;
    }
}
