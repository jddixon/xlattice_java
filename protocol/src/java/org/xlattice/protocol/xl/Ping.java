/* Ping.java */
package org.xlattice.protocol.xl;

/**
 * This message must have destination and source attributes, and
 * somewhere a TTL field.
 */
public class Ping extends XLMsg {

    public Ping () {
        super (PING);
    }
    public Ping (byte[] msgID) {
        super (PING, msgID);
    }
}
