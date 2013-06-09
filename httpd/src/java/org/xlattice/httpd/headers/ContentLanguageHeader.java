/* ContentLanguageHeader.java */
package org.xlattice.httpd.headers;

import org.xlattice.httpd.Header;
import org.xlattice.httpd.EntityHeader;
import org.xlattice.httpd.MalformedHeaderException;

/**
 * XXX No attempt is made to interpret the sub-syntax.
 *
 * @author Jim Dixon
 */
public class ContentLanguageHeader  extends AbstractText 
                                    implements EntityHeader {
    
    public ContentLanguageHeader (String languages)     
                                    throws MalformedHeaderException {
        super(languages);
    }
    // INTERFACE Header, AbstractText ///////////////////////////////
    public String getTag () {
        return "Content-Language";
    }
    // OTHER METHODS ////////////////////////////////////////////////
}
