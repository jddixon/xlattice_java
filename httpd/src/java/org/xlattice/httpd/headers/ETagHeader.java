/* ETagHeader.java */
package org.xlattice.httpd.headers;

import org.xlattice.httpd.Header;
import org.xlattice.httpd.ResponseHeader;
import org.xlattice.httpd.MalformedHeaderException;

/**
 *
 * @author Jim Dixon
 */
public class ETagHeader     extends AbstractText 
                            implements ResponseHeader {
    
    public ETagHeader (String entityTag)     
                                    throws MalformedHeaderException {
        super(entityTag);
    }
    // INTERFACE Header, AbstractText ///////////////////////////////
    public String getTag () {
        return "ETag";
    }
    // OTHER METHODS ////////////////////////////////////////////////
}
