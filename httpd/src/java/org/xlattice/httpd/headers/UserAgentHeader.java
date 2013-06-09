/* UserAgentHeader.java */
package org.xlattice.httpd.headers;

import org.xlattice.httpd.Header;
import org.xlattice.httpd.RequestHeader;
import org.xlattice.httpd.MalformedHeaderException;

/**
 *
 * @author Jim Dixon
 */
public class UserAgentHeader    extends AbstractText 
                                implements RequestHeader {
    
    public UserAgentHeader (String agentName)     
                                    throws MalformedHeaderException {
        super(agentName);
    }
    // INTERFACE Header, AbstractText ///////////////////////////////
    public String getTag () {
        return "User-Agent";
    }
    // OTHER METHODS ////////////////////////////////////////////////
}
