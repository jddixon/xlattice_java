/* SchedulableTcpAcceptor.java */
package org.xlattice.transport.tcp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.xlattice.Connection;
import org.xlattice.EndPoint;
import org.xlattice.transport.CnxListenerFactory;
import org.xlattice.transport.ConnectionListener;
import org.xlattice.transport.IOScheduler;
import org.xlattice.transport.IPAddress;
import org.xlattice.transport.SchedulableAcceptor;

import org.xlattice.Transport; 
import org.xlattice.util.NonBlockingLog; 

/**
 * @author Jim Dixon
 */

public class SchedulableTcpAcceptor implements SchedulableAcceptor {

    // DEBUG
    private final NonBlockingLog debugLog 
                            = NonBlockingLog.getInstance("debug.log");
    private void DEBUG_MSG(String s) {
        debugLog.message("SchedTcpAcc" + s);
    }
    // END
    //public final TcpAcceptor        tcpAcceptor;   // blocking counterpart

    private final EndPoint nearEnd;
    private       Transport transport;      // yes, redundant
    
    public final ServerSocketChannel srvChan;

    private      IOScheduler        receiver;
    private      CnxListenerFactory listenerFactory;

    private SelectionKey key;

    // CONSTRUCTORS /////////////////////////////////////////////////
    public SchedulableTcpAcceptor (EndPoint nearEnd)
                                                throws IOException {
        this (nearEnd, null, null);
    }
//  public SchedulableTcpAcceptor (IPAddress addr)
//                                              throws IOException {
//      this (addr, null, null);
//  }
//  public SchedulableTcpAcceptor (IPAddress addr,
//                                 IOScheduler receiver,
//                                 ConnectionListener listener) 
//                                              throws IOException {
//      this (addr.getHost(), addr.getPort(), receiver, listener);
//  } 
//  /**
//   * Creates an acceptor which will assign incoming connections
//   * to the specified IOScheduler after attaching a ConnectionListener.
//   *
//   * @param host     IP address we are listening on
//   * @param port     port number we listen on; if null, ephemeral
//   * @param receiver the scheduler connections are assigned to
//   * @param connListener instance of the ConnectionListener attached
//   */
//  public SchedulableTcpAcceptor (InetAddress host, int port,
//                           IOScheduler receiver,
//                           ConnectionListener connListener)
//                                          throws IOException {
//      // can't make this test because if called from Tcp.getAcceptor 
//      // these are null
//      if (receiver == null || connListener == null)
//          throw new IllegalArgumentException(
//              "receiver and/or connection listener null");
//      nearEnd = new EndPoint (Tcp.class, new IPAddress(host, port));
//      this.receiver = receiver;
//      this.connListener = connListener;
//      srvChan = ServerSocketChannel.open();
//      srvChan.configureBlocking(false);
//      
//      ServerSocket srvSock = srvChan.socket();
//      // XXX THIS SHOULD BE OPTIONAL
//      srvSock.setReuseAddress(true);  // must precede bind()
//      srvSock.bind(new InetSocketAddress(host, port));
//      
//      // calls setReuseAddress
//      //tcpAcceptor = new TcpAcceptor(srvSock);
//  } // GEEP

    /**
     * Creates an acceptor which will assign incoming connections
     * to the specified IOScheduler after attaching a ConnectionListener.
     *
     * @param endPoint IP address:port we are listening on
     * @param receiver the scheduler connections are assigned to
     * @param factory  ConnectionListener factory
     */
    public SchedulableTcpAcceptor (EndPoint endPoint,
                             IOScheduler receiver,
                             CnxListenerFactory factory)
                                            throws IOException {
        // can't make this test because if called from Tcp.getAcceptor 
        // these are null
//      if (receiver == null || factory == null)
//          throw new IllegalArgumentException(
//              "receiver and/or connection listener null");

        if (endPoint == null)
            throw new IllegalArgumentException ("null near EndPoint");
        nearEnd   = (EndPoint) endPoint;
        transport = nearEnd.getTransport();
        IPAddress nearAddr = (IPAddress)nearEnd.getAddress();
        InetAddress host   = nearAddr.getHost();
        int port           = nearAddr.getPort();
        
        this.receiver = receiver;
        this.listenerFactory = factory;
        srvChan = ServerSocketChannel.open();
        srvChan.configureBlocking(false);
        
        ServerSocket srvSock = srvChan.socket();
        // XXX THIS SHOULD BE OPTIONAL
        srvSock.setReuseAddress(true);  // must precede bind()
        srvSock.bind(new InetSocketAddress(host, port));
        
        // calls setReuseAddress
        //tcpAcceptor = new TcpAcceptor(srvSock);
    } // GEEP

    // SchedulableAcceptor INTERFACE ////////////////////////////////
    /**
     * The accept key handler.  Bear in mind that srvChan returns a
     * fully connected sChan.
     *
     * @return a reference to the new SchedulableConnection
     */
    public Connection accept()              throws IOException {
        if (receiver == null || listenerFactory == null)
            throw new IllegalStateException (
                    "receiver and/or listener factory has not been set");
        SocketChannel sChan = null;
        try {
            sChan = srvChan.accept();
        } catch (IOException ioe) {
            /* ignore for now */
            DEBUG_MSG(": srvChan.accept() caused IOException");
        }
        DEBUG_MSG(".accept(), channel "
                + sChan.hashCode()); 
        SchedulableTcpConnection conn = null;
        if (sChan != null) {
            sChan.configureBlocking(false);
            // create a new instance of the ConnectionListener
            ConnectionListener listener;
            DEBUG_MSG(".accept(), creating listener");
            // XXX EXCEPTIONS?
            listener = listenerFactory.getInstance();
            conn = new SchedulableTcpConnection(
                                        sChan, receiver, listener);
            DEBUG_MSG(".accept(), adding connection, channel "
                    + sChan.hashCode());
            receiver.add (conn);
        }
        return conn;
    }
    // PROPERTIES /////////////////////////////////////////
    public SelectableChannel getChannel() {
        return srvChan;
    }
    
    public CnxListenerFactory getCnxListenerFactory() {
        return listenerFactory;
    }
    /**
     * Set the CnxListenerFactory assigned to new connections 
     * created by this acceptor. 
     */
    public SchedulableAcceptor setCnxListenerFactory 
                                        (CnxListenerFactory factory) {
        if (factory == null)
            throw new IllegalArgumentException("null CnxListenerFactory");
        listenerFactory = factory;
        return this;
    }

    public SelectionKey getKey() {
        return key;
    } 
    public SchedulableAcceptor setKey (SelectionKey selKey) {
        if (selKey == null)
            throw new IllegalArgumentException("null selection key");
        if (key != null)
            throw new IllegalStateException("key has already been set");
        if (receiver == null || listenerFactory == null)
            throw new IllegalStateException (
                    "receiver and/or listener factory has not been set");
        key = selKey;
        return this;
    }

    public IOScheduler getReceiver () {
        return receiver;
    }
    /**
     * Set the IOScheduler assigned to new connections created by
     * this acceptor.
     */
    public SchedulableAcceptor setReceiver (IOScheduler sched) {
        if (sched == null)
            throw new IllegalArgumentException ("null receiver");
        receiver = sched;
        return this;
    }
    // INTERFACE Acceptor ///////////////////////////////////////////
    public void close ()                    throws IOException {
        srvChan.close();
    }
    public boolean isClosed() {
        return !srvChan.isOpen();
    }
    public EndPoint getEndPoint() {
        return nearEnd;
    }
    // OTHER METHODS ////////////////////////////////////////////////
    public String toString() {
        return new StringBuffer("SchedulableTcpAcceptor: ")
            .append(nearEnd.toString())
            .toString();
    }
}
