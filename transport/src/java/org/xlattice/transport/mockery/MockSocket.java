/* MockSocket.java */
package org.xlattice.transport.mockery;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;  // extends Socket Address
import java.net.Socket;
import java.net.SocketAddress;      // abstract
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Random;

/**
 * A mock for java.net.Socket for use in unit testing.  The mock socket
 * has near and far ends.  If connected and bound, these should refer
 * to valid host IP addresses and port numbers.  Input and output
 * streams can be associated with the socket, so that test data can
 * be fed to programs using the socket and responses can be collected
 * for analysis.  Various options (SO_KEEPALIVE, OOBINLINE, etc) are
 * supported and their values can be checked, but are ignored.
 *
 * @author Jim Dixon
 */
public class MockSocket extends Socket {

    // XXX NO, I DON'T BELONG HERE
    public static final byte[] LOOPBACK = new byte[] {
        (byte)127, (byte)0, (byte)0, (byte)0 };

    // The order is significant, but not the actual values.
    // Pretend you're writing in BASIC ;-)
    public static final int UNBOUND      =  0;
    public static final int BOUND        = 10;
    public static final int PENDING      = 20;
    public static final int CONNECTED    = 30;
    public static final int DISCONNECTED = 40;
    private int state;
    
    private InputStream input;          // what the socket will produce
    private OutputStream output;        // data written to the socket

    // the socket itself
    private InetAddress localAddr;
    private int         localPort;
    private String      localHost;
    
    private InetAddress remoteAddr;
    private int         remotePort;
    private String      remoteHost;
   
    private boolean inputShutdown;
    private boolean keepAliveEnabled;
    private boolean outputShutdown;
    private boolean receiptOfUrgentDataEnabled;
    private boolean reuseAddrEnabled;
    private boolean soLingerEnabled;
    private boolean tcpNoDelay;     // by default disables Nagle's alg

    /** in seconds */
    private int soLinger = 10;
    private int soRcvBuf = 65536; 
    private int soSndBuf = 65536;
    /** in ms, 0 means infinity */
    private int soTimeout;
    /** RFC1349 precedence and TOS fields */
    private int trafficClass;

    Random rng = new Random (new Date().getTime());
    // CONSTRUCTORS /////////////////////////////////////////////////
    // these match the standard constructors, other than the 
    // deprecated ones
    /**
     * Create an unconnected mock socket.
     */
    public MockSocket () {
        state = UNBOUND;
    }
   
    /**
     * Create a mock socket, specifying the remote host and 
     * connecting to it.  Default values are accepted for the local
     * end.
     */
    public MockSocket (InetAddress remoteAddr, int remotePort) 
                                                throws IOException {
        bind    ( null );
        connect ( new InetSocketAddress (remoteAddr, remotePort) );
    }
    /**
     * Create a mock socket, specifying both ends of the connection
     * by address and port number.
     */
    public MockSocket (InetAddress remoteAddr, int remotePort, 
                       InetAddress localAddr,  int localPort) 
                                                throws IOException {
        bind    ( new InetSocketAddress (localAddr,  localPort)  );
        connect ( new InetSocketAddress (remoteAddr, remotePort) );
    } 
    /**
     * Create a mock socket, specifying the remote host by name and
     * port number, and connecting to it.
     */
    public MockSocket (String host, int port)   throws IOException {
        this (InetAddress.getByName(host), port);
    }
    /**
     * Create a mock socket, specifying the remote host by name and
     * port number, and specifying the local end.
     */
    public MockSocket (String host,           int port, 
                       InetAddress localAddr, int localPort) 
                                                throws IOException {
        this (InetAddress.getByName(host), port, localAddr, localPort);
    }
    
    // SPECIAL TEST METHODS /////////////////////////////////////////
    public int getState() {
        return state;
    }
    /**
     * Assign input and output streams to the mock socket.  Both are
     * stream interfaces to ByteBuffers.  For testing, 'input' data 
     * should be supplied via the ByteBuffer underlying the in stream
     * and output should be collected from the ByteBuffer underlying
     * the out stream.
     *
     * These streams may only be set once.
     *
     * @param in  input data stream
     * @param out collects output from the MockSocket
     */
    public void setStreams (ByteBufferInputStream in, 
                            ByteBufferOutputStream out) {
        if (input != null || output != null)
            throw new IllegalStateException (
                    "input or output has already been set");
        if (in == null)
            throw new IllegalArgumentException (
                    "input stream may not be null");
        input  = in;
        if (out == null) 
            out = new ByteBufferOutputStream(); // accept default length
        output = out;
    }
    // OTHER METHODS //////////////////////////////////////////////// 
    /**
     * Bind this socket to a local address, a host:port or address:port 
     * pair.  If the local address is null, pick a random port and a 
     * valid local address.
     * 
     * @param bindpoint the local address
     * @throws IOException if the socket is already bound
     */
    public void bind (SocketAddress bindpoint) throws IOException {
        if (state != UNBOUND)
            throw new IllegalStateException (
                    "socket has already been bound");
        if (bindpoint == null) {
            localAddr = InetAddress.getByAddress (LOOPBACK);
            localPort = 1024 + rng.nextInt( 65536 - 1024 );
        } else {
            if (! (bindpoint instanceof InetSocketAddress) )
                throw new IllegalStateException (
                        "can only handle InetSocketAddress");
            InetSocketAddress local = (InetSocketAddress) bindpoint;
            localAddr = local.getAddress();
            localPort = local.getPort();
            localHost = local.getHostName();
        }
        state = BOUND;
    }

    public void close()                         throws IOException {
        if (state == CONNECTED) {
            state = DISCONNECTED;
            if (input != null)
                input.close();
            if (output != null)
                output.close();
        } else 
            throw new IOException("socket is not connected");
    }
    /**
     * Connect this socket to a server.
     */
    public void connect (SocketAddress endpoint)
                                                throws IOException {
        if (state >= CONNECTED)
            throw new IOException ("socket already connected");
        if (state != BOUND)
            throw new IOException("socket has not been bound");
        if (endpoint == null)
            throw new IllegalArgumentException();
        if (! (endpoint instanceof InetSocketAddress) )
            throw new IllegalStateException ("");
        InetSocketAddress remote = (InetSocketAddress) endpoint;
        remoteAddr = remote.getAddress();
        remotePort = remote.getPort();
        remoteHost = remote.getHostName();
        state = CONNECTED;
    }
    /**
     * Connect this socket to a server with the timeout specified in ms.
     * In this implementation, the timeout is ignored.
     */
    public void connect (SocketAddress endpoint, int timeout)
                                                    throws IOException {
        if (timeout < 0)
            throw new IllegalArgumentException();
        if (timeout == 0)
            connect(endpoint);
        else
            connect(endpoint);  // just ignore the timeout
    }
    /** Stub; always returns null. */
    public SocketChannel getChannel() { return null; }

    /**
     * @return the remote IP address if connected, null otherwise
     */
    public InetAddress getInetAddress() {
        if (state == CONNECTED)
            return remoteAddr;
        else
            return null;
    }
    /**
     * @return a reference to the input stream
     */
    public InputStream getInputStream () {
        return input;
    }
    /**
     * @return whether keep-alives are enabled
     */
    public boolean getKeepAlive () {
        return keepAliveEnabled;
    }
    /**
     * @return the local address, 32 bits if IPv4
     */
    public InetAddress getLocalAddress() {
        if (localAddr != null)
            return localAddr;
        else try {
            return InetAddress.getByName("0.0.0.0");
        } catch (UnknownHostException e) {
            return null;
        }
    }
    /**
     * Returns the local port or -1 if not bound.
     *
     * @return the local port number 
     */
    public int getLocalPort() {
        if (state >= BOUND)
            return localPort;
        else
            return -1;
    }
    /**
     * If bound, returns the address of the endpoint; otherwise
     * returns null;
     */
    public SocketAddress getLocalSocketAddress() {
        if (state >= BOUND)
            return new InetSocketAddress (localAddr, localPort);
       else
            return null;
    }
    /**
     * @return whether receipt of urgent TCP data is enabled 
     */
    public boolean getOOBInLine() {
        return receiptOfUrgentDataEnabled;
    }
   
    /**
     */
    public OutputStream getOutputStream() {
        return output;
    }
    
    /**
     * Returns the remote port or zero if not connected.
     *
     * @return the remote port number 
     */
    public int getPort() {
        if (state >= CONNECTED)
            return remotePort;
        else
            return 0;
    }

    /**
     * Get the value of SO_RCVBUF.
     */
    public int getReceiveBufferSize() {
        return soRcvBuf;
    }
    
    public SocketAddress getRemoteSocketAddress() {
        if (state >= CONNECTED)
            return new InetSocketAddress(remoteAddr, remotePort);
        else
            return null;
    }
    public boolean getReuseAddress () {
        return reuseAddrEnabled;
    }
    public int getSendBufferSize() {
        return soSndBuf;
    }
    public int getSoLinger() {
        if (soLingerEnabled)
            return soLinger;
        else 
            return -1;
    }
    public int getSoTimeout() {
        return soTimeout;
    }
    /** @return whether Nagle's algorithm is enabled */
    public boolean getTcpNoDelay() {
        return tcpNoDelay;
    }
    public int getTrafficClass() {
        return trafficClass;
    }
    public boolean isBound() {
        return state >= BOUND;
    }
    public boolean isClosed() {
        return state >= DISCONNECTED;
    }
    public boolean isConnected() {
        return state == CONNECTED;
    }
    public boolean isInputShutdown () {
        return inputShutdown;
    }
    public boolean isOutputShutdown () {
        return outputShutdown;
    }
    public void sendUrgentData (int data) {
        // do nothing
    }
    public void setKeepAlive (boolean on) {
        keepAliveEnabled = on;
    }
    public void setOOBInLine (boolean on) {
        receiptOfUrgentDataEnabled = on;
    }
    public void setReceiveBufferSize (int size) {
        if (size <= 0)
            throw new IllegalArgumentException();
        soRcvBuf = size;
    }

    /**
     * Enable/disable the SO_REUSEADDR socket option.
     */
    public void setReuseAddress (boolean on) {
        reuseAddrEnabled = on;
    }
    public void setSendBufferSize (int size) {
        if (size <= 0)
            throw new IllegalArgumentException();
        soSndBuf = size;
    }
    /////////////////////////////////////////////////////////////////
    // static void setSocketImplFactory (SocketImplFactory fac) {}
    /////////////////////////////////////////////////////////////////
    
    /**
     * Enable/disable SO_LINGER.  
     * @param on     whether to enable
     * @param linger time in seconds
     */
    public void setSoLinger (boolean on, int linger) {
        if (linger < 0)
            throw new IllegalArgumentException();
        soLingerEnabled = on;
    }
    /**
     * @param timeout in ms
     */
    public void setSoTimeout (int timeout) {
        if (timeout < 0)
            timeout = 0;
        soTimeout = timeout;
    }
    /**
     * Enable/disable Nagle's algorithm.
     */
    public void setTcpNoDelay(boolean on) {
        tcpNoDelay = on;
    }
    /**
     * Set the RFC1394 precedence and TOS fields 
     */
    public void setTrafficClass (int tc) {
        if ( tc < 0 || tc > 255)
            throw new IllegalArgumentException();
        trafficClass = tc;
    }
    public void shutdownInput() {
        inputShutdown = true; 
    }
    public void shutdownOutput() {
        outputShutdown = true; 
    }
    public String toString() {
        return new StringBuffer ("MockSocket: ")
            .append (localAddr).append(":").append(localPort)
            .append (" ==> ")
            .append (remoteAddr).append(":").append(remotePort)
            .toString();
    }
}
