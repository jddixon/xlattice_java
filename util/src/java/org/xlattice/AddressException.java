/* AddressException.java */
package org.xlattice;

/** 
 *
 * @author Jim Dixon
 */
public class AddressException extends Exception {
    /** No-argument constructor. */
    public AddressException () {
        super();
    }
    /** Constructor taking a single String argument. */
    public AddressException (String msg) {
        super(msg);
    }
}
