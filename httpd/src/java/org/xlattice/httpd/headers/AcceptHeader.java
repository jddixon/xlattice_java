/* AcceptHeader.java */
package org.xlattice.httpd.headers;

import org.xlattice.httpd.Header;
import org.xlattice.httpd.RequestHeader;
import org.xlattice.httpd.MalformedHeaderException;

/**
 * XXX No attempt is made to interpret the sub-syntax.
 *
 * @author Jim Dixon
 */
public class AcceptHeader       extends AbstractText 
                                implements RequestHeader {
    
    public AcceptHeader (String mediaTypes)     
                                    throws MalformedHeaderException {
        super(mediaTypes);
    }
    // INTERFACE Header, AbstractText ///////////////////////////////
    public String getTag () {
        return "Accept";
    }
    // OTHER METHODS ////////////////////////////////////////////////
}
