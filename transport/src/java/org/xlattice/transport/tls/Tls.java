/* Tls.java */
package org.xlattice.transport.tls;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.xlattice.Address;
import org.xlattice.Acceptor;
import org.xlattice.Connection;
import org.xlattice.Connector;
import org.xlattice.EndPoint;
import org.xlattice.Transport;

import org.xlattice.crypto.tls.TlsConst;

import org.xlattice.transport.ClientServer;
import org.xlattice.transport.IPAddress;
import org.xlattice.transport.tcp.*;

/**
 * @author Jim Dixon
 */

public class Tls                                implements ClientServer {

//  // this trusts anything at all
//  public static final TrustManager TRUST_ANYONE = new X509TrustManager() {
//      public void checkClientTrusted(
//              X509Certificate[] chain, String authType) 
//                                      throws CertificateException {}
//      public void checkServerTrusted(
//              X509Certificate[] chain, String authType) 
//                                      throws CertificateException {}
//      public X509Certificate[] getAcceptedIssuers() {
//          return new X509Certificate[0];
//      }
//  }; 
//  /** the only anonymous cipher supported if using TLS */
//  public  static final String[] TLS_ANONYMOUS_CIPHERS
//                  = new String[] {"TLS_DH_anon_WITH_AES_128_CBC_SHA"};
//  
//  /** neither side uses public key */
//  public static final int ANONYMOUS_TLS               = 0x0000;
 
//  /** low byte contains client-related flags */
//  public static final int SERVER_MASK                 = 0x00ff;
//  /** accept any cert for the server */
//  public static final int ANY_SERVER_CERT             = 0x0001;
//  /** accept any cert signed by a known CA */
//  public static final int CA_SIGNED_SERVER_CERT       = 0x0002;
//  /** accept any previously learned cert */
//  public static final int KNOWN_SERVER_CERT           = 0x0004;
//  /** if this cert is accepted, add to list of known server certs */
//  public static final int LEARN_SERVER_CERT           = 0x0008;
//  
//  /** high byte contains server-related flags */
//  public static final int CLIENT_MASK                 = 0xff00;
//  /** accept any cert for the client */
//  public static final int ANY_CLIENT_CERT             = 0x0100;
//  /** accept any cert signed by a known CA */
//  public static final int CA_SIGNED_CLIENT_CERT       = 0x0200;
//  /** accept any previously learned cert */
//  public static final int KNOWN_CLIENT_CERT           = 0x0400;
//  /** if this cert is accepted, add to list of known client certs */
//  public static final int LEARN_CLIENT_CERT           = 0x0800;
    
    // INSTANCE VARIABLES ///////////////////////////////////////////
    private   final Tcp          myTcp;
    /** assumed to be thread-safe */
    protected final SecureRandom rng;
    
    // CONSTRUCTORS /////////////////////////////////////////////////
    /**
     * Create a Tls provider.  
     *
     */
    public Tls (Tcp tcp) {
        if (tcp == null)
            tcp = new Tcp();
        myTcp = tcp;
        rng = new SecureRandom();
        int junk = rng.nextInt();       // initializes
    }
    public Tls () {
        this ( new Tcp() );
    }
    // INTERFACE Transport ///////////////////////////////////////////
    /**
     * The address passed is a private TLS serverAddress, which includes
     * the server private key materials, directly or indirectly.
     */
    public Acceptor getAcceptor (Address nearAddr, boolean blocking) 
                                                throws IOException {
        if (blocking) {
            return new TlsAcceptor (new EndPoint(this, nearAddr));
        } else {
            //return new SchedulableTlsAcceptor (
            //        new EndPoint(this, (IPAddress)nearAddr));
            throw new UnsupportedOperationException();
        }
    }
    /**
     * Both of the addresses are public TLS addresses, one a client
     * address and the other a server address.
     *
     * @param nearAddr client's address
     * @param farAddr  the server address
     * @param blocking whether the connection is blocking
     */
    public Connection getConnection (Address nearAddr, Address farAddr,
                            boolean blocking)   throws IOException {
        if (blocking) {
            // XXX CAN THIS EVER BE USEFUL?
            // return new TlsConnection(this, 
            //                (TlsAddress)nearAddr, (TlsAddress)farAddr);
            /* STUB */
            return null;
        } else {
            // XXX NEED A CONSTRUCTOR THAT HANDLES ALREADY-CONNECTED
            // XXX SOCKET CHANNELS, for Connectors -- this is
            // XXX return new SchedulableTcpConnection(ServerSocket sChan)
            // XXX However, we don't have access to sChan, unless we allow
            // XXX an Object to be passed
            throw new UnsupportedOperationException();
        }
    }
    /**
     * The far end is specified here, the near end in the connect()
     * call.
     * 
     * The address passed is a public TLS server address, which
     * contains, directly or indirectly, the server's public key.
     */
    public Connector getConnector (Address farAddr, boolean blocking) 
                                                throws IOException {
        if (blocking) {
            return new TlsConnector (
                    new EndPoint(this, (IPAddress)farAddr));
        } else {
            //return new SchedulableTcpConnector (
            //        new EndPoint(this, (IPAddress)farAddr));
            throw new UnsupportedOperationException();
        }
    }
    /**
     * We might want to call this "tls/tcp".
     */
    public String name () {
        return "tls";
    }
    // ACCESS METHODS ///////////////////////////////////////////////
    public Tcp getTcp() {
        return myTcp;
    }
}
