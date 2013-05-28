/* TcpAcceptor.java */
package org.xlattice.transport.tcp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketException;

import org.xlattice.Acceptor;
import org.xlattice.Address;
import org.xlattice.Connection;
import org.xlattice.EndPoint;
import org.xlattice.Transport;
import org.xlattice.transport.IPAddress;

/**
 * A thin wrapper around a ServerSocket.
 *
 * @author Jim Dixon
 */
public class TcpAcceptor implements Acceptor {

    // 256 for RedHat 7.3, 100 for Windows 2000 Professional
    public final static int DEFAULT_BACKLOG = 128;

    private final EndPoint nearEnd;
    private       Transport transport;
    
    private final ServerSocket srvSock;

    // CONSTRUCTORS /////////////////////////////////////////////////
    public TcpAcceptor (EndPoint nearEnd)       throws IOException {
        if (nearEnd == null)
            throw new IllegalArgumentException("null nearEnd");
        this.nearEnd = nearEnd;
        transport = nearEnd.getTransport();
        IPAddress myAddr = (IPAddress)nearEnd.getAddress();
        InetAddress host = myAddr.getHost();
        int port = myAddr.getPort();
        try {
            srvSock = new ServerSocket ();
            srvSock.setReuseAddress(true);
            srvSock.bind( new InetSocketAddress (host, port),
                          DEFAULT_BACKLOG);
        } catch (SocketException se) {
            // DEBUG
            System.out.println("host = " + host + ", port = " + port);
            // END
            // se.printStackTrace();
            throw new IOException ("can't create TcpAcceptor - " + se);
        }
    }
//  public TcpAcceptor (InetAddress host, int port)
//                                              throws IOException {
//      try {
//          srvSock = new ServerSocket ();
//          srvSock.setReuseAddress(true);
//          srvSock.bind( new InetSocketAddress (host, port),
//                        DEFAULT_BACKLOG);
//      } catch (SocketException se) {
//          // DEBUG
//          System.out.println("host = " + host + ", port = " + port);
//          // END
//          // se.printStackTrace();
//          throw new IOException ("can't create TcpAcceptor - " + se);
//      }
//      nearEnd = new EndPoint (Tcp.class, new IPAddress(host, port));
//  } // GEEP

//  public TcpAcceptor (InetSocketAddress addr) throws IOException {
//      this (addr.getAddress(), addr.getPort());
//  }
//  public TcpAcceptor (IPAddress addr)         throws IOException {
//      this (addr.getHost(), addr.getPort());
//  } 
//  public TcpAcceptor (ServerSocket srvSock)   throws IOException {
//      // DO REASONABLENESS CHECKS
//      this.srvSock = srvSock;
//      srvSock.setReuseAddress(true);
//      nearEnd = new EndPoint (Tcp.class, new IPAddress(
//                                              srvSock.getInetAddress(),
//                                              srvSock.getLocalPort()));
//  }
    // Acceptor INTERFACE //////////////////////////////////////////
    /**
     * Blocking accept.  Waits indefinitely for a new connection.
     *
     * @return a reference to the new Connection
     */
    public Connection accept()              throws IOException {
        return new TcpConnection (transport, srvSock.accept());
    }
    public void close ()                    throws IOException {
        srvSock.close();
    }
    public boolean isClosed() {
        return srvSock.isClosed();
    }
    public EndPoint getEndPoint () {
        return nearEnd;
    }
    // OTHER METHODS ////////////////////////////////////////////////
    public ServerSocket socket() {
        return srvSock;
    }
    public String toString() {
        return new StringBuffer("TcpAcceptor: ")
            .append(nearEnd.toString())
            .toString();
    }
}
