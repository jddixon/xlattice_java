/* TcpConnector.java */
package org.xlattice.transport.tcp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.xlattice.Connection;
import org.xlattice.Connector;
import org.xlattice.EndPoint;
import org.xlattice.Transport;
import org.xlattice.transport.IPAddress;

/**
 * @author Jim Dixon
 */

public class TcpConnector implements Connector {

    protected final EndPoint    _farEnd;
    /** redundant references to EndPoint internals */
    protected       Transport   transport;
    protected final InetAddress farHost;
    protected final int         farPort;

//  /**
//   * Set up the mechanism for establishing connections from this
//   * host (EndPoint as yet unspecified) to a far EndPoint, that is,
//   * to an Acceptor on another host (network node).
//   *
//   * @param host must not be null
//   * @param port must be a positive unsigned short
//   * @throws IllegalArgumentException if the port number is out of range
//   */
//  public TcpConnector (InetAddress host, int port)
//                                              throws IOException {
//      if (host == null)
//          throw new IllegalArgumentException("null remote host");
//      if (port <= 0 || port > 65535)
//          throw new IllegalArgumentException("port value out of range: "
//                  + port);
//      farHost = host;
//      farPort = port;
//  }
//  public TcpConnector (InetSocketAddress farAddr)
//                                              throws IOException {
//      this (farAddr.getAddress(), farAddr.getPort());
//  }
//  public TcpConnector (IPAddress farAddr)     throws IOException {
//      this (farAddr.getHost(), farAddr.getPort());
//  } // FOO
    /**
     * XXX This only makes sense if the near and far end use the same 
     * transport.
     */
    public TcpConnector (EndPoint farEnd)       throws IOException {
        if (farEnd == null)
            throw new IllegalArgumentException("null far EndPoint");
        _farEnd = (EndPoint)farEnd;
        
        transport = _farEnd.getTransport();
        IPAddress farAddr = (IPAddress)_farEnd.getAddress();
        farHost = farAddr.getHost();
        farPort = farAddr.getPort();
    }
    // Connector INTERFACE //////////////////////////////////////////
    /** @return a copy of the far EndPoint */
    public EndPoint getFarEnd() {
        return (EndPoint) _farEnd;
    }
    public Connection connect(EndPoint nearEnd, boolean blocking)
                                                throws IOException {
        // XXX WON'T WORK: Tls DOES NOT EXTEND Tcp XXx
//      if (! (nearEnd.getTransport() instanceof Tcp) ) {
//          throw new IllegalArgumentException (
//                                  "near end not a TCP endpoint");
//      }
        IPAddress nearAddr = (IPAddress)nearEnd.getAddress();
        return connect (nearAddr.getHost(), nearAddr.getPort(), blocking);
    }
    // PROTOCOL-SPECIFIC ////////////////////////////////////////////
    /**
     * Create a Connection to the server from the local Address 
     * specified.  If the nearPort is zero, the operating system
     * picks an unused ephemeral port number.
     *
     * Normally nearPort should be zero.
     *
     * @param nearHost one of this host's Internet names or IP addresses
     * @param nearPort the port used; usually 0
     */
    public Connection connect (InetAddress nearHost, int nearPort,
                            boolean blocking)   throws IOException {
        if (blocking) {
            Socket socket  = new Socket ();
            socket.bind ( new InetSocketAddress (nearHost, nearPort) );
            socket.connect( new InetSocketAddress (farHost, farPort) );
            return new TcpConnection(transport, socket);
        } else {
            throw new UnsupportedOperationException();
        }
    }
    public Connection connect (IPAddress nearAddr, boolean blocking)
                                                throws IOException {
        return connect (nearAddr.getHost(), nearAddr.getPort(), blocking);
    }
    // OTHER METHODS ////////////////////////////////////////////////
    public String toString() {
        return new StringBuffer("TcpConnector: ")
            .append(farHost.toString())
            .append(":")
            .append(farPort)
            .toString();
    }
}
