/* IllegalParserStateException.java */
package org.xlattice.httpd;

/**
 * @author Jim Dixon
 */

public class IllegalParserStateException extends HttpException {
    /** No-argument constructor. */
    public IllegalParserStateException () {
        super();
    }
    /** Constructor taking a single String argument. */
    public IllegalParserStateException (String msg) {
        super(msg);
    }
}

