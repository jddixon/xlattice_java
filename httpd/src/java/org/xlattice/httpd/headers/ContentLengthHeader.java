/* ContentLengthHeader.java */
package org.xlattice.httpd.headers;

import org.xlattice.httpd.Header;
import org.xlattice.httpd.EntityHeader;
import org.xlattice.httpd.MalformedHeaderException;

/**
 * @author Jim Dixon
 */

public class ContentLengthHeader extends AbstractInt 
                                 implements EntityHeader {

    public ContentLengthHeader (String def) 
                                throws MalformedHeaderException {
        super(def);
        if (value < 0)
            throw new MalformedHeaderException ("negative length");
    }
    public ContentLengthHeader (int i) 
                                throws MalformedHeaderException {
        super(i);
        if (value < 0)
            throw new MalformedHeaderException ("negative length");
    }
    // INTERFACE Header /////////////////////////////////////////////
    public String getTag() {
        return "Content-Length";
    }
}
