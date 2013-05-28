/* SchedulableTcpConnector.java */
package org.xlattice.transport.tcp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.xlattice.Connection;
import org.xlattice.EndPoint;
import org.xlattice.Transport;
import org.xlattice.transport.CnxListenerFactory;
import org.xlattice.transport.ConnectionListener;
import org.xlattice.transport.IOScheduler;
import org.xlattice.transport.IPAddress;
import org.xlattice.transport.SchedulableConnection;
import org.xlattice.transport.SchedulableConnector;

/**
 * @author Jim Dixon
 */

public class SchedulableTcpConnector implements  SchedulableConnector {

    private final EndPoint    _farEnd;
    /** redundant references to _farEnd internals */
    private final Transport   transport;
    private final InetAddress farHost;
    private final int farPort;

    private       IOScheduler        receiver;
    private       CnxListenerFactory listenerFactory;
    private       SelectionKey       sKey;
    private       SocketChannel      sChan;

//  public SchedulableTcpConnector (IPAddress farAddr)
//                                              throws IOException {
//      this (farAddr.getHost(), farAddr.getPort());
//  }
//  public SchedulableTcpConnector (InetAddress farHost, int farPort)
//                                              throws IOException {
//      if (farHost == null)
//          throw new IllegalArgumentException ("null remote host");
//      if (farPort <= 0 || farPort > 65535)
//          throw new IllegalArgumentException ("port out of range");
//      this.farHost = farHost;
//      this.farPort = farPort;
//      sChan = SocketChannel.open();
//      sChan.configureBlocking(false);
//  } 
    public SchedulableTcpConnector (EndPoint farEnd)
                                                throws IOException {
        if (farEnd == null)
            throw new IllegalArgumentException ("null far EndPoint");
        _farEnd  = (EndPoint)farEnd; 
        transport = _farEnd.getTransport();
        IPAddress farAddr = (IPAddress) _farEnd.getAddress();
        farHost = farAddr.getHost();
        farPort = farAddr.getPort();
        sChan = SocketChannel.open();
        sChan.configureBlocking(false);
    }
   
    // INTERFACE SchedulableConnector ///////////////////////////////
    /**
     * An experiment.
     */
    public SchedulableConnection connection ()  throws IOException {
        Socket sock = sChan.socket();
        sock.bind(null);        // XXX A HACK
        sChan.connect( new InetSocketAddress(farHost, farPort) );
        // XXX Exceptions?
        ConnectionListener listener = listenerFactory.getInstance();
        return new SchedulableTcpConnection (sChan, receiver, listener);
    }
    public SelectableChannel getChannel() {
        return sChan;
    }
    public SelectionKey getKey() {
        return sKey;
    }
    public SchedulableConnector setKey(SelectionKey key) {
        if (key == null)
            throw new IllegalArgumentException("null selection key");
        sKey = key;
        return this;
    }
    public CnxListenerFactory getCnxListenerFactory () {
        return listenerFactory;
    }
    public SchedulableConnector setCnxListenerFactory (
                                    CnxListenerFactory factory) {
        if (factory == null)
            throw new IllegalArgumentException ("null listener factory");
        listenerFactory = factory;
        return this;
    }
    public IOScheduler getReceiver () {
        return receiver;
    }
    public SchedulableConnector setReceiver (IOScheduler receiver) {
        if (receiver == null)
            throw new IllegalArgumentException ("null receiver");
        this.receiver = receiver;
        return this;
    }
    // INTERFACE Connector //////////////////////////////////////////
    /**
     * Makes the connection between the remote host/port and local
     * host/port.
     *
     * If the remote host is in fact another port on this same host
     * the connection should be made immediately.
     */
    public Connection connect (EndPoint nearEnd, boolean blocking)
                                                throws IOException {
        return connect((IPAddress)(nearEnd.getAddress()), blocking);
    }
    public EndPoint getFarEnd() {
        return (EndPoint)_farEnd;
    }
    // PROTOCOL-SPECIFIC METHODS ////////////////////////////////////
    public TcpConnection connect (IPAddress nearEnd, boolean blocking)
                                                throws IOException {
        if (blocking)
            throw new UnsupportedOperationException();
        
        // XXX STUB
        return null;
        
    }
    // OTHER METHODS ////////////////////////////////////////////////
}
