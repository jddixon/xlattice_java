/* TlsConnector.java */
package org.xlattice.transport.tls;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.xlattice.Connection;
import org.xlattice.EndPoint;
import org.xlattice.Transport;
import org.xlattice.crypto.tls.TlsConst;
import org.xlattice.transport.tcp.TcpConnection;
import org.xlattice.transport.tcp.TcpConnector;
import org.xlattice.util.NonBlockingLog;

/**
 * XXX In this implementation, there are auth levels associated
 * with both ends.  They may be inconsistent.  We 'solve' this
 * by ignoring the level associated with the near end.
 */
public class TlsConnector extends TcpConnector implements TlsConst {

    private final Transport         myTls;
    private final String            serverName;
    private final int               serverPort;
         
    // CRYPTO STUFF ///////////////////////////////////////
    protected final int             level;

    protected final SecureRandom    rng;
    protected       KeyStore        myKeyStore;
    protected final KeyStore        serverKeyStore;
   
    // XXX These are likely to be dropped
    protected       char[]          myPassphrase;
    protected final char[]          serverPassphrase;
    
    // LOGGING ////////////////////////////////////////////
    protected static NonBlockingLog debugLog 
                            = NonBlockingLog.getInstance("simple.log");
    protected void DEBUG_MSG(String s) {
        debugLog.message("TlsConnector: " + s);
    }
    // CONSTRUCTORS /////////////////////////////////////////////////
    protected TlsConnector (EndPoint farEnd) 
                                            throws IOException {
        super (farEnd);

        myTls            = (Tls)farEnd.getTransport();
        TlsAddress addr  = (TlsAddress)farEnd.getAddress();
        level            = addr.level;
        serverName       = addr.getHost().getHostAddress();
        serverPort       = addr.getPort();

        if (level == ANONYMOUS_TLS) {
            serverKeyStore   = null;
            serverPassphrase = null;
        } else {
            serverKeyStore   = addr.getKeyStore();
            serverPassphrase = addr.passphrase;
        }
        rng = new SecureRandom();
        rng.nextInt();
    }
    // INTERFACE Connector //////////////////////////////////////////
    /**
     * XXX BUT blocking is always true.
     */
    public Connection connect(EndPoint nearEnd, boolean blocking)
                                                throws IOException {
        // this checks the parameters
        Socket tcpSocket = ((TcpConnection)super.connect(nearEnd, blocking))
                                    .socket();

        KeyManagerFactory kmf;
        TrustManager[] tmArray;
        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("TLS");
            if (level == ANONYMOUS_TLS) {
                sslContext.init(null, null, rng);
            } else {
                // configure km: what we offer the server
                if ( (level & CLIENT_MASK) == 0) {
                    // server expects nothing from us
                    myKeyStore = null;
                    kmf = null;
                } else {
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
            throw new IOException ( gse.toString() );
        }
        SSLSocketFactory sf = sslContext.getSocketFactory();
        SSLSocket clientSocket = (SSLSocket)sf.createSocket(
                            tcpSocket, serverName, serverPort, true);
        if (level == ANONYMOUS_TLS)
            clientSocket.setEnabledCipherSuites(TLS_ANONYMOUS_CIPHERS);
        clientSocket.setUseClientMode(true);

        return new TlsConnection (myTls, clientSocket);
    }
}
