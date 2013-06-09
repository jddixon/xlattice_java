/* MalformedHeaderException.java */
package org.xlattice.httpd;

/**
 * @author Jim Dixon
 */

public class MalformedHeaderException extends MessageFormatException {
    /** No-argument constructor. */
    public MalformedHeaderException () {
        super();
    }
    /** Constructor taking a single String argument. */
    public MalformedHeaderException (String msg) {
        super(msg);
    }
}

