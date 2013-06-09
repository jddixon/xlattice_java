/* WarningHeader.java */
package org.xlattice.httpd.headers;

import org.xlattice.httpd.Header;
import org.xlattice.httpd.GeneralHeader;
import org.xlattice.httpd.MalformedHeaderException;

/**
 * XXX There is a rather complicated syntax to the warnings, 
 * XXX ignored here.
 *
 * @author Jim Dixon
 */
public class WarningHeader  extends AbstractText 
                            implements GeneralHeader {
    
    public WarningHeader (String warningValue)     
                                    throws MalformedHeaderException {
        super(warningValue);
    }
    // INTERFACE Header, AbstractText ///////////////////////////////
    public String getTag () {
        return "Warning";
    }
    // OTHER METHODS ////////////////////////////////////////////////
}
