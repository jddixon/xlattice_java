/* ExtensionHeader.java */
package org.xlattice.httpd.headers;

import org.xlattice.httpd.Header;
import org.xlattice.httpd.EntityHeader;
import org.xlattice.httpd.MalformedHeaderException;

/**
 *
 * @author Jim Dixon
 */
public class ExtensionHeader    extends AbstractText 
                                implements EntityHeader {
    private String tag;
    
    public ExtensionHeader (String tag, String value)     
                                    throws MalformedHeaderException {
        super(value);
        if (tag == null || tag.equals(""))
            throw new IllegalArgumentException("null or empty tag");
        this.tag = tag;
    }
    // INTERFACE Header, AbstractText ///////////////////////////////
    public String getTag () {
        return tag;
    }
    // OTHER METHODS ////////////////////////////////////////////////
}
