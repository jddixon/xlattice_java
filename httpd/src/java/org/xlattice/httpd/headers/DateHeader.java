/* DateHeader.java */
package org.xlattice.httpd.headers;

import org.xlattice.httpd.HttpDate;
import org.xlattice.httpd.GeneralHeader;
import org.xlattice.httpd.MalformedHeaderException;

/**
 *
 *
 * @author Jim Dixon
 */
public class DateHeader extends AbstractDate
                                        implements GeneralHeader {

    public DateHeader (String definition) 
                                        throws MalformedHeaderException {
        super(definition);
    }
    public DateHeader (HttpDate when) {
        super (when);
    }
    public DateHeader () {
        super ( new HttpDate() );
    }
    // INTERFACE Header /////////////////////////////////////////////
    public String getTag() {
        return "Date";
    }
}
