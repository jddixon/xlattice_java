/* AbstractText.java */
package org.xlattice.httpd.headers;

import org.xlattice.httpd.Header;
import org.xlattice.httpd.MalformedHeaderException;

/**
 * Superclass of HTTP headers whose field value is a collection 
 * of characters other than delimiters.  Subclasses need only 
 * call this class's constructor and implement getTag().
 *
 * @author Jim Dixon
 */
public abstract class AbstractText extends Header {

    private final String def;

    public AbstractText (String definition) 
                                    throws MalformedHeaderException {
        if (definition == null || definition.equals(""))
            throw new MalformedHeaderException(
                    "null or empty header field value");
        def = definition;
    }
    // INTERFACE Header /////////////////////////////////////////////
    public String toString() {
        return new StringBuffer(getTag())
            .append(": ")
            .append(def)
            .append("\n")
            .toString();
    }
    // OTHER METHODS ////////////////////////////////////////////////
    public String getValue() {
        return def;
    }
}
