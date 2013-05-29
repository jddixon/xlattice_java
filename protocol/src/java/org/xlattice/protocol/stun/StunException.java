/* StunException.java */
package org.xlattice.protocol.stun;

/**
 * @author Jim Dixon
 **/

public class StunException extends Exception {
    /** No-argument constructor. */
    public StunException () {
        super();
    }
    /** Constructor taking a single String argument. */
    public StunException (String msg) {
        super(msg);
    }
}
