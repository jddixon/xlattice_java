/* PragmaHeader.java */
package org.xlattice.httpd.headers;

import org.xlattice.httpd.Header;
import org.xlattice.httpd.GeneralHeader;
import org.xlattice.httpd.MalformedHeaderException;

/**
 * 
 *
 * @author Jim Dixon
 */
public class PragmaHeader   extends AbstractText 
                            implements GeneralHeader {
    
    public PragmaHeader (String serverName)     
                                    throws MalformedHeaderException {
        super(serverName);
    }
    // INTERFACE Header, AbstractText ///////////////////////////////
    public String getTag () {
        return "Pragma";
    }
    // OTHER METHODS ////////////////////////////////////////////////
}
