/* Tcp.java */
package org.xlattice.transport.tcp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;

import org.xlattice.Address;

import org.xlattice.Acceptor;
import org.xlattice.Connection;
import org.xlattice.Connector;
import org.xlattice.EndPoint;
import org.xlattice.Transport;

import org.xlattice.transport.ClientServer;
import org.xlattice.transport.IPAddress;

/**
 * @author Jim Dixon
 */

public class Tcp                            implements ClientServer {

    // INTERFACE Transport ///////////////////////////////////////////
    public Acceptor getAcceptor (Address nearAddr, boolean blocking) 
                                                throws IOException {
        if (blocking) {
            return new TcpAcceptor (
                    new EndPoint(this, (IPAddress)nearAddr));
        } else {
            return new SchedulableTcpAcceptor (
                    new EndPoint(this, (IPAddress)nearAddr));
        }
    }
    public final Socket makeSocket (Address nearAddr, Address farAddr)
                                                throws IOException {
        InetAddress nearHost;
        if (nearAddr == null)
            nearHost = null;
        else
            nearHost = ((IPAddress)nearAddr).getHost();
        InetAddress farHost;
        if (farAddr == null)
            farHost = null;
        else
            farHost = ((IPAddress)farAddr).getHost();
        // WILL GET NPE IF EITHER Host NULL
        return new Socket (                                   
            farHost,  ((IPAddress)farAddr).getPort(),
            nearHost, ((IPAddress)nearAddr).getPort());
    }
    /**
     * This method is quasi-deprecated.
     */
    public Connection getConnection (Address nearAddr, Address farAddr,
                            boolean blocking)   throws IOException {
        if (blocking) {
            if (nearAddr == null && farAddr == null)
                return new TcpConnection(this);
            else
                return new TcpConnection (this, 
                                        makeSocket(nearAddr, farAddr));
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
     */
    public Connector getConnector (Address farAddr, boolean blocking) 
                                                throws IOException {
        if (blocking) 
            return new TcpConnector (
                    new EndPoint(this, (IPAddress)farAddr));
        else 
            return new SchedulableTcpConnector (
                    new EndPoint(this, (IPAddress)farAddr));
    }
    public String name () {
        return "tcp";
    }
    // PROTOCOL-SPECIFIC METHODS ////////////////////////////////////
    
    // OTHER METHODS ////////////////////////////////////////////////
}
