/* TestTimestamp.java */
package org.xlattice.util;

import java.util.Date;

/**
 * @author Jim Dixon
 **/

import junit.framework.*;

public class TestTimestamp extends TestCase {

    public TestTimestamp (String name) {
        super(name);
    }

    public void setUp () {
    }
    public void testAFew()                      throws Exception {
        Timestamp now   = new Timestamp();
        String encoded  = now.toString();
        Timestamp now2  = new Timestamp(encoded);
        String encoded2 = now2.toString();
        assertEquals (encoded, encoded2);
        assertEquals ('-', encoded.charAt( 4));
        assertEquals ('-', encoded.charAt( 7));
        assertEquals (' ', encoded.charAt(10));
        assertEquals (':', encoded.charAt(13));
        assertEquals (':', encoded.charAt(16));
        assertEquals (19, encoded.length());

    }
}
