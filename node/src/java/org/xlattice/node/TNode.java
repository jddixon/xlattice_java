/* TNode.java */
package org.xlattice.node;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.xlattice.Address;
import org.xlattice.NodeID;
import org.xlattice.Peer;
import org.xlattice.Overlay;
import org.xlattice.crypto.RSAKey;
import org.xlattice.protocol.stun.NattedAddress;
import org.xlattice.transport.IOScheduler;
import org.xlattice.transport.IPAddress;
import org.xlattice.transport.udp.SchUdpPort;

/**
 * Represents a Node which executes in a separate thread within the
 * same JVM as as the runner.
 *
 * TENTATIVELY: a TNode exposes a functional interface to the XLattice
 * messaging (XM) protocol; this includes or is extended to include a close().
 *
 * The TNode also incorporates an XM interpreter, which translates XM
 * messages into function calls and vice-versa.
 *
 * @author Jim Dixon
 */
public class TNode extends AbstractNode implements Runnable {
    
    // INSTANCE VARIABLES ///////////////////////////////////////////
    private volatile boolean running = false;
    private Thread myThread;
    protected final IOScheduler scheduler;
    protected final Timer timer;

    protected SchUdpPort   port0     = null;
    protected IPAddress    port0Addr = null;
    protected Port0Handler p0Handler = null;
    protected int          timeout   = 250; // ms

    // PING PARAMETERS ////////////////////////////////////
    // Used to be wired-in constants, no longer are.
    /** basic time period in ms */
    protected int D = 1000;     // ms, default
    /** period between pings, in terms of D */
    protected int J = 360;      // default 6min = 360s 
    /** maximum number of pings to send */
    protected int Z = 0;        // <= 0 means infinity
   
    protected TNode()                           throws IOException {
        this(null, null, null, null, null);
    }
    public TNode(RSAKey key, NodeID myID, File myDir, 
                             Overlay [] myOverlays, Peer[] myPeers) 
                                                throws IOException {
        super (key, myID, myDir, myOverlays, myPeers); 
        scheduler = new IOScheduler();  // runs in separate thread
        timer     = new Timer();        // -ditto-
    }

    // TIMER ////////////////////////////////////////////////////////
    /** access to the thread-safe tnode timer */
    public Timer getTimer() {
        return timer;
    }
    // PARAMETERS ///////////////////////////////////////////////////
    public int getD() { 
        return D; 
    }
    public int getJ() { 
        return J; 
    }
    public int getZ() { 
        return Z; 
    }
    public void setD(int n) { 
        if (n < 1) 
            throw new IllegalArgumentException("D mst be positive: " + n); 
        D = n; 
    }
    public void setJ(int n) { 
        if (n < 1) 
            throw new IllegalArgumentException("J mst be positive: " + n); 
        J = n; 
    }
    public void setZ(int n) { 
        if (n < 1) 
            throw new IllegalArgumentException("Z mst be positive: " + n); 
        Z = n; 
    }
    // this has an effect ONLY if called before the Port0Handler is
    // created
    public void setTimeout(int ms) {
        if (ms < 0)
            ms = 0;                 // construed as infinity
        timeout = ms;
    }
    public int getTimeout() {
        return timeout;
    }
    // THREAD-RELATED ///////////////////////////////////////////////
    public void run() {
        myThread = Thread.currentThread();
        while (!scheduler.isRunning()) 
            try { myThread.sleep(2); } catch (InterruptedException ie){}

        // scan this node's overlays, open ports //////////
        for (int i = 0; i < overlays.size(); i++) {
            Overlay o = (Overlay)overlays.get(i);
            if (o.transport().equals("udp") && o.protocol().equals("xl")) {
                Address a = o.address();
                if (a instanceof IPAddress)
                    port0Addr = (IPAddress) a;
                else if (a instanceof NattedAddress)
                    port0Addr = new IPAddress(
                        ((NattedAddress)a).getLocalHost(),
                        ((NattedAddress)a).getLocalPort() );
                try {
                    port0 = new SchUdpPort(port0Addr);
                    break;              // XXX other ports ignored
                } catch (IOException ioe) {
                    // XXX SHOULD LOG
                    // DEBUG
                    System.out.println("can't open " + port0Addr
                            + " - " + ioe);
                    // END
                    port0 = null;
                    port0Addr = null;
                }
            }
            
        }
        // DEBUG
        System.out.println("Node " + nodeID.toString().substring(0, 8) 
                         + "...  " + port0Addr);
        // END
        if (port0 != null) 
            doRun(port0);
        // OTHERWISE SHOULD LOG UNTIMELY DEATH
    }
    protected void doRun(SchUdpPort port0) {

        // Create lists of neighbors and their IP addresses.  Eligible
        // peers have a udp-xl-NattedAddress or -IPAddress overlay that
        // we should be able to talk to.
        ArrayList neighbors = new ArrayList();
        ArrayList addrs     = new ArrayList();
        for (int i = 0; i < peers.size(); i++) {
            Peer peer = (Peer)peers.get(i);
            for (int n = 0; n < peer.sizeOverlays(); n++) {
                Overlay o = peer.getOverlay(n);
                if (o.transport().equals("udp") && o.protocol().equals("xl")) {
                    Address a = o.address();
                    IPAddress ipAddr = null;
                    if (a instanceof IPAddress)
                        ipAddr = (IPAddress) a;
                    else if (a instanceof NattedAddress)
                        ipAddr = new IPAddress(
                        ((NattedAddress)a).getLocalHost(),
                        ((NattedAddress)a).getLocalPort() );
                    if (ipAddr != null) {
                        neighbors.add(peer);
                        addrs.add(ipAddr);
                        // DEBUG
                        System.out.println("  peer " 
                                + peer.getNodeID().toString().substring(0, 8) 
                                + "...  " + ipAddr);
                        // END
                        break;
                    }
                }
            }
        }
        Peer[] goodNeighbors    = new Peer[ neighbors.size() ];
        IPAddress[] goodAddrs   = new IPAddress[ neighbors.size() ];
        for (int i = 0; i < neighbors.size(); i++) {
            goodNeighbors[i] = (Peer) neighbors.get(i);
            goodAddrs[i]     = (IPAddress) addrs.get(i);
        }

        // create handler /////////////////////////////////       
        p0Handler = new Port0Handler( this, goodNeighbors, goodAddrs, 
                                                            D , J, Z);
        p0Handler.setTimeout(timeout);

        port0.setListener(p0Handler);
        scheduler.add(port0); 
        Thread handlerThread = new Thread( p0Handler );
        handlerThread.start();
        running  = true;
        
        /* STUB */
        
        try { myThread.sleep(1000); } catch (InterruptedException ie) {}
    }
    public Thread getThread() {
        return myThread;
    }
    // XXX synchronized?
    public boolean isRunning() {
        return running;
    }

    /**
     * Stops the node from running.  
     *
     * XXX This is incomplete.  Should close any open connections,
     * XXX release all assets in use.
     */
    public void close() {
        running = false;
        timer.cancel();         // any running task unaffected
        // DEBUG
        System.out.println("TNode.close() "  + port0Addr);
        // END
        scheduler.close();      // blocks

        // CLOSE ALL PORTS //////////////////////
        if (port0 != null)
            try {
                port0.close();
            } catch (IOException ioe) {}
        /* STUB */
    }
    
    // EQUALS, HASHCODE /////////////////////////////////////////////
    // use super.equals() and super.hashCode()
}

