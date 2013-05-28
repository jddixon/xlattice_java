/* EchoServer.java */
package org.xlattice.transport.tls;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import org.xlattice.Connection;
import org.xlattice.EndPoint;
import org.xlattice.crypto.tls.TlsConst;
import org.xlattice.transport.IPAddress;
import org.xlattice.util.NonBlockingLog;

/**
 * A simple TLS echo server based on the XLattice TlsAcceptor.
 * The server loops until a message beginning with "quit" is 
 * received.
 * 
 * This is a functional replacement for SimpleTlsServer.  It 
 * tests TlsAcceptor.
 * 
 * XXX This and SimpleTlsServer could subclass the same class.
 * 
 * @see SimpleTlsServer
 */

public class EchoServer extends Thread implements TlsConst {

    // INSTANCE VARIABLES ///////////////////////////////////////////
    // SERVER-RELATED /////////////////////////////////////
    private volatile boolean        running;

    private final InetAddress       serverAddr;
    private final int               serverPort;
    private       EndPoint          nearEnd;
    private       TlsAcceptor       acc;

    private Thread myThread;

    // CRYPTO STUFF ///////////////////////////////////////
    public  final int               level;
    public  final String            myKeyStoreName;
    public  final char[]            myPassphrase;
    public  final String            clientKeyStoreName;
    public  final char[]            clientPassphrase;

    public        KeyStore     myKeyStore;
    public        KeyStore     clientKeyStore;
    
    // LOGGING ////////////////////////////////////////////
    protected static NonBlockingLog debugLog 
                            = NonBlockingLog.getInstance("simple.log");
    protected void DEBUG_MSG(String s) {
        debugLog.message("EchoServer: " + s);
    }
    // CONSTRUCTORS /////////////////////////////////////////////////
    /**
     * Simple iterative echo server using XLattice TLS transport.
     * 
     * @param sA     server host address
     * @param sP     server port 
     * @param level  TLS level as defined in transport/tls/TLS
     * @param serverKeyStoreName  name of server private KeyStore file
     * @param serverPasswd        its passphrase
     * @param clientKeyStoreName  name of client public KeyStore file
     * @param clientPasswd        its passphrase
     */
    public EchoServer (InetAddress sA, int sP, int level, 
                String serverKeyStoreName, String serverPasswd,
                // XXX TlsAcceptor currently cannot handle
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
        // DEBUG
        DEBUG_MSG("starting acceptor thread on port " + serverPort);
        // END
        start();
    }

    public void run() {
        myThread = Thread.currentThread();

        try {
            if (level != ANONYMOUS_TLS) {
                myKeyStore     = KeyStore.getInstance("JKS");
                myKeyStore.load(
                    new FileInputStream(myKeyStoreName), myPassphrase);
            } else {
                myKeyStore = null;
            }
            nearEnd = new EndPoint (new Tls(), 
                                new TlsAddress(
                                    new IPAddress(serverAddr, serverPort),
                                    level,
                                    myKeyStore, myPassphrase, true));
            // DEBUG
            DEBUG_MSG("creating acceptor on " + serverAddr + ':' + serverPort);
            // END
            acc = new TlsAcceptor (nearEnd);
            // DEBUG
            DEBUG_MSG("created acceptor on port " + serverPort);
            // END
        } catch (GeneralSecurityException gse) {
            DEBUG_MSG("fatal: " + gse.toString());
            gse.printStackTrace();
            close();
            return;                 // will cause hang
        } catch (IOException ioe) {
            DEBUG_MSG("fatal: " + ioe.toString());
            ioe.printStackTrace();
            close();
            return;                 // will cause hang
        }
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
            Connection cnx = null;
            try {
                cnx = acc.accept();
                int count = cnx.getInputStream()
                                .read(buffer, 0, buffer.length);
                cnx.getOutputStream()
                                .write(buffer, 0, count);
                running = false;
                for (int i = 0; i < endMarker.length; i++) 
                    if (buffer[i] != endMarker[i]) {
                        running = true;
                        break;
                    }
                if (running == false) {
                    // a bit of a hack: delays closing the socket
                    Thread.currentThread().sleep(50);
                }
                // we rely on client to close connection    
            } catch (InterruptedIOException ie) {
                DEBUG_MSG(ie.toString());
                running = false;
                interrupt();
            } catch (IOException ioe) {
                DEBUG_MSG (ioe.toString());
                if (cnx != null)
                    try { cnx.close(); } catch (IOException ioe2) {} 
            } 
        }
    }
    /** we override Thread.interrupt() */
    public void interrupt () {
        super.interrupt();
        try {
            acc.close();
        } catch (IOException ioe) {}

    }
    public void close() {
        running = false;
        if (myThread != null && myThread.isAlive()) {
            Thread thread = Thread.currentThread();
            if (thread != myThread) {
                myThread.interrupt();
                try {
                    myThread.join(2000);    // wait no more than (ms)
                } catch (InterruptedException ie) { /* ignore */ }
            }
        }
        if (!acc.isClosed()) {
            try {
                acc.close();
            } catch (IOException ioe) {
            }
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
    public ServerSocket getServerSocket() {
        if (acc == null)
            return null;
        else
            return acc.socket();
    }
}
