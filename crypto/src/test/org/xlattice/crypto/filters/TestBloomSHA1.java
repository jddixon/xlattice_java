/* TestBloomSHA1.java */
package org.xlattice.crypto.filters;

import junit.framework.*;

/**
 * Bloom filters for sets whose members are SHA1 digests.
 * 
 * @author Jim Dixon
 */
public class TestBloomSHA1 extends TestCase {

    private BloomSHA1     filter;
    private int n;          // number of strings in set
    private int m;          // size of set expressed as a power of two
    private int k;          // number of filters
    private byte[][] keys;
    
    public TestBloomSHA1 (String name) {
        super(name);
    }

    public void setUp () {
        filter = null;
        m = 20;             // default
        k = 8;
        keys = new byte[100][20];
    }
    
    public void testEmptyFilter() {
        filter = new BloomSHA1(m, k);
        assertEquals("brand new filter isn't empty", 0, filter.size());
        assertEquals("filter capacity is wrong", 
                                        2 << (m-1), filter.capacity());
    }
    /** 
     * Verify that out of range or otherwise unacceptable constructor
     * parameters are caught.
     */
    public void testParamExceptions() {
        // m checks
        try {
            filter = new BloomSHA1(-5);
            fail("didn't catch negative filter size exponent");
        } catch (IllegalArgumentException e) {}
        try {
            filter = new BloomSHA1(0);
            fail("didn't catch zero filter size exponent");
        } catch (IllegalArgumentException e) {}
        try {
            filter = new BloomSHA1(21);
            fail("didn't catch too-large filter size exponent");
        } catch (IllegalArgumentException e) {}
        
        // checks on k
        try {
            filter = new BloomSHA1(20, -1);
            fail("didn't catch zero hash function count");
        } catch (IllegalArgumentException e) {}
        try {
            filter = new BloomSHA1(20, 0);
            fail("didn't catch zero hash function count");
        } catch (IllegalArgumentException e) {}
        try {
            filter = new BloomSHA1(3, 0);
            fail("didn't catch invalid hash function count");
        } catch (IllegalArgumentException e) {}
        try {
            filter = new BloomSHA1(247, 0);
            fail("didn't catch invalid hash function count");
        } catch (IllegalArgumentException e) {}
    }
    public void doTestInserts (int m, int k, int numKey) {
        byte [][] keys = new byte[numKey][20];
        // set up distinct keys
        for (int i = 0; i < numKey; i++) {
           for (int j = 0; j < 20; j++) {
               keys[i][j] = (byte)(0xff & (i + j + 100));
           }
       }
        filter = new BloomSHA1(m, k);   // default m=20, k=8
        for (int i = 0; i < numKey; i++) {
            assertEquals(i, filter.size());
            assertFalse("key " + i + " not yet in set, but found!", 
                filter.member(keys[i]));
            filter.insert(keys[i]);
        }
        for (int i = 0; i < numKey; i++) {
            // if the message isn't there, we get an NPE - weird
            assertTrue("key " + i + " has been added but not found in set",
                                            filter.member(keys[i]));
        }
    }
    public void testInserts () {
        doTestInserts ( m, k, 16);  // default values
        doTestInserts (14, 8, 16);  // stride = 9
        doTestInserts (13, 8, 16);  // stride = 8
        doTestInserts (12, 8, 16);  // stride = 7
        
        doTestInserts (14, 7, 16);  // stride = 9
        doTestInserts (13, 7, 16);  // stride = 8
        doTestInserts (12, 7, 16);  // stride = 7

        doTestInserts (14, 6, 16);  // stride = 9
        doTestInserts (13, 6, 16);  // stride = 8
        doTestInserts (12, 6, 16);  // stride = 7

        doTestInserts (14, 5, 16);  // stride = 9
        doTestInserts (13, 5, 16);  // stride = 8
        doTestInserts (12, 5, 16);  // stride = 7
    }
}
