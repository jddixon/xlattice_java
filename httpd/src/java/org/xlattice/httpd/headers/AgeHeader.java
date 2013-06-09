/* AgeHeader.java */
package org.xlattice.httpd.headers;

import org.xlattice.httpd.Header;
import org.xlattice.httpd.ResponseHeader;
import org.xlattice.httpd.MalformedHeaderException;

/**
 * @author Jim Dixon
 */

public class AgeHeader          extends AbstractInt 
                                implements ResponseHeader {

    public AgeHeader (String def) 
                                throws MalformedHeaderException {
        super(def);
        if (value < 0)
            throw new MalformedHeaderException ("negative delta-seconds");
    }
    public AgeHeader (int i) 
                                throws MalformedHeaderException {
        super(i);
        if (value < 0)
            throw new MalformedHeaderException ("negative delta-seconds");
    }
    // INTERFACE Header /////////////////////////////////////////////
    public String getTag() {
        return "Age";
    }
}
