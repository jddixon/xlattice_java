/* RetryAfterHeader.java */
package org.xlattice.httpd.headers;

import org.xlattice.httpd.ResponseHeader;
import org.xlattice.httpd.MalformedHeaderException;

/**
 * XXX This should accept either an HttpDate or an integer
 * XXX representing a number of seconds from the time of the
 * XXX response.
 *
 * @author Jim Dixon
 */
public class RetryAfterHeader           extends AbstractDate
                                        implements ResponseHeader {

    public RetryAfterHeader (String definition) 
                                        throws MalformedHeaderException {
        super(definition);
    }
    // XXX NEED (Date) or (HttpDate) constructor

    // INTERFACE Header /////////////////////////////////////////////
    public String getTag() {
        return "Retry-After";
    }
}
