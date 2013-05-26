/* Timestamp.java */
package org.xlattice.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Convenience class handling YYYY-MM-DD HH:MM:SS formatted dates.
 *
 * @author Jim Dixon
 */
public class Timestamp extends Date {

    public final static SimpleDateFormat DATE_FMT 
                        = new SimpleDateFormat ( "yyyy-MM-dd HH:mm:ss");

    public Timestamp () {
        super();
    }

    public Timestamp (String s)             throws ParseException {
        super ( DATE_FMT.parse (s.trim()).getTime() );      
    }


    public String toString () {
        return DATE_FMT.format (this);
    }
}
