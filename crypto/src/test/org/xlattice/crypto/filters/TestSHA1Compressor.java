/* TestSHA1Compressor.java */
package org.xlattice.crypto.filters;

import junit.framework.*;

/**
 * Tests compression/decompression for SHA1-based Bloom filters.
 * 
 * XXX THIS IS JUST A TEMPLATE - nothing appears to be working.
 *
 * @author Jim Dixon
 */
public class TestSHA1Compressor extends TestCase {

    private BloomSHA1     filter;
    private int n;          // number of strings in set
    private int m;          // size of set expressed as a power of two
    private int k;          // number of filters
    private byte[][] keys;
    
    public TestSHA1Compressor (String name) {
        super(name);
    }

    public void setUp () {
        filter  = null;
        m       = 20;             // default
        k       = 8;
        keys    = null;
    }
    
    public void testEmptyFilter() {
        filter = new BloomSHA1(m, k);
        assertEquals("brand new filter isn't empty", 0, filter.size());
        assertEquals("filter capacity is wrong", 
                                        1 << m, filter.capacity());
    }
    public void testInserts () {
    }
}
