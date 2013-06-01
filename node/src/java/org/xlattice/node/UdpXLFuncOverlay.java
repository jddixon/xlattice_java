/* UdpXLFuncOverlay.java */
package org.xlattice.node;

import java.util.Timer;
import java.util.TimerTask;

import org.xlattice.Address;
import org.xlattice.Overlay;
import org.xlattice.NodeID;
import org.xlattice.Protocol;
import org.xlattice.protocol.stun.NattedAddress;
import org.xlattice.protocol.xl.*;

/**
 * XXX TNode argument to constructor is a nonsense.  This gets passed
 * XXX to the TNode construtor!
 * 
 * XXX Capitalization is inconsistent; should be UdpXl.
 */
public class UdpXLFuncOverlay extends NattedOverlay implements XLConst {
    protected static final XL xl = new XL();   // yes, ridiculous
    
    // INSTANCE VARIABLES ///////////////////////////////////////////
    protected final TNode  myNode;
    protected final NodeID myNodeID;
    protected final Timer  timer;
    
    // CONSTRUCTORS /////////////////////////////////////////////////
    public UdpXLFuncOverlay (TNode t, NattedAddress a) {
        super(xl, a);
        if (t == null)
            throw new IllegalArgumentException("null parent TNode");
        myNode   = t;
        myNodeID = t.getNodeID();       // convenience
        timer    = t.getTimer();        // likewise
    }
    // INTERFACE XLFunc /////////////////////////////////////////////
    public void ping( NodeID target ) {
        if (target == null)
            throw new IllegalArgumentException("null ping target");
        XLMsg msg = new Ping();
        msg.add( new Source(myNodeID)    );
        msg.add( new Destination(target) );
        
        // get msg length and encode into buffer
        // schedule a send with the IOScheduler
        // schedule a timeout, which needs to know how to report the event
        // need to have database of outstanding pings, with EITHER the 
        //   pong OR the timeout removing the entry; should collect ping
        //   time
    }
    /**
     * Pongs could be handled at a lower level, since they can be
     * completely stateless - unless we learn NodeIDs from incoming
     * pings, adding them to the Peer database.
     * 
     * XXX Need a TTL. 
     * XXX Copying the msgID over and over again is silly.
     */
    public void pong( NodeID target, byte[] msgID ) {
        // POSSIBLY add the sender to our Peer database
        // construct a packet as above
        // schedule a send with the IOScheduler
    }
}
