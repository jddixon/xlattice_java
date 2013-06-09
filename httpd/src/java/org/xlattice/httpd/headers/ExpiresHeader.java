/* ExpiresHeader.java */
package org.xlattice.httpd.headers;

import org.xlattice.httpd.EntityHeader;
import org.xlattice.httpd.MalformedHeaderException;

/**
 *
 *
 * @author Jim Dixon
 */
public class ExpiresHeader extends AbstractDate
                                        implements EntityHeader {

    public ExpiresHeader (String definition) 
                                        throws MalformedHeaderException {
        super(definition);
    }
    // XXX NEED (Date) or (HttpDate) constructor

    // INTERFACE Header /////////////////////////////////////////////
    public String getTag() {
        return "Expires";
    }
}
