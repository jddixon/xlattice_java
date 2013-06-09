/* HttpDate.java */
package org.xlattice.httpd;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

/**
 * Convenience class handling HTTP protocol dates.  These are always
 * in GMT.  The output format must follow the form 
 *    EEE, dd MMM yyyy HH:mm:ss GMT
 * as for example
 *    Tue, 15 Nov 1994 08:12:31 GMT
 * 
 * XXX On input two other formats should be accepted:
 * XXX   ANSI C asctime(), Sun Nov  6 08:49:37 1994
 * XXX   the obsolete RFC 850 form, Sunday, 06-Nov-94 08:49:37 GMT
 * XXX However, these are not yet implemented.
 *
 * This class is NOT thread-safe.  In a multi-threaded environment access
 * to SimpleDateFormat instances must be synchronized.
 *
 * @author Jim Dixon
 */
public class HttpDate extends Date {

    // this is the format used to PARSE input
    public final static SimpleDateFormat httpDateFormat 
                        = new SimpleDateFormat ( 
                                "EEE, dd MMM yyyy HH:mm:ss zzz");

    // this is the format used to format output
    public final static SimpleDateFormat GMTFormat 
                        = new SimpleDateFormat ( 
                                "EEE, dd MMM yyyy HH:mm:ss zzz");
    static {
        GMTFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }
    public HttpDate () {
        super();
    }
    public HttpDate (Date when) {
        super(when.getTime());
    }
    public HttpDate (String s)             throws ParseException {
        super ( httpDateFormat.parse (s.trim()).getTime() );      
    }
    public String toString () {
        return GMTFormat.format (this);
    }
}
