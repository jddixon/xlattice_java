/* AbstractInt.java */
package org.xlattice.httpd.headers;

import org.xlattice.httpd.Header;
import org.xlattice.httpd.MalformedHeaderException;

/**
 *
 * @author Jim Dixon
 */
public abstract class AbstractInt extends Header {

    protected final int value;

    /**
     * Subclasses may need to do further checks on value.
     */
    public AbstractInt (String definition) throws MalformedHeaderException {
        if (definition == null || definition.equals(""))
            throw new MalformedHeaderException(
                    "null or empty header field value");
        // XXX THIS IS WRONG, SHOULD BE CONVERTING INT
        try {
            value = Integer.parseInt(definition);
        } catch (NumberFormatException nfe) {
            throw new MalformedHeaderException(
                    "number format error: " + nfe);
        }
    }
    public AbstractInt (int i) {
        value = i;
    }
    // INTERFACE Header /////////////////////////////////////////////
    public String toString() {
        return new StringBuffer(getTag())
            .append(": ")
            .append(value)
            .append("\n")
            .toString();
    }
    // OTHER METHODS ////////////////////////////////////////////////
    public int getValue() {
        return value;
    }
}
