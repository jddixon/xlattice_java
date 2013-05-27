/* TestNibbleCounters.java */
package org.xlattice.crypto.filters;

import junit.framework.*;

/**
 * Tests the counters associated with Bloom filters for sets whose members 
 * are 20-byte SHA1 digests.
 * 
 * @author Jim Dixon
 */
public class TestNibbleCounters extends TestCase {

    private NibbleCounters  nibCount;
    private int filterInts;
    
    public TestNibbleCounters (String name) {
        super(name);
    }

    public void setUp () {
    }
    
    public void doTestBit (int filterWord, int filterBit) {
        int value;
        for (int i = 0; i < 18; i++) {
            value = nibCount.inc(filterWord, filterBit);
            if (i < 15) {
                assertEquals (
                    "word " + filterWord + " bit " + filterBit
                    + ": error adding 1 to " + i,
                    i + 1, value);
            } else {
                assertEquals (
                    "word " + filterWord + " bit " + filterBit
                    + ": overflow error",
                    15, value);
            }
        } 
        for (int i = 0; i < 18; i++) {
            value = nibCount.dec(filterWord, filterBit);
            if (i < 15) {
                assertEquals (
                    "word " + filterWord + " bit " + filterBit
                    + ": error subtracting 1 from " + (15 - i),
                    14 - i, value);
            } else {
                assertEquals (
                    "word " + filterWord + " bit " + filterBit
                    + ": underflow error",
                    0, value);
            }
        } // GEEP
    }
    public void doTestWord (int filterWord) {
        // test the low order bit, the high order bit, and a few from
        // the middle
        doTestBit(filterWord, 0);
        doTestBit(filterWord, 1);
        doTestBit(filterWord, 14);
        doTestBit(filterWord, 15);
        doTestBit(filterWord, 16);
        doTestBit(filterWord, 31);
        
    }
    public void doTest(int m) {
        filterInts = (1 << m)/32;   // 2^m bits fit into this many ints
        // test the low order word, the high order word, and a few from
        // the middle
        nibCount = new NibbleCounters(filterInts);
        doTestWord(0);
        doTestWord( (filterInts/2) - 1 );
        doTestWord( (filterInts/2)     );
        doTestWord( (filterInts/2) + 1 );
        doTestWord(filterInts - 1);
    }
    public void testNibs () {
        doTest (12);
        doTest (13);
        doTest (14);
        doTest (20);
    }
}
