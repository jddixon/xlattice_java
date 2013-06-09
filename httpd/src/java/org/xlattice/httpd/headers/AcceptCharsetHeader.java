/* AcceptCharsetHeader.java */
package org.xlattice.httpd.headers;

import org.xlattice.httpd.Header;
import org.xlattice.httpd.RequestHeader;
import org.xlattice.httpd.MalformedHeaderException;

/**
 * XXX No attempt is made to interpret the sub-syntax.
 *
 * @author Jim Dixon
 */
public class AcceptCharsetHeader    extends AbstractText 
                                    implements RequestHeader {
    
    public AcceptCharsetHeader (String charsets)     
                                    throws MalformedHeaderException {
        super(charsets);
    }
    // INTERFACE Header, AbstractText ///////////////////////////////
    public String getTag () {
        return "Accept-Charset";
    }
    // OTHER METHODS ////////////////////////////////////////////////
}
