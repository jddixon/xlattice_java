/* ContentLocationHeader.java */
package org.xlattice.httpd.headers;

import org.xlattice.httpd.Header;
import org.xlattice.httpd.EntityHeader;
import org.xlattice.httpd.MalformedHeaderException;

/**
 *
 * @author Jim Dixon
 */
public class ContentLocationHeader extends AbstractURI
                            implements EntityHeader {
    
    public ContentLocationHeader (String serverName)     
                                    throws MalformedHeaderException {
        super(serverName);
    }
    // INTERFACE Header, AbstractURI ////////////////////////////////
    public String getTag () {
        return "Content-Location";
    }
    // OTHER METHODS ////////////////////////////////////////////////
}
