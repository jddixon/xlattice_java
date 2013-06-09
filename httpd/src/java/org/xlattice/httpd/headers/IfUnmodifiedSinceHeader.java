/* IfUnmodifiedSinceHeader.java */
package org.xlattice.httpd.headers;

import org.xlattice.httpd.RequestHeader;
import org.xlattice.httpd.MalformedHeaderException;

/**
 *
 * @author Jim Dixon
 */
public class IfUnmodifiedSinceHeader extends AbstractDate
                                     implements RequestHeader {

    public IfUnmodifiedSinceHeader (String definition) 
                                        throws MalformedHeaderException {
        super(definition);
    }
    // XXX NEED (Date) or (HttpDate) constructor

    // INTERFACE Header /////////////////////////////////////////////
    public String getTag() {
        return "If-Unmodified-Since";
    }
}
