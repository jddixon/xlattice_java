/* TcpConnection.java */
package org.xlattice.transport.tcp;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.xlattice.Address;
import org.xlattice.Connection;
import org.xlattice.EndPoint;
import org.xlattice.Key;
import org.xlattice.PublicKey;
import org.xlattice.Transport;
import org.xlattice.transport.IPAddress;

/**
 * @author Jim Dixon
 */

public class TcpConnection implements Connection {

    private final Transport transport;
    private final Socket socket;
    private int     state;
    private boolean encrypted; 
    
    // CONSTRUCTORS /////////////////////////////////////////////////
    public TcpConnection(Transport t) {
        this(t, null);
    }
    public TcpConnection (Transport t, Socket socket) {
        if (t == null)
            throw new IllegalArgumentException("null Transport");
        transport = t;
        if (socket == null) {
            socket = new Socket();      // unbound, unconnected
            state = UNBOUND;
        }
        else {
            if (socket.isClosed())
                throw new IllegalStateException("socket is closed");
            else if (socket.isInputShutdown() || socket.isOutputShutdown())
                throw new IllegalStateException(
                    "socket is wholly or partially shutdown");
            // XXX INCOMPLETE IMPLEMENTATION: NEED TO EXAMINE STATE
            // XXX MORE CAREFULLY
            if (socket.getLocalPort() <= 0)
                state = UNBOUND;
            else if (socket.getPort() <= 0)
                state = BOUND;
            else
                state = CONNECTED;
        }
        this.socket = socket;
        
    }
    // Connection INTERFACE /////////////////////////////////////////
    // STATE //////////////////////////////////////////////
    public int getState () {
        return state;  
    }
    /**
     * TCP/IP bind().
     */
    public void bindNearEnd(EndPoint near)      throws IOException {
        if (state >= BOUND)
            throw new IllegalStateException("already bound");
            
        Address nearAddr = near.getAddress();
        if (nearAddr instanceof IPAddress)
            socket.bind(((IPAddress)nearAddr).getSocketAddress());
        else 
            throw new UnsupportedOperationException ();
        state = BOUND;
    }
    /**
     * TCP/IP connect().
     */
    public void bindFarEnd (EndPoint far)       throws IOException {
        if (state >= CONNECTED)
            throw new IllegalStateException("already connected");
        if (state < BOUND)
            throw new IllegalStateException("near end not yet bound");
        Address farAddr = far.getAddress();
        if (farAddr instanceof IPAddress)
            socket.connect(((IPAddress)farAddr).getSocketAddress());
        else 
            throw new UnsupportedOperationException ();
        state = CONNECTED;
    }
    public void close()                         throws IOException {
        state = DISCONNECTED;
        socket.close();
    }
    /**
     * Whether the connection is DISCONNECTED.  The peculiar 
     * implementation allows us to correct out-of-date state
     * values.
     */
    public boolean isClosed() {
        if (state == DISCONNECTED)
            return true;
        if (socket.isClosed()) {
            state = DISCONNECTED;
            return true;
        }
        return false;
    }
    // END POINTS /////////////////////////////////////////
    /**
     * Return the Connection's far/remote EndPoint.  This 
     * implementation glosses over the way -1 and 0 ports are
     * handled.
     */
    public EndPoint getFarEnd() {
        int nearPort = socket.getLocalPort();
        if (nearPort <= 0)
            return null;            // not yet bound
        int farPort = socket.getPort();
        if (farPort <= 0) {
            return null;
        } else {
            return new EndPoint (transport, new IPAddress (
                                    socket.getInetAddress(), farPort));
        }
    }
    /**
     * XXX Problem with this implementation.
     */
    public EndPoint getNearEnd() {
        // XXX if near end is constructed from null, the port address is
        // -1, which causes InetSocketAddress to throw an
        //      IllegalArgumentException: port out of range:-1
        int port = socket.getLocalPort();
        if (port <= 0) {
            return null;
        } 
        else 
            return new EndPoint (transport, new IPAddress (
                        socket.getLocalAddress(), socket.getLocalPort()));
    }
    // I/O ////////////////////////////////////////////////
    public boolean isBlocking() {
        return true;
    }
    public InputStream getInputStream()         throws IOException {
        return socket.getInputStream();
    }
    public OutputStream getOutputStream()       throws IOException {
        return socket.getOutputStream();
    }
    // ENCRYPTION /////////////////////////////////////////
    public boolean isEncrypted () {
        return encrypted;
    }
    public void negotiate (Key myKey, PublicKey hisKey) {
    }
    // PROTOCOL-SPECIFIC VERSION OF METHODS /////////////////////////
    /**
     * TCP/IP bind().
     */
    public void bindNearEnd(InetSocketAddress near) 
                                                throws IOException {
        socket.bind(near);
    }
    /**
     * TCP/IP connect().  
     * XXX VERSION WITH timeout NOT SUPPORTED.
     * XXX Can the connection on a socket be changed??
     *
     * @throws IOException
     * @throws IllegalBlockingModeException
     * @throws IllegalArgumentException if null endpoint or unsupported
     *              SocketAddress subclass
     */
    public void bindFarEnd(InetSocketAddress far) 
                                                throws IOException {
        socket.connect(far);
    }
    // OTHER METHODS ////////////////////////////////////////////////
    protected void setEncrypted (boolean b) {
        encrypted = b;
    }
    /**
     * @return TCP/IP socket, possibly null
     */
    public Socket socket() {
        return socket;
    }
    public String toString() {
        return new StringBuffer("TcpConnection: ")
            .append(getNearEnd())
            .append(" --> ")
            .append(getFarEnd())
            .toString();
    }
}
