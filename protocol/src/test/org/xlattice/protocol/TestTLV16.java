/* TestTLV16.java */
package org.xlattice.protocol;

import java.util.Random;

import junit.framework.*;

/**
 * @author Jim Dixon
 */

public class TestTLV16 extends TestCase {

    Random rng = new Random();
    TLV16 tlv;
    byte[] value;
    
    
    public TestTLV16 (String name) {
        super(name);
    }

    public void setUp () {
        tlv = null;
    }

    public void testConstructors()              throws Exception {
        value = new byte[16];
        rng.nextBytes(value);
        
        // impossible types
        try {
            tlv = new TLV16 (-1, value); 
            fail("constructor succeeds with negative type");
        } catch (IllegalArgumentException iae) { /* success */ }
        try {
            tlv = new TLV16 (70000, value); 
            fail("constructor succeeds with type over 64K");
        } catch (IllegalArgumentException iae) { /* success */ }

        // bad value arrays
        try {
            tlv = new TLV16 (2, null);
            fail("constructor succeeds with null value array");
        } catch (NullPointerException npe) { /* success */ }

        // bad length
        try {
            tlv = new TLV16 (2, -1, value);
            fail("constructor succeeds with negative array length");
        } catch (IllegalArgumentException iae) { /* success */ }
        try {
            tlv = new TLV16 (2, 20, value);
            fail("constructor succeeds with incorrect array length");
        } catch (IllegalArgumentException iae) { /* success */ }
    }
    public void testFields()                    throws Exception {
        value = new byte[16];
        rng.nextBytes(value);
        
        tlv = new TLV16 (23, value);
        assertEquals (23, tlv.type);
        assertTrue( value == tlv.value );
        assertEquals (value.length, tlv.length());

        tlv = new TLV16 (2345, value);
        assertEquals (2345, tlv.type);
    }
    public void testReadWrite()                 throws Exception {
        for (int i = 0; i < 16; i++) {
            int type = 1 + rng.nextInt(128);
            int len  = 4 * (1 + rng.nextInt(16));
            byte[] value = new byte[len];
            rng.nextBytes(value);
            tlv = new TLV16 ( type, value );
            byte[] buffer = new byte[ 4 + len ];
            tlv.encode(buffer, 0);
            TLV16 decoded = TLV16.decode(buffer, 0);
            assertEquals (type, decoded.type);
            assertEquals (len,  decoded.length());
            for (int j = 0; j < len; j++)
                assertEquals(tlv.value[j],  decoded.value[j]);
        }
    }
}
