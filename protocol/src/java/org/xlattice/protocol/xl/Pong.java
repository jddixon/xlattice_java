/* Pong.java */
package org.xlattice.protocol.xl;

/**
 * This message must have destination and source attributes, and
 * somewhere a TTL field.
 */
public class Pong extends XLMsg {

    public Pong () {
        super (PONG);
    }
    public Pong (byte[] msgID) {
        super (PONG, msgID);
    }
}
