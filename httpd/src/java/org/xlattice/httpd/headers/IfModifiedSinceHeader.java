/* IfModifiedSinceHeader.java */
package org.xlattice.httpd.headers;

import org.xlattice.httpd.RequestHeader;
import org.xlattice.httpd.MalformedHeaderException;

/**
 *
 *
 * @author Jim Dixon
 */
public class IfModifiedSinceHeader extends AbstractDate
                                   implements RequestHeader {

    public IfModifiedSinceHeader (String definition) 
                                        throws MalformedHeaderException {
        super(definition);
    }
    // XXX NEED (Date) or (HttpDate) constructor

    // INTERFACE Header /////////////////////////////////////////////
    public String getTag() {
        return "If-Modified-Since";
    }
}
