/* MaxForwardsHeader.java */
package org.xlattice.httpd.headers;

import org.xlattice.httpd.Header;
import org.xlattice.httpd.RequestHeader;
import org.xlattice.httpd.MalformedHeaderException;

/**
 * @author Jim Dixon
 */

public class MaxForwardsHeader          extends AbstractInt 
                                        implements RequestHeader {

    public MaxForwardsHeader (String def) 
                                throws MalformedHeaderException {
        super(def);
        if (value < 0)
            throw new MalformedHeaderException ("negative delta-seconds");
    }
    public MaxForwardsHeader (int i) 
                                throws MalformedHeaderException {
        super(i);
        if (value < 0)
            throw new MalformedHeaderException ("negative delta-seconds");
    }
    // INTERFACE Header /////////////////////////////////////////////
    public String getTag() {
        return "Max-Forwards";
    }
}
