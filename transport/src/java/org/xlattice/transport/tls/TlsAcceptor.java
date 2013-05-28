/* TlsAcceptor.java */
package org.xlattice.transport.tls;

import java.io.IOException;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.xlattice.Acceptor;
import org.xlattice.Connection;
import org.xlattice.EndPoint;
import org.xlattice.Transport;

import org.xlattice.crypto.tls.TlsConst;

import org.xlattice.transport.tcp.Tcp;
import org.xlattice.transport.tcp.TcpAcceptor;

import org.xlattice.util.NonBlockingLog;

/**
 *
 * @author Jim Dixon
 */
public class TlsAcceptor extends TcpAcceptor implements TlsConst {
    
    private final Tls         myTls;
   
    private final int         level;

    private final KeyStore    myKeyStore;
    private final char[]      myPassphrase;

    // XXX NEED TO PROVIDE SETTERS
    private       KeyStore    clientKeyStore;
    private       char[]      clientPassphrase;

    private final SecureRandom      rng;
    private final SSLSocketFactory  ssf;
    
    // LOGGING ////////////////////////////////////////////
    protected static NonBlockingLog debugLog 
                            = NonBlockingLog.getInstance("simple.log");
    protected void DEBUG_MSG(String s) {
        debugLog.message("TlsAcceptor: " + s);
    }
    // CONSTRUCTORS /////////////////////////////////////////////////
    protected TlsAcceptor (EndPoint nearEnd)    throws IOException {
        // XXX Can't do this without violating Acceptor interface:
        //            throws GeneralSecurityException, IOException {
        super(new EndPoint(((Tls)nearEnd.getTransport()).getTcp(),
                    nearEnd.getAddress()));

        myTls = (Tls)nearEnd.getTransport();
        
        TlsAddress nearAddr = (TlsAddress) nearEnd.getAddress();
        level               = nearAddr.level;
        if (level == ANONYMOUS_TLS) {
            clientKeyStore   = null;
            clientPassphrase = null;
            myKeyStore       = null;
            myPassphrase     = null;
        } else {
            myKeyStore       = nearAddr.getKeyStore();
            myPassphrase     = nearAddr.passphrase;
        }
        rng                 = new SecureRandom();
        rng.nextInt();      // initialization can take some time
       
        // SSLContext /////////////////////////////////////
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
                kmf = KeyManagerFactory.getInstance("SunX509");
                kmf.init (myKeyStore, myPassphrase);
                sslContext.init( kmf.getKeyManagers(), tmArray, rng);
            }
        } catch (GeneralSecurityException gse) {
            throw new IOException( gse.toString() );
        }
        ssf = sslContext.getSocketFactory();
    }
    // INTERFACE Acceptor ///////////////////////////////////////////
    public Connection accept ()                 throws IOException {
        Socket tcpSocket  = /*super.*/socket().accept();
        String clientHost = tcpSocket.getInetAddress().toString();
        int clientPort   = tcpSocket.getPort();
        SSLSocket socket = (SSLSocket)ssf.createSocket(tcpSocket, 
                                clientHost, clientPort, true);  
        // XXX Doesn't setting this here create the possibility of a 
        // race condition? -- no, because the handshake occurs before
        // the first I/O, or on invoking startHandshake()
        socket.setUseClientMode(false);
        if (level == ANONYMOUS_TLS)
            socket.setEnabledCipherSuites(TLS_ANONYMOUS_CIPHERS);
        // XXX NOT THE RIGHT TEST
        if (level == ANONYMOUS_TLS)
            socket.setNeedClientAuth(false);
        return new TlsConnection(myTls, socket);
    }
    /**
     * Do any TLS-specific actions before or after calling the
     * superclass's close().
     */
    public void close()                         throws IOException {
        // PRE-actions
        super.close();
        // POST-actions
    }
    // OTHER METHODS ////////////////////////////////////////////////
    public String toString() {
        return new StringBuffer("TlsAcceptor: ")
            .append(getEndPoint().getAddress().toString())
            .toString();
    }
}
