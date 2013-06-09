/* HttpException.java */
package org.xlattice.httpd;

/**
 * @author Jim Dixon
 */

public class HttpException extends Exception {
    /** No-argument constructor. */
    public HttpException () {
        super();
    }
    /** Constructor taking a single String argument. */
    public HttpException (String msg) {
        super(msg);
    }
}

