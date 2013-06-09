/* ExpectHeader.java */
package org.xlattice.httpd.headers;

import org.xlattice.httpd.Header;
import org.xlattice.httpd.RequestHeader;
import org.xlattice.httpd.MalformedHeaderException;

/**
 * The sub-syntax is not supported.
 *
 * @author Jim Dixon
 */
public class ExpectHeader   extends AbstractText 
                            implements RequestHeader {
    
    public ExpectHeader (String expectation)     
                                    throws MalformedHeaderException {
        super(expectation);
    }
    // INTERFACE Header, AbstractText ///////////////////////////////
    public String getTag () {
        return "Expect";
    }
    // OTHER METHODS ////////////////////////////////////////////////
}
