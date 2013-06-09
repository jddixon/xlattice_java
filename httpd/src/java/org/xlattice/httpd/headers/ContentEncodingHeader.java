/* ContentEncodingHeader.java */
package org.xlattice.httpd.headers;

import org.xlattice.httpd.Header;
import org.xlattice.httpd.EntityHeader;
import org.xlattice.httpd.MalformedHeaderException;

/**
 * XXX No attempt is made to interpret the sub-syntax.
 *
 * @author Jim Dixon
 */
public class ContentEncodingHeader  extends AbstractText 
                                    implements EntityHeader {
    
    public ContentEncodingHeader (String mediaTypes)     
                                    throws MalformedHeaderException {
        super(mediaTypes);
    }
    // INTERFACE Header, AbstractText ///////////////////////////////
    public String getTag () {
        return "Content-Encoding";
    }
    // OTHER METHODS ////////////////////////////////////////////////
}
