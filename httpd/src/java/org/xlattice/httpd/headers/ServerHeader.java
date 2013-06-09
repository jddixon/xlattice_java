/* ServerHeader.java */
package org.xlattice.httpd.headers;

import org.xlattice.httpd.Header;
import org.xlattice.httpd.ResponseHeader;
import org.xlattice.httpd.MalformedHeaderException;

/**
 * The server name should follow the 'product' form (name/version)
 * but we don't check this.
 *
 * @author Jim Dixon
 */
public class ServerHeader   extends AbstractText 
                            implements ResponseHeader {
    
    public ServerHeader (String serverName)     
                                    throws MalformedHeaderException {
        super(serverName);
    }
    // INTERFACE Header, AbstractText ///////////////////////////////
    public String getTag () {
        return "Server";
    }
    // OTHER METHODS ////////////////////////////////////////////////
}
