/* AbstractDate.java */
package org.xlattice.httpd.headers;

import java.util.Date;

/**
 * @author Jim Dixon
 */

import org.xlattice.httpd.Header;
import org.xlattice.httpd.HttpDate;
import org.xlattice.httpd.MalformedHeaderException;

/**
 */
public abstract class AbstractDate extends Header {

    protected final HttpDate time;

    public AbstractDate (String definition) throws MalformedHeaderException {
        if (definition == null || definition.equals(""))
            throw new MalformedHeaderException(
                    "null or empty header field value");
        try {
            time = new HttpDate(definition);
        } catch (java.text.ParseException pe) {
            throw new MalformedHeaderException(
                    "date format error: " + pe);
        }
    }
    public AbstractDate (HttpDate when) {
        time = when;
    }
    // INTERFACE Header /////////////////////////////////////////////
    public String toString() {
        return new StringBuffer(getTag())
            .append(": ")
            .append(time)
            .append("\n")
            .toString();
    }
}
