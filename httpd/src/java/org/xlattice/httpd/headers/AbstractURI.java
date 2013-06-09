/* AbstractURI.java */
package org.xlattice.httpd.headers;

import org.xlattice.httpd.Header;
import org.xlattice.httpd.MalformedHeaderException;

/**
 * Superclass of HTTP headers whose field value is an absolute
 * or relative URI.
 *
 * @author Jim Dixon
 */
public abstract class AbstractURI extends Header {

    private final String loc;

    /**
     * Better syntax checking might be a good idea.
     *
     * XXX However at least one standard browser (Win98 MS IE) produces
     * XXX Referer headers with no value, just "Referer: ".
     */
    public AbstractURI (String location) 
                                    throws MalformedHeaderException {
        if (location == null || location.equals(""))
            throw new MalformedHeaderException("null or empty URI");
        loc = location;
    }
    // INTERFACE Header /////////////////////////////////////////////
    public String toString() {
        return new StringBuffer(getTag())
            .append(": ")
            .append(loc)
            .append("\n")
            .toString();
    }
    // OTHER METHODS ////////////////////////////////////////////////
    /**
     * Subclasses should override if relative URIs are *not* permitted.
     */
    public boolean relativePermitted () {
        return true;
    }
    public String getURI() {
        return loc;
    }
}
