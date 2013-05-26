/* CryptoException.java */
package org.xlattice;

/**
 * @author Jim Dixon
 **/

public class CryptoException extends Exception {
    /** No-argument constructor. */
    public CryptoException () {
        super();
    }
    /** Constructor taking a single String argument. */
    public CryptoException (String msg) {
        super(msg);
    }
}
