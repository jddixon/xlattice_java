/* MessageFormatException.java */
package org.xlattice.httpd;

/**
 * @author Jim Dixon
 */

public class MessageFormatException extends HttpException {
    /** No-argument constructor. */
    public MessageFormatException () {
        super();
    }
    /** Constructor taking a single String argument. */
    public MessageFormatException (String msg) {
        super(msg);
    }
}

