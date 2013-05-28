/* StreamConnection.java */
package org.xlattice.transport;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.xlattice.Address;
import org.xlattice.Connection;
import org.xlattice.EndPoint;
import org.xlattice.Transport;

/**
 * A bidirectional Stream connection.
 */

public abstract class StreamConnection      implements Connection {

    protected final Transport    transport;
    protected       Address      nearAddr;
    protected       Address      farAddr;
    
    protected final boolean      blocking;
    protected       boolean      closed;
    protected       InputStream  ins;
    protected       OutputStream outs;
    protected       int          state;
    protected       int          timeout;
    
    /**
     * Creates the bidirectional buffered connection.
     *
     * XXX Connection is defined in terms of EndPoints.
     */
    protected StreamConnection (Transport transport, 
                    Address near, Address far, boolean blocking) {
        if (transport == null)
            throw new IllegalArgumentException("null transport");
        this.transport = transport;
        nearAddr = near;
        farAddr  = far;
        this.blocking = blocking;
    }

    // INTERFACE Connection /////////////////////////////////////////
    public boolean isClosed() {
        return closed;
    }
    public EndPoint getFarEnd() {
        return new EndPoint (transport, farAddr);
    }
    public EndPoint getNearEnd() {
        return new EndPoint (transport, nearAddr);
    }
    public int getState() {
        return state;
    }
    public boolean isBlocking() {
        return blocking;
    }
    // PROPERTIES ///////////////////////////////////////////////////
    /** @return the timeout on the connection in ms */
    public int getTimeout() {
        return timeout;
    }

    /** @param ms  the timeout on the connection */
    public void setTimeout (int ms) {
        if (ms < 0)
            throw new IllegalArgumentException(
                    "negative timeout: "+ ms);
        timeout = ms;
    }
}
