/* TestStringLib.java */
package org.xlattice.util;

import junit.framework.*;

import static org.xlattice.util.StringLib.*;

/**
 * @author Jim Dixon
 */

public class TestStringLib extends TestCase {

    public TestStringLib (String name) {
        super(name);
    }

    public void setUp () {
    }
    public void testChomp()                     throws Exception {
        assertNull ( chomp(null) );
        assertEquals ("", chomp(""));
        assertEquals ("", chomp("\r"));
        assertEquals ("", chomp("\n"));
        // arbitrary sequences eaten
        assertEquals ("abc", chomp("abc\n\r\n\r\r"));
        // trailing spaces preserved
        assertEquals ("def ", chomp("def \n\r\n\r\r"));
    }
    public void testByteArrayToHex()            throws Exception {
        byte[] b0 = new byte[0];
        byte[] b1 = new byte[1];
        byte[] b2 = new byte[2];
        byte[] b3 = new byte[3];
        byte[] b5 = new byte[5];
        assertEquals ("", byteArrayToHex(b0));
        b1[0] = -127;
        assertEquals ("81", byteArrayToHex(b1));
        b2[0] = -126;
        b2[1] = -125;
        String actual   = byteArrayToHex(b2);
        String expected = "8283";
        assertEquals (expected, actual);

        b3[0] = -124;
        b3[1] = -123;
        b3[2] = -122;
        actual   = byteArrayToHex(b3);
        expected = "848586";
        assertEquals (expected, actual);
        
        b5[0] = -121;
        b5[1] = -120;
        b5[2] = -119;
        b5[3] = -118;
        b5[4] = -117;
        actual   = byteArrayToHex(b5);
        expected = "8788898a8b";
        assertEquals (expected, actual);

        assertEquals("88898a", byteArrayToHex(b5, 1, 3));
    }
    public void testLCFirst()                   throws Exception {
        try {
            lcFirst(null);
            fail("lcFirst() didn't catch null argument");
        } catch (Exception e) { /* OK */ }
        try {
            lcFirst("");
            fail("lcFirst() didn't catch empty argument");
        } catch (Exception e) { /* OK */ }
        assertEquals ("ladies",     lcFirst("Ladies"));
    } 
    public void testUCFirst()                   throws Exception {
        try {
            ucFirst(null);
            fail("ucFirst() didn't catch null argument");
        } catch (Exception e) { /* OK */ }
        try {
            ucFirst("");
            fail("ucFirst() didn't catch empty argument");
        } catch (Exception e) { /* OK */ }

        assertEquals ("A",      ucFirst("A"));
        assertEquals ("Z",      ucFirst("z"));
        assertEquals ("0",      ucFirst("0"));
        assertEquals ("Abcd",   ucFirst("abcd"));
    } 

}
