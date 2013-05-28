/* PacketPort.java */
package org.xlattice.transport;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.xlattice.Address;

/**
 * 
 */

public abstract class PacketPort  {

    // STATICS //////////////////////////////////////////////////////
    /** unopened */
    public final static int UNBOUND   = 100;
    /** open, local address bound */
    public final static int BOUND     = 200;
    /** far end bound */
    public final static int CONNECTED = 400;
    /** closed, cannot be reopened */
    public final static int CLOSED    = 600;
    
    public final String[] STATES = {
        "UNBOUND", "BOUND", "CONNECTED", "CLOSED" };
    public final String stateToString(int n) {
        switch (n) {
            case UNBOUND:   return "UNBOUND";
            case BOUND:     return "BOUND";
            case CONNECTED: return "CONNECTED";
            case CLOSED:    return "CLOSED";
            default:    
                return "unrecogized state " + n;
        }
    }

    // INSTANCE VARIABLES ///////////////////////////////////////////
    protected       Address nearAddr;
    protected       Address farAddr;

    protected int state = UNBOUND;
    protected int timeout;      // ms, <= 0 construed as infinite

    // CONSTRUCTORS /////////////////////////////////////////////////
    protected PacketPort () {
    }
    
    /**
     * Open the port and bind the local address.  
     */
    protected PacketPort ( Address a ) {
        if (a == null) 
            throw new IllegalArgumentException ("null local address");
        nearAddr = a;
        setState(BOUND);
    }
    // METHODS ////////////////////////////////////////////
    /**
     * Change state if transition is permitted.  In this implementation
     * the only downward change permitted is CONNECTED to DISCONNECTED.
     * 
     * Subclasses should tolerate closing closed connections.
     */
    protected void setState (int newState) {
        switch (state) {
            case UNBOUND:
                if (newState != BOUND)
                    badNewState ("can't go from UNBOUND to ", newState);
                break;
            case BOUND:
                if (newState != CONNECTED && newState != CLOSED)
                    badNewState ("can't go from BOUND to ", newState);
                break;
            case CONNECTED:
                if (newState != BOUND && newState != CLOSED)
                    badNewState ("can't go from CONNECTED to ", newState);
                break;
            case CLOSED:
                if (newState != CLOSED)
                    badNewState("can't go from CLOSED to ", newState);
                break;
            default:
                throw new IllegalStateException(
                        "INTERNAL ERROR: unrecognized state " + state);
        }
        state = newState;
    }
    protected void bindNearEnd (Address a)      throws IOException {
        if (a == null)
            throw new IllegalArgumentException("null far address");
        setState(BOUND);
        // at this point we know the action is legal
        nearAddr = a;
    }
        
    protected void bindFarEnd (Address a)       throws IOException {
        if (a == null)
            throw new IllegalArgumentException("null far address");
        setState(CONNECTED);
        // at this point we know the action is legal
        farAddr = a;
    }
    protected void unbindFarEnd()               throws IOException {
        setState(BOUND);
        // transition is legal 
        farAddr = null;
    }
    protected void close ()                     throws IOException {
        setState(CLOSED);
    }
    protected boolean isClosed() {
        return state == CLOSED;
    }
    // OTHER PROPERTIES /////////////////////////////////////////////
    public Address getNearAddress() {
        return nearAddr;
    }
    public Address getFarAddress() {
        return farAddr;
    }
    public int getTimeout() {
        return timeout;
    }
    /**
     * Subclasses should override to do whatever is necessary, 
     * calling this method first.
     */
    protected void setTimeout (int ms)          throws IOException {
        if (ms < 0)
            ms = 0;
        timeout = ms;
    }
    
    // OTHER METHODS ////////////////////////////////////////////////
    protected final void badNewState(String msg, int newState) {
        throw new IllegalStateException ( 
                msg + stateToString(newState));
    }
    // EQUALS/HASHCODE //////////////////////////////////////////////
    // SERIALIZATION ////////////////////////////////////////////////
    
}
