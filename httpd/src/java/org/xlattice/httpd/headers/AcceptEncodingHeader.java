/* AcceptEncodingHeader.java */
package org.xlattice.httpd.headers;

import org.xlattice.httpd.Header;
import org.xlattice.httpd.RequestHeader;
import org.xlattice.httpd.MalformedHeaderException;

/**
 * XXX The simple sub-syntax has not been implemented.
 *
 * @author Jim Dixon
 */
public class AcceptEncodingHeader   extends AbstractText 
                                    implements RequestHeader {
    
    public AcceptEncodingHeader (String codings)     
                                    throws MalformedHeaderException {
        super(codings);
    }
    // INTERFACE Header, AbstractText ///////////////////////////////
    public String getTag () {
        return "Accept-Encoding";
    }
    // OTHER METHODS ////////////////////////////////////////////////
}
