/* TestUIntLib.java */
package org.xlattice.util;

import java.util.Random;

import junit.framework.*;

/**
 * @author Jim Dixon
 **/

public class TestUIntLib extends TestCase {

    private Random rng = new Random();
    
    public TestUIntLib (String name) {
        super(name);
    }

    public void setUp () {
    }

    public void testUInt16 ()                   throws Exception {
        byte[] buffer;
        for (int i = 0; i < 16; i++) {
            int n = rng.nextInt(65536);
            buffer = new byte[256];
            int offset = rng.nextInt(127) * 2;

            int newOffset = UIntLib.encodeUInt16(n, buffer, offset);
            assertEquals( offset + 2,   newOffset);
            int decoded = UIntLib.decodeUInt16(buffer, offset);
            assertEquals( n,            decoded);
        }
    }
}
