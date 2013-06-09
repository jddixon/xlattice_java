/* ConnectionHeader.java */
package org.xlattice.httpd.headers;

import org.xlattice.httpd.Header;
import org.xlattice.httpd.GeneralHeader;
import org.xlattice.httpd.MalformedHeaderException;

/**
 * Common options seem to be 'close' and 'keep-alive'.
 *
 * @author Jim Dixon
 */
public class ConnectionHeader   extends AbstractText 
                                implements GeneralHeader {
    
    public ConnectionHeader (String option)     
                                    throws MalformedHeaderException {
        super(option);
    }
    // INTERFACE Header, AbstractText ///////////////////////////////
    public String getTag () {
        return "Connection";
    }
    // OTHER METHODS ////////////////////////////////////////////////
}
