/* TestBase64.java */
package org.xlattice.util;

/**
 * @author Jim Dixon
 **/

// XXX TEMPORARY ONLY
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.util.Date;
import java.util.Random;

import junit.framework.*;

/**
 * Test encoding and decoding Base64, using Sun's classes as a 
 * standard.
 *
 * XXX We use a crude hack to convert between the older Base64 encoding
 * character set to the newer URL and file-safe character set, 
 * which replaces characters 62 and 63 "+/" with "-_"
 *
 * @author <A HREF="mailto:jddixon@users.sourceforge.net">Jim Dixon</A>
 */
public class TestBase64 extends TestCase {

    private BASE64Decoder sunDecoder = new BASE64Decoder();
    private BASE64Encoder sunEncoder = new BASE64Encoder();

    private Base64Coder coder = new Base64Coder();
    
    Date   now = new Date();
    Random rng = new Random ( now.getTime() );  // time is a long (ms) 
    
    public TestBase64 (String name) {
        super(name);
    }

    public void setUp () {
    }
    /** 
     * Convert from straight Base64 encoding to filesafe character
     * set.  A temporary hack.
     */
    private String toFileSafe (String s) {
        s = s.replace   ('+', '-');
        return s.replace('/', '_');
    }
    /**
     * Convert from filesafe Base64 encoding to older standard's
     * character set.  A temporary hack.
     */
    private String fromFileSafe (String s) {
        s = s.replace('-', '+');
        return s = s.replace('_', '/');
    }
   
    public void testRevMap()                    throws Exception {
        for (char x = 'A' ; x <= 'Z'; x++)
            assertEquals (x - 'A', Base64Coder.reverseMap(x));
        for (char x = 'a' ; x <= 'z'; x++)
            assertEquals (x - 'a' + 26, Base64Coder.reverseMap(x));
        for (char x = '0' ; x <= '9'; x++)
            assertEquals (x - '0' + 52, Base64Coder.reverseMap(x));
        assertEquals(62, Base64Coder.reverseMap('-'));
        assertEquals(63, Base64Coder.reverseMap('_'));
    }
    /**
     * Test byte arrays whose length is a multiple of 3, the easy case.
     */
    public void testTriplets()                  throws Exception {
        byte [] data = new byte [21];
        for (int k = 0; k < 32; k++) {
            rng.nextBytes(data);        // get 21 sort-of random bytes
            String sunString = toFileSafe(sunEncoder.encodeBuffer(data));
            assertNotNull(sunString);
            sunString = sunString.trim();
            // without the trim() this fails, length is 29 
            assertEquals (28, sunString.length());
            assertEquals (sunString, coder.encode(data));
                    
            byte [] sunBytes = sunDecoder
                                .decodeBuffer(fromFileSafe(sunString));
            byte [] ourBytes = coder.decode(sunString);
            assertNotNull(sunBytes);
            assertEquals(21, sunBytes.length);
            for (int i = 0; i < 21; i++) {
                assertEquals(data[i], sunBytes[i]);
                assertEquals(data[i], ourBytes[i]);
            } 
        }
    } 
    public void testVaryingLengths()            throws Exception {
        for ( int n = 1; n < 29; n++ ) {
            byte [] data = new byte [n];
            int expectedLen = ((n + 2)/3) * 4;
            for (int k = 0; k < 16; k++) {
                rng.nextBytes(data);        // get N quasi-random bytes
                String sunString = toFileSafe(sunEncoder.encodeBuffer(data));
                assertNotNull(sunString);
                sunString = sunString.trim();
                assertEquals (expectedLen, sunString.length());
                assertEquals (sunString, coder.encode(data));

                byte [] sunBytes = sunDecoder.decodeBuffer(
                                                fromFileSafe(sunString));
                byte [] ourBytes = coder.decode(sunString);
                assertNotNull(ourBytes);
                assertEquals(n, ourBytes.length);
                for (int i = 0; i < n; i++) {
                    assertEquals(data[i], sunBytes[i]);
                    assertEquals(data[i], ourBytes[i]);
                } 
            }
        } 
    }
}
