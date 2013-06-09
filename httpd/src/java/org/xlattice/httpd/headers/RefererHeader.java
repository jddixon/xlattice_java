/* RefererHeader.java */
package org.xlattice.httpd.headers;

import org.xlattice.httpd.Header;
import org.xlattice.httpd.RequestHeader;
import org.xlattice.httpd.MalformedHeaderException;

/**
 *
 * @author Jim Dixon
 */
public class RefererHeader  extends AbstractURI
                            implements RequestHeader {
    
    public RefererHeader (String referrer)     
                                    throws MalformedHeaderException {
        super(referrer);
    }
    // INTERFACE Header, AbstractURI ////////////////////////////////
    public String getTag () {
        return "Referer";
    }
    // OTHER METHODS ////////////////////////////////////////////////
}
