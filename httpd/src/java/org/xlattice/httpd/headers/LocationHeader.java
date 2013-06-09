/* LocationHeader.java */
package org.xlattice.httpd.headers;

import org.xlattice.httpd.Header;
import org.xlattice.httpd.ResponseHeader;
import org.xlattice.httpd.MalformedHeaderException;

/**
 * Should be a single absolute URI
 * but we don't check this.
 *
 * @author Jim Dixon
 */
public class LocationHeader extends AbstractURI
                            implements ResponseHeader {
    
    public LocationHeader (String serverName)     
                                    throws MalformedHeaderException {
        super(serverName);
    }
    // INTERFACE Header, AbstractURI ////////////////////////////////
    public String getTag () {
        return "Location";
    }
    // OTHER METHODS ////////////////////////////////////////////////
    public boolean relativePermitted() {
        return false;
    }
}
