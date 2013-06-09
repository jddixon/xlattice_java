/* NotImplementedException.java */
package org.xlattice.httpd;

/**
 * XXX This should probably be replaced by an org.xlattice class
 * XXX of the same name.
 *
 * @author Jim Dixon
 */
public class NotImplementedException        extends HttpException {
    /** No-argument constructor. */
    public NotImplementedException () {
        super();
    }
    /** Constructor taking a single String argument. */
    public NotImplementedException (String msg) {
        super(msg);
    }
}

