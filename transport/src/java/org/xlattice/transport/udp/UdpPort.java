/* UdpPort.java */
package org.xlattice.transport.udp;

import java.nio.ByteBuffer;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.xlattice.Address;
import org.xlattice.transport.BlockingPacketPort;
import org.xlattice.transport.IPAddress;
import org.xlattice.transport.PacketPort;


public class UdpPort extends PacketPort implements BlockingPacketPort {

    private DatagramSocket socket;

    // CONSTRUCTORS /////////////////////////////////////////////////
    private final void createSocket()           throws IOException {
        if (! (nearAddr instanceof IPAddress) )
            throw new IllegalArgumentException(
                    "local address is not an IPAddress");
        IPAddress a = (IPAddress)nearAddr; 
        socket = new DatagramSocket(a.getPort(), a.getHost());
        a.setPort( socket.getLocalPort() );
    }
    protected UdpPort (InetAddress a, int p)    throws IOException {
        super( new IPAddress (a, p) );          // sets state to BOUND
        createSocket();
    }
    protected UdpPort (InetAddress a)           throws IOException {
        super( new IPAddress (a, 0) );
        createSocket();
    }
    protected UdpPort (Address a)               throws IOException {
        super( a );
        createSocket();
    }
    // INTERFACE PacketPort /////////////////////////////////////////
    /**
     */
    public void setTimeout (int ms)             throws IOException {
        super.setTimeout(ms);
        if (state == CLOSED)
            throw new IllegalStateException (
                    "cannot set timer on closed UdpPort");
        socket.setSoTimeout(ms);    // can throw SocketException
    }
    
    public void bindFarEnd (Address a)          throws IOException {
        if (!(a instanceof IPAddress) )
            throw new IllegalArgumentException(
                    "far address is not an IP address");
        IPAddress ipA = (IPAddress)a;
        bindFarEnd( ipA.getHost(), ipA.getPort() );
    }
    public void bindFarEnd (InetAddress a, int p)
                                                throws IOException {
        super.bindFarEnd( new IPAddress (a, p) );
        if (state != BOUND)
            throw new IllegalStateException(
                    "far end is already bound or PacketPort is closed");
        socket.connect (a, p);
    }            
    public void unbindFarEnd()                  throws IOException {
        super.unbindFarEnd();
        socket.disconnect();        // a no-op if not connected
    }
    public void close()                         throws IOException {
        super.close();
        socket.close();
    }
    /**
     * XXX If the socket and the superclass disagree about whether
     * the socket is closed, this implementation closes it at both
     * levels.  The alternative would seem to be to force the 
     * superclass to agree with the socket, marking it open if the
     * socket is open.  This leads to complexities, in that you 
     * must set the state to either BOUND or CONNECTED.  The simpler
     * solution is to silently close it.
     */
    public boolean isClosed() {
        boolean socketClosed = socket.isClosed();
        boolean superClosed  = super.isClosed();
        
        if ( (socketClosed && !superClosed) 
                || (superClosed && !socketClosed) ) {
            try {close();} catch (IOException ioe) {}
            return true;
        }
        return superClosed;
    }
    // I/O ////////////////////////////////////////////////
    /** 
     * Check whether the PacketPort is bound and the ByteBuffer
     * is non-null.
     */
    protected final void checkBound(ByteBuffer dest) {
        if (dest == null)
            throw new IllegalArgumentException("null ByteBuffer");
        if ( state != BOUND )
            throw new IllegalStateException("can't do I/O if not bound");
    }       
    /** 
     * Check whether the PacketPort is connected and the ByteBuffer
     * is non-null.
     */
    protected final void checkConnected(ByteBuffer dest) {
        if (dest == null)
            throw new IllegalArgumentException("null ByteBuffer");
        if ( state != CONNECTED )
            throw new IllegalStateException("can't do I/O if not connected");
    }
    /** 
     * Accept a packet through a connected PacketPort.
     */
    public int receive (ByteBuffer buff)        throws IOException {
        checkConnected(buff);
        /* STUB */
        return -1;
    }
    /** 
     * Accept a packet through a bound but not connected PacketPort.
     *
     * XXX Should examine SO_RCVBUF, receive buffer size.
     */
    public Address receiveFrom(ByteBuffer buff) throws IOException {
        checkBound(buff);
        int len = buff.array().length;          // XXX HACK
        DatagramPacket p = new DatagramPacket(buff.array(), len);
        socket.receive(p);
        buff.position(0);
        buff.limit(p.getLength());
        return new IPAddress( p.getAddress(), p.getPort() );
    }
    /** 
     *
     * XXX Should examine SO_SNDBUF, receive buffer size.
     */
    public int send (ByteBuffer buff)           throws IOException {
        checkConnected(buff);
        /* STUB */
        return -1;
    }
    /**
     * XXX Simplistic implementation - should check offsets in ByteBuffer.
     *
     * XXX Should examine SO_SNDBUF, receive buffer size.
     */
    public int sendTo (ByteBuffer buff, Address target) 
                                                throws IOException {
        checkBound(buff);
        if (!(target instanceof IPAddress))
            throw new IllegalArgumentException(
                    "far address must be IPAddress");
        IPAddress a = (IPAddress)target;
        int count = buff.array().length;        // XXX HACK
        DatagramPacket packet = new DatagramPacket(
                        buff.array(), count, a.getHost(), a.getPort());
        socket.send( packet );
        return count;
    }
    
    
    // InetAddress + port VIEW OF ADDRESSES /////////////////////////
    // XXX Local SHOULD BE Near
    public InetAddress getLocalHost() {
        return socket.getLocalAddress();
    }
    public int getLocalPort() {
        return socket.getLocalPort();
    }
    /**
     * If connected, returns the address of the remote host.  Otherwise 
     * returns null.
     */
    public InetAddress getFarHost() {
        return socket.getInetAddress();
    }
    /**
     * If connected, returns the far port.  Otherwise returns -1.
     */
    public int getFarPort() {
        return socket.getPort();
    }
    // INTERFACE SchedulablePacketPort //////////////////////////////

    // OTHER METHODS ////////////////////////////////////////////////
    // EQUALS/HASHCODE //////////////////////////////////////////////
    // SERIALIZATION ////////////////////////////////////////////////
    
}
