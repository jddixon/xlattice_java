/* FromHeader.java */
package org.xlattice.httpd.headers;

import org.xlattice.httpd.Header;
import org.xlattice.httpd.RequestHeader;
import org.xlattice.httpd.MalformedHeaderException;

/**
 * Email address syntax is not checked.
 *
 * @author Jim Dixon
 */
public class FromHeader     extends AbstractText 
                            implements RequestHeader {
    
    public FromHeader (String emailAddr)     
                                    throws MalformedHeaderException {
        super(emailAddr);
    }
    // INTERFACE Header, AbstractText ///////////////////////////////
    public String getTag () {
        return "From";
    }
    // OTHER METHODS ////////////////////////////////////////////////
}
