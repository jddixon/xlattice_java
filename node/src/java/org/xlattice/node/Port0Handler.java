/* Port0Handler.java */
package org.xlattice.node;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.xlattice.NodeID;
import org.xlattice.Peer;
import org.xlattice.protocol.xl.*;
import org.xlattice.transport.IPAddress;
import org.xlattice.transport.PacketPortListener;
import org.xlattice.transport.SchPacketPort;
import org.xlattice.transport.udp.SchUdpPort;
import org.xlattice.util.Queue;
import org.xlattice.util.StringLib;

public class Port0Handler implements PacketPortListener, Runnable, XLConst {

    protected final TNode           myNode;
    protected final Timer           timer;
    
    // outQ AND FLAG ////////////////////////////////////////////////
    /** access to outQ must be synchronized */
    protected final Queue           outQ  = new Queue();
    /** synchronize access to this flag on outQ */
    protected       boolean         sendInProgress = false;
    // END outQ AND FLAG ////////////////////////////////////////////

    /** number of neighbors to be pinged */
    protected final int K;
    /** basic time period in ms */
    protected final int D;      // ms
    /** period between pings, in terms of D */
    protected final int J;      // period = J * D ms
    /** maximum number of pings to send */
    protected final int Z ;     // <= 0 means infinity
   
    /** timeout on pings */
    protected       int timeout = 250;  // ms, enough to go around the world

    protected final Peer[]          peers;
    protected final IPAddress[]     addrs;
    
    /** non-blocking UDP port associated with this handler */
    protected       SchUdpPort      udpPort;
    /** reference to udpPort's input buffer */
    protected       ByteBuffer      inBuf;
   
    // CONSTRUCTORS /////////////////////////////////////////////////
    /**
     * @param neighbors  initial list of Peers
     * @param addrs      UDP addresses of those Peers, in same order
     * @param d          fundamental time base in ms
     * @param j          period between pings, J * D ms
     * @param z          maximum number of pings (if <= 0, infinite)
     */
    public Port0Handler (   final TNode parent, 
                            Peer[] neighbors, IPAddress[] addrs,
                            int d, int j, int z) {
        // XXX check ??
        myNode = parent;
        timer = myNode.getTimer();
        if ( neighbors == null || neighbors.length == 0)
            throw new IllegalArgumentException(
                    "null or empty peer list");
        peers     = neighbors;
        // XXX BE CONSISTENT - check or don't
        this.addrs = addrs;
        D = d;
        J = j;
        K = neighbors.length;
        Z = z;
    }
    // STATISTICS ///////////////////////////////////////////////////
    private Object statsLock = new Object();
    private long pktsIn  = 0;
    private long pktsOut = 0;
    public long getPktsIn() {
        synchronized (statsLock) {
            return pktsIn;
        }
    }
    public long getPktsOut() {
        synchronized (statsLock) {
            return pktsOut;
        }
    }

    // TimerTasks ///////////////////////////////////////////////////
    /** synchronize access on the HashMap itself */
    public HashMap timeouts = new HashMap();
    class Timeout                               extends TimerTask {
        final Pinger     myPinger;
        final MessageID  msgID;
        public Timeout(Pinger p, MessageID m) {
            if (p == null || m == null)
                throw new IllegalArgumentException (
                        "null Pinger or msgID");
            myPinger = p;
            msgID    = m;
        }
        public void run() {
            myPinger.cancel();
            // DEBUG
            System.out.println("Timeout for "
                + StringLib.byteArrayToHex(msgID.id)
                + "; killing Pinger "
                + myNode.port0Addr.getPort() + ':' + myPinger.peerIndex 
            );
            // END
        }
        public String toString() {
            return new StringBuffer("Timeout pinger ")
                        .append(myPinger.peerIndex)
                        .append(": ")
                        .append(msgID)
                        .toString();
        }
    }
    class OutPacket {
        final IPAddress     addr;
        final ByteBuffer    buffer;
        public OutPacket(IPAddress a, ByteBuffer b) {
            addr = a;
            buffer = b;
        }
    }
    class Pinger                                extends TimerTask {
        final    int peerIndex;
        volatile int pingsSoFar;
        final    int maxPings;
        private  boolean cancelled = false;
        /**
         * @param i index of the ping packet to send
         * @param z maximum number of pings to send
         */
        public Pinger ( int i, int z) {
            peerIndex  = i;
            pingsSoFar = 0;
            maxPings   = z;
        }
        // INTERFACE Runnable /////////////////////////////
        /**
         * If this gets invoked, we sent a packet, whether or not we
         * are still running.
         */
        public void run() {
            if (pingsSoFar <= maxPings) {
                // DEBUG
                System.out.println("  ping " + myNode.port0Addr.getPort() 
                        + " --> " + addrs[peerIndex].getPort()
                        + ", total of " + (pingsSoFar + 1));
                // END
                XLMsg ping = new Ping();
                // XXX OPTIMIZATION: save the next two values
                ping.add( new Source( myNode.getNodeID() ) );
                ping.add( new Destination( peers[peerIndex].getNodeID() ) );
                byte[] data = new byte[ ping.wireLength() ];
                ping.encode(data);
                ByteBuffer buf = ByteBuffer.wrap(data);
                MessageID pingMsgID = new MessageID(ping.getMsgID());
                if (timeout > 0) {
                    Timeout killPinger = new Timeout(this, pingMsgID);
                    timer.schedule(killPinger, timeout);
                    synchronized(timeouts) {
                        timeouts.put( pingMsgID,  killPinger);
                    }
                }
                sendTo( buf, addrs[peerIndex] );
                if ((maxPings > 0) && (++pingsSoFar >= maxPings)) {
                    cancel();
                } 
            }
        }
        public boolean cancel() {
            cancelled = true;
            return super.cancel();
        }
        public boolean getCancelled() {
            return cancelled;
        }
    }
    protected void sendTo ( ByteBuffer data, IPAddress to ) {
        synchronized (outQ) {
            if (sendInProgress) {
                OutPacket out = new OutPacket(to, data);
                outQ.enqueue(out);
            } else {
                sendInProgress = true;
                udpPort.sendData (data, to);
                synchronized(statsLock) {
                    pktsOut++;
                }
            }
        }
    }
    // INTERFACE Runnable ///////////////////////////////////////////
    protected Pinger[] pingers;
    
    public void run () {
        // DEBUG
        System.out.println("Port0Handler " 
            + myNode.port0Addr.getPort() + " run()"
            + "; timeout = " + timeout       
        );
        
        // END
        pingers = new Pinger[K];
        for (int k = 0; k < K; k++) {
            pingers[k] = new Pinger (k, Z);
            timer.schedule ( pingers[k], 
                             (long) (k + 1) * D,    // initial delay
                             (long) J * D);         // period
        }
        boolean running = true;
        Thread myThread = Thread.currentThread();
        while (running) {
            try {myThread.sleep(50);}catch(InterruptedException ie){}
            running = !stopped();
        }
    }
    public boolean stopped() {
        for (int i = 0; i < pingers.length; i++)
            if (!pingers[i].getCancelled())
                return false;
        return true;
    }
    // INTERFACE PacketPortListener /////////////////////////////////
    /**
     * Tells the listener what PacketPort it is listening to and what
     * input buffer it should use.  The listener can get the IOScheduler
     * and SelectionKey from the SchPacketPort, but should not need to.
     *
     * Called by SchPacketPort.setKey().
     */
    public void setPacketPort (SchPacketPort spp, ByteBuffer buffer) {
        if (spp == null || buffer == null)
            throw new IllegalArgumentException(
                    "null SchPacketPort or ByteBuffer");
        inBuf = buffer;
        udpPort = (SchUdpPort) spp;
        udpPort.initiateReading();
    }
   
   
    /** 
     * Used by the PacketPort to report that transmission of an
     * item has been completed.  
     *
     * The address of the recipient is in SchUdpPort.addressee.
     */
    public void dataSent () {
        synchronized (outQ) {
            if ( outQ.isEmpty() ) {
                sendInProgress = false;
            } else {
                OutPacket out = (OutPacket) outQ.dequeue();
                udpPort.sendData (out.buffer, out.addr);
            }
        }
    }

    /**
     * Used by SchUdpPort to report that some data has been received
     * and is in the input buffer. 
     *
     * The address of the sender is in the variable SchUdpPort.sender.
     */
    public void dataReceived () {
        byte[] value = inBuf.array();
        byte[] dataIn = new byte[ inBuf.limit() ];
        // XXX NOT NECESSARY but no way to pass limit() to decode()
        System.arraycopy(value, 0, dataIn, 0, inBuf.limit());
        synchronized(statsLock) {
            pktsIn++;       // but may get dropped 
        } 
        XLMsg pktIn = null;
        try {
            pktIn = XLMsg.decode(dataIn);
        } catch (IOException ioe) {
            /* STUB */
        }
        
        if (pktIn.type == PING) {
//          // DEBUG
//          System.out.println("Port0Handler " + NDX 
//                  + ": dataReceived(), PING from " + value[1]
//                  + ", msgID " + pktIn.getMsgID() );
//          // END

            XLMsg pktOut = new Pong( pktIn.getMsgID() );
            NodeID srcID  = null;
            NodeID destID = null;   // error if not this node
            for (int  i = 0; i < pktIn.size(); i++) {
                XLAttr attr = pktIn.get(i);
                if (attr.type == SOURCE)
                    srcID  = new NodeID( attr.value );
                else if (attr.type == DESTINATION)
                    destID = new NodeID( attr.value );
            }
            // XXX check not null, right destination   
            pktOut.add(new Source(destID));
            pktOut.add(new Destination(srcID));
            byte[] dataOut = new byte[ pktOut.wireLength() ];
            pktOut.encode(dataOut);
            // DEBUG
            System.out.println("  pong " + myNode.port0Addr.getPort() 
                    + " --> " + udpPort.getSender().getPort()
//                  + ", total of " + pingsSoFar
                    );
            // END
           
            // under load, it appears that sender is sometimes null
            IPAddress sender = udpPort.getSender();
            if (sender == null) {
                /* SHOULD LOG EXCEPTION and gracefully recover from NPEs */
                System.out.println("null sender, dropping packet");
            } else {
                sendTo( ByteBuffer.wrap(dataOut), udpPort.getSender());
            }
        } else if (pktIn.type == PONG) {
            MessageID pongMsgID = new MessageID(pktIn.getMsgID());
            Timeout killer;
            synchronized(timeouts) {
                killer = (Timeout)timeouts.remove(pongMsgID);
            }
            if (killer == null) {
                // DEBUG
                System.out.println("no timeout found for "
                        + StringLib.byteArrayToHex(pongMsgID.id));
                // END
            } else {
                killer.cancel();
            }
//          // DEBUG
//          System.out.println("Port0Handler " + NDX 
//                  + ": dataReceived(), PONG from " + value[1]
//                  + ", msgID " + pktIn.getMsgID() );
//          // END
            /* STUB */
        } else {
//          // DEBUG
//          System.out.println("Port0Handler " + NDX 
//                  + ": dataReceived(), unknown/unexpected type  " 
//                  + pktIn.type 
//                  + " from " + pktIn.???);
//          // END
            /* STUB */
        }
        udpPort.initiateReading();
        
    }

    /**
     * Used by SchUdpPort to report an exception.  
     */
    public void reportException (Exception exc) {
        System.out.println("unexpected exception: " + exc);
    }
    // PROPERTIES ///////////////////////////////////////////////////
    public int getTimeout() {
        return timeout;
    }
    public void setTimeout(int ms) {
        if (ms < 0)
            ms = 0;             // construed as infinity
        timeout = ms;
    }
}
