/* PingPonger.java */
package org.xlattice.transport.udp;

import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

import org.xlattice.*;
import org.xlattice.transport.*;
import org.xlattice.util.Queue;

public class PingPonger implements PacketPortListener, Runnable {

    public final static byte PING = (byte) PingPongPkt.PING;
    public final static byte PONG = (byte) PingPongPkt.PONG;

    protected final Timer           timer = new Timer();
    
    /** access to outQ must be synchronized */
    protected final Queue           outQ  = new Queue();
    /** synchronize access to this flag on outQ */
    protected       boolean         sendInProgress = false;

    protected final int             NDX;
    /** number of PingPongers */
    protected final int             N;
    /** number of neighbors pinged */
    protected final int             K;
    /** basic time perio in ms */
    protected final int             D;
    /** period between pings, in terms of D */
    protected final int             J;
    /** maximum number of pings to send */
    protected final int             Z;
    
    protected final IPAddress[]     peers;
    protected final int[]           dest;
    protected final PingPongPkt[]   packets;
    protected       SchUdpPort      udpPort;
    protected       ByteBuffer      inBuf;
    
    private         Thread          myThread;
    private volatile boolean running;
    
    // tracker bug 1492580 ////////////////////////////////
    protected final int             baseMsgID;
    
    // tracker bug 1492582 ////////////////////////////////
    /** use this to sync access to the counts */
    protected final Object statsLock = new Object();
    protected final int[] pingCounts;
    protected final int[] pongCounts;
   
    protected       boolean pongsIn = false;
    
    public PingPonger ( final int ndx, final int nodeN,
                            IPAddress[] neighbors, PingPongPkt[] pkts,
                            int d, int j, int z) {
        if (ndx < 0)
            throw new IllegalArgumentException("negative index");
        NDX = ndx;
        if (nodeN < 0 || NDX >= nodeN)
            throw new IllegalArgumentException("impossible node count: "
                    + nodeN);
        N = nodeN;
        if ( neighbors == null || neighbors.length == 0
               || pkts == null || pkts.length == 0) 
            throw new IllegalArgumentException(
                    "null or empty peer list or packet list");
        peers     = neighbors;
        K = neighbors.length;
        if (K >= N)
            throw new IllegalArgumentException ("more neighbors ("
                    + K + ") than number of nodes (" + N + ") allows");
       
        pingCounts = new int[K];
        pongCounts = new int[K];

        packets   = pkts;
        baseMsgID = packets[0].getMsgID();
        if (packets.length != K)
            throw new IllegalArgumentException(
                    K + " peers but " + packets.length + " packets");
        D = d;  // XXX must be > 0
        J = j;  // XXX ditto
        Z = z;  // XXX interesting! must be >= 0
        

        dest = new int[ K ];
        
        // make sure packets are reasonable
        for (int i = 0; i < K; i++) {
            PingPongPkt p = packets[i];
            if (p.getType() != PING)
                throw new IllegalArgumentException(
                    "packet " + i + " is of type " + p.getType());
            if (p.getSrc() != NDX)
                throw new IllegalArgumentException(
                    "packet " + i + " should have src " + NDX 
                    + " but it's " + p.getSrc());
            dest[i] = p.getDest();
            if (dest[i] < 0 || N <= dest[i])
                throw new IllegalArgumentException(
                    "packet " + i + " has impossible destination " 
                    + dest[i]);
            if (dest[i] == NDX)
                throw new IllegalArgumentException(
                    "packet " + i + " pings me!");
            int len = p.getLen();
            if (len + 8 != p.value.length)
                throw new IllegalArgumentException(
                    "packet " + i + " payload length " + len 
                    + " doesn't match overall length of " + p.value.length);

        }
    }
    public class OutPacket {
        final IPAddress     addr;
        final ByteBuffer    buffer;
        public OutPacket(IPAddress a, ByteBuffer b) {
            addr = a;
            buffer = b;
        }
    }
    // TimerTasks ///////////////////////////////////////////////////
    public class Pinger                         extends TimerTask {
        final    int pingIndex;
        volatile int pingsSoFar;
        final    int maxPings;
        final    ByteBuffer data;
        final    IPAddress to;
        /**
         * @param i index of the ping packet to send
         * @param z maximum number of pings to send
         */
        public Pinger ( int i, int z) {
            pingIndex  = i;
            pingsSoFar = 0;
            maxPings   = z;
            
            data = ByteBuffer.wrap(packets[pingIndex].value);
            to   = peers  [pingIndex];
        }
        // INTERFACE Runnable /////////////////////////////
        /**
         * If this gets invoked, we sent a packet, whether or not we
         * are still running.
         */
        public void run() {
            // DEBUG
            // System.out.println("Pinger " + NDX + ':' + pingIndex 
            //        + " sending ping " + pingsSoFar);
            // END
            data.rewind();
            sendTo( data, to );
            synchronized (statsLock) {
                pingCounts[pingIndex]++;
            }
            if (++pingsSoFar >= maxPings || !running )
                cancel();       // this TimerTask
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
            }
        }
    }
    // INTERFACE Runnable ///////////////////////////////////////////
    // and related methods //////////////////////////////////////////
    public void run () {
        myThread = Thread.currentThread();
        running  = true;
        // DEBUG
        // System.out.println("PingPonger " + NDX + " running");
        // END
        Pinger[] pingers = new Pinger[K];
        for (int k = 0; k < K; k++) {
            pingers[k] = new Pinger (k, Z);
            timer.schedule ( pingers[k], 
                             (long) (k + 1) * D,    // initial delay
                             (long) J * D);         // period
        }
        boolean stillRunning = true;
        while (stillRunning) {
            try { Thread.currentThread().sleep(50); }
            catch (InterruptedException ie) { }
            stillRunning = false;
            for (int k = 0; k < K; k++) {
                int pingCount, pongCount;
                synchronized(statsLock) {
                    pingCount = pingCounts[k];
                    pongCount = pongCounts[k];
                }
                if (pongCount < Z) {
                    // DEBUG
                    System.out.println("PingPonger " + NDX 
                        + ": pingCounts[" + k + "] = " + pingCount
                        + ", pongCounts[" + k + "] = " + pongCount);
                    // END
                    stillRunning = true;
                    break;
                }
            }
        }
        pongsIn = true;
        timer.cancel();
        while (running) {
            try { Thread.currentThread().sleep(50); }
            catch (InterruptedException ie) { }
        }
    }
    
    /**
     * Shuts down the Listener.  Caller should expect this to block.
     */
    public void close () {
        running = false;
    }
    public Thread getThread() {
        return myThread;
    }
    public boolean isRunning() {
        return running;
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
        // DEBUG
        // System.out.println("PingPonger " + NDX + ": dataSent()");
        // END
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
        System.arraycopy(value, 0, dataIn, 0, inBuf.limit());
        PingPongPkt pktIn = new PingPongPkt(dataIn);
        byte type    = pktIn.getType();
        byte src     = pktIn.getSrc();
        byte dest    = pktIn.getDest();
        byte dataLen = pktIn.getLen();
        int  msgID   = pktIn.getMsgID();
        
        if (type == PING) {
//          // DEBUG
//          System.out.println("PingPonger " + NDX 
//                  + ": dataReceived(), PING from " + dataIn[1]
//                  + ", msgID " + pktIn.getMsgID() );
//          // END
            int len1   = inBuf.limit();
            int pktLen = dataLen + 8;
            if (len1 != pktLen)
                System.out.println(
                    "inBuf limit is " + len1
                    + " but length computed from packet is " + pktLen);
            byte[] dataOut = new byte[pktLen];
            System.arraycopy(dataIn, 0, dataOut, 0, pktLen);
            PingPongPkt pktOut = new PingPongPkt(dataOut);    
            pktOut.setType(PONG);
            pktOut.setSrc(dest);
            pktOut.setDest(src);
            sendTo( ByteBuffer.wrap(dataOut), udpPort.sender);
        } else if (type == PONG) {
            /** tracker bug 1492580 */
            int pktIndex = msgID - baseMsgID;
            if ( pktIndex < 0 || pktIndex >= K) {
                System.out.println("PONG packet msgID " + msgID 
                        + " is out of range");
            } else {
                PingPongPkt pingPacket = packets[pktIndex]; 
                if (pingPacket.getLen() != dataLen) {
                    System.out.println("PONG packet msgID " + msgID
                        + " has wrong data length");
                } else {
                    // expensive, does a copy
                    byte[] pongData = pktIn.getData();
                    byte[] pingData = pingPacket.getData();
                    boolean ok = true;
                    for (int i = 0; i < dataLen; i++) {
                        if (pingData[i] != pongData[i]) {
                            ok = false;
                            System.out.println("PONG packet msgID " + msgID
                               + " data differs from PING at offset " + i);
                            break;
                        }
                    }
                    if (ok) {
                        synchronized(statsLock) {
                            pongCounts[pktIndex]++;
                        }
                    }
                }

            }
        } else {
            // DEBUG
            System.out.println("PingPonger " + NDX 
                    + ": dataReceived(), unknown/unexpected type  " + value[0]
                    + " from " + value[1]);
            // END
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
}
