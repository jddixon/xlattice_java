/* SchUdpPort.java */
package org.xlattice.transport.udp;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

import org.xlattice.Address;
import org.xlattice.transport.BlockingPacketPort;
import org.xlattice.transport.IOScheduler;
import org.xlattice.transport.IPAddress;
import org.xlattice.transport.PacketPort;
import org.xlattice.transport.PacketPortListener;
import org.xlattice.transport.SchPacketPort;
import org.xlattice.util.NonBlockingLog;

public class SchUdpPort extends PacketPort implements SchPacketPort {

    // STATICS //////////////////////////////////////////////////////
    
    // XXX There should be some way to change this, at least in the 
    // constructor.  No experiments to check whether it causes problems.
    // Might want to use a value in the parent class as a default.
    public static final int PKT_BUFSIZE = 65536;
        
    // INSTANCE VARIABLES ///////////////////////////////////////////
    /** assigned by the constructor, so should be final */
    private       DatagramChannel     chan;
    private       DatagramSocket      socket;

    /** not assigned by the constructor */
    private IOScheduler         scheduler;
    private PacketPortListener  listener;
    private SelectionKey        sKey;
 
    // INPUT AND OUTPUT BUFFERS ///////////////////////////

    /** last datagram received */
    protected ByteBuffer inBuf = ByteBuffer.allocate(PKT_BUFSIZE);
    /** where whatever is in the input buffer came from */
    protected IPAddress  sender;
    
    /** buffer used for outgoing messages, from listener */
    protected ByteBuffer outBuf;
    /** target of outgoing messages, from listener */
    protected InetSocketAddress addressee;
   
    // DEBUG LOGGING //////////////////////////////////////
    protected final NonBlockingLog log  
                        = NonBlockingLog.getInstance("SchUdpPort.log");
    protected       String PROMPT;
    protected void LOG_MSG (String s) {
        log.message(PROMPT + s);
    }
    protected void setUpLog (int port) {
        PROMPT = new StringBuffer("port ")
                    .append(port).append(": ").toString();
    }
    // END DEBUG

    // CONSTRUCTORS /////////////////////////////////////////////////
    private final void createChannel()           throws IOException {
        if (! (nearAddr instanceof IPAddress) )
            throw new IllegalArgumentException(
                    "near address is not an IPAddress");
        InetSocketAddress isa = ((IPAddress)nearAddr).getSocketAddress(); 
        chan = DatagramChannel.open();
        chan.configureBlocking(false);
        socket = chan.socket();
        socket.bind(isa);
        // DEBUG
        setUpLog( ((IPAddress)nearAddr).getPort() );
        // END
    }
    public SchUdpPort (InetAddress a, int p)
                                                throws IOException {
        super( new IPAddress (a, p) );
        createChannel();
    }
    public SchUdpPort (InetAddress a)        throws IOException {
        this(a, 0);
    }
    public SchUdpPort (Address a)            throws IOException {
        super( a );
        createChannel();
    }
    public SchUdpPort (DatagramChannel chan) throws IOException {
        super();
        if (chan == null)
            throw new IllegalArgumentException("null DatagramChannel");
        this.chan = chan;
        chan.configureBlocking(false);

        // we require that the channel be bound
        socket = chan.socket();
        if (!socket.isBound())
            socket.bind(null);      // binds to wildcard address

        InetAddress nearHost = socket.getLocalAddress();
        int         nearPort = socket.getLocalPort();
        nearAddr = new IPAddress( nearHost, nearPort );
        setState(BOUND);
        if (socket.isConnected()) {
            InetAddress farHost = socket.getInetAddress();
            int         farPort = socket.getPort();
            farAddr = new IPAddress( farHost, farPort );
            setState(CONNECTED);
        }
        setUpLog(nearPort);             // DEBUG
    }
    // INTERFACE PacketPort /////////////////////////////////////////
    /**
     */
    public void setTimeout (int ms)             throws IOException {
        super.setTimeout(ms);
        if (state == CLOSED)
            throw new IllegalStateException (
                    "cannot set timer on closed SchUdpPort");
        // XXX This must be implemented with a timer.
        /* STUB */
    }
    
    public void bindFarEnd (Address a)          throws IOException {
        if (!(a instanceof IPAddress) )
            throw new IllegalArgumentException(
                    "far address is not an IP address");
        IPAddress ipA = (IPAddress)a;
        bindFarEnd( ipA.getHost(), ipA.getPort() );
        chan.connect(ipA.getSocketAddress());
        setState(CONNECTED);
    }
    public void bindFarEnd (InetAddress a, int p)
                                                throws IOException {
        super.bindFarEnd( new IPAddress (a, p) );
        if (state != BOUND)
            throw new IllegalStateException(
                    "far end is already bound or PacketPort is closed");

        /* STUB */
    }            
    public void unbindFarEnd()              throws IOException {
        super.unbindFarEnd();
        setState(BOUND);
        chan.disconnect();        // a no-op if not connected
    }
    public void close()                     throws IOException {
        super.close();
        chan.close();
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
        boolean chanClosed  = !chan.isOpen();
        boolean superClosed = super.isClosed();
        
        if ( (chanClosed && !superClosed) 
                || (superClosed && !chanClosed) ) {
            try { close(); } catch (IOException ioe) {}
            return true;
        }
        return superClosed;
    }
    
    // InetAddress + port VIEW OF ADDRESSES /////////////////////////
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
    // INTERFACE SchPacketPort //////////////////////////////////////
    public IOScheduler  getScheduler() {
        return scheduler;
    }
    /**
     * XXX Might want to make scheduler immutable.
     */
    public void setScheduler(IOScheduler ios) {
        if (ios == null)
            throw new IllegalArgumentException("null IOScheduler");
        scheduler = ios;
    }
    public PacketPortListener getListener() {
        return listener;
    }
    /**
     * XXX Might want to make listener immutable.
     */
    public void setListener(PacketPortListener ppl) {
        if (ppl == null)
            throw new IllegalArgumentException("null PacketPortListener");
        listener = ppl;
    }
    
    public SelectableChannel  getChannel() {
        return chan;
    }
    
    public void setKey(SelectionKey key) {
        if (key == null)
            throw new IllegalArgumentException("null SelectionKey");
        sKey = key;
        listener.setPacketPort(this, inBuf);
    }
    public SelectionKey getKey() {
        return sKey;
    }

    // INTERFACE TO IOScheduler ///////////////////////////
    /**
     * Called by the IOScheduler when some data has been received.
     */
    public void readyToRead() {
        LOG_MSG("readyToRead()");
        inBuf.clear();
        // possible exceptions: ClosedChannel, AsynchronousClose, 
        // ClosedByInterrupt, Security, IOException
        try { 
            sender =  new IPAddress ((InetSocketAddress) chan.receive(inBuf));
            // XXX Very dodgey: if nothing received, should just ignore,
            // and interestOps() can itself throw an exception
            //
            // XXX HACK - commented this out and tests succeed !
            //sKey.interestOps(
            //      sKey.interestOps() & ~SelectionKey.OP_READ);
            if (inBuf.position() > 0) {
                inBuf.flip();
                listener.dataReceived();
            }
        } catch (IOException ioe) {
            listener.reportException(ioe);
        } 
    }
    /**
     * Called by the IOScheduler when data can be written. 
     *
     * Provisionally, ignored if there is no data ready to send.
     */
    public void readyToWrite() {
        LOG_MSG("readyToWrite()");
        processOutBuffer();
    }
    protected void processOutBuffer() {
        if (outBuf != null && addressee != null) {
            try {
                chan.send( outBuf, addressee );
                // might block; might throw IllegalArg or CancelledKeyException
                sKey.interestOps(
                        sKey.interestOps() & ~SelectionKey.OP_WRITE);
                listener.dataSent();
            } catch (IOException ioe) {
                listener.reportException(ioe);
            }
        }
        // XXX LOG IF CANNOT SEND?
    }
    // INTERFACE TO PacketPortListener ////////////////////
    /**
     *  Called by the PacketPortListener (or assignee) to signal that it
     *  is prepared to receive data.
     */
    public void initiateReading() {
        LOG_MSG("initiateReading()");
        // might block, and might throw IllegalArg or CancelledKeyException
        sKey.interestOps(
                    sKey.interestOps() | SelectionKey.OP_READ);
    }

    /** 
     * Called by the PacketPort Listener to initiate a write to an
     * unconnected PacketPort.  
     */
    public void sendData (ByteBuffer buff, Address target) {
        if (target == null)
            throw new IllegalArgumentException("null target address");
        if (!(target instanceof IPAddress))
            throw new IllegalArgumentException(
                    "not an IPAddress: " + target.toString());
        addressee = ((IPAddress)target).getSocketAddress();
        sendData (buff);
    }
    /** 
     * Called by the PacketPortListener to initiate a write to a 
     * connected port or to an unconnected port after the address
     * has been set up.
     * 
     * The write will not occur until the IOScheduler signals that 
     * it should proceed by calling readyToWrite().
     *
     * This implementation is a bit dodgey.  Should check whether
     * connected; in other words, there should be two entirely 
     * separate sendData()s.
     */
    public void sendData (ByteBuffer buff) {
        if (buff == null) 
            throw new IllegalArgumentException("null output buffer");
        outBuf = buff;
       
        // DEBUG
        int before = sKey.interestOps();
        // END
        sKey.interestOps(sKey.interestOps() | SelectionKey.OP_WRITE);
        // DEBUG
        int after = sKey.interestOps();
        LOG_MSG("sendData to " + addressee + "; sKey " + before + " => " + after);
        // END
        //
        // XXX A HACK: don't see why we should call this here - but if we
        // don't, nothing is sent
        processOutBuffer();
    }

    // OTHER METHODS ////////////////////////////////////////////////
    public IPAddress getSender() {
        return sender;
    }
    // EQUALS/HASHCODE //////////////////////////////////////////////
    // SERIALIZATION ////////////////////////////////////////////////
}
