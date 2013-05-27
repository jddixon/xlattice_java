/* TestKeySelector.java */
package org.xlattice.crypto.filters;

import java.util.Date;
import java.util.Random;

/**
 * @author Jim Dixon
 **/

import junit.framework.*;
/**
 * Bloom filters for sets whose members are SHA1 digests.
 * 
 * @author <A HREF="mailto:jddixon@users.sourceforge.net">Jim Dixon</A>
 */
public class TestKeySelector extends TestCase {

    private KeySelector ks;
    private int m;          // size of set expressed as a power of two
    private int k;          // number of filters
    private byte[][] keys;
    private int[] bOff;
    private int[] wOff;
    
    public TestKeySelector (String name) {
        super(name);
    }

    public void setUp () {
        ks = null;
        m = 20;             // default
        k = 8;
        // 32 keys by default
        keys = new byte[32][20];
        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 20; j++) {
                keys[i][j] = 0;
            }
        }
        bOff = new int[k];  // array length == 8 by default
        wOff = new int[k];
    }
    
    /** 
     * Verify that out of range or otherwise unacceptable constructor
     * parameters are caught.
     */
    public void testParamExceptions() {
        // m checks
        try {
            ks = new KeySelector(-5, k, bOff, wOff);
            fail("didn't catch negative filter size exponent");
        } catch (IllegalArgumentException e) {}
        try {
            ks = new KeySelector(0, k, bOff, wOff);
            fail("didn't catch zero filter size exponent");
        } catch (IllegalArgumentException e) {}
        
        // checks on k
        try {
            ks = new KeySelector(20, -1, bOff, wOff);
            fail("didn't catch zero hash function count");
        } catch (IllegalArgumentException e) {}
        try {
            ks = new KeySelector(20, 0, bOff, wOff);
            fail("didn't catch zero hash function count");
        } catch (IllegalArgumentException e) {}
        try {
            ks = new KeySelector(3, 0, bOff, wOff);
            fail("didn't catch invalid hash function count");
        } catch (IllegalArgumentException e) {}
        try {
            ks = new KeySelector(247, 0, bOff, wOff);
            fail("didn't catch invalid hash function count");
        } catch (IllegalArgumentException e) {}
        
        // checks on arrays
        try {
            ks = new KeySelector(20, 8, null, wOff);
            fail("didn't catch null bit offset array");
        } catch (IllegalArgumentException e) {}
        try {
            ks = new KeySelector(20, 8, bOff, null);
            fail("didn't catch null word offset array");
        } catch (IllegalArgumentException e) {}
    }
    /** 
     * Set the bit selectors, which are 5-bit values packed at
     * the beginning of a key.
     * @param b   key, expected to be at least 20 bytes long
     * @param val array of key values, expected to be k long
     */
    private void setBitOffsets (byte[] b, int[] val) {
        int bLen = b.length;
        int vLen = val.length;
        int curBit = 0;
        int curByte;
        for (int i = 0; i < vLen; i++) {
            curByte  = curBit / 8;
            int offsetInByte = curBit - (curByte * 8);
            int bVal = val[i] & KeySelector.UNMASK[5];   // mask value to 5 bits
//          // DEBUG
//          System.out.println(
//              "hash " + i + ": bit " + curBit + ", byte " + curByte
//              + "; inserting " + itoh(bVal) 
//              + " into " + btoh(b[curByte]));
//          // END
            if (offsetInByte == 0) {
                // write val to left end of byte
                //b[curByte] &= 0xf1;
                b[curByte] |= (bVal << 3);
//              // DEBUG
//              System.out.println(
//                  "    current byte becomes " + btoh(b[curByte]));
//              // END
            } else if (offsetInByte < 4) {
                // it will fit in this byte
                //b[curByte] &= ( KeySelector.MASK[5] << (3 - offsetInByte) );
                b[curByte] |= ( bVal << (3 - offsetInByte) );
//              // DEBUG
//              System.out.println(
//                  "    offsetInByte " + offsetInByte
//              + "\n    current byte becomes " + btoh(b[curByte]));
//              // END
            } else { 
                // some goes in this byte, some in the next
                int bitsThisByte = 8 - offsetInByte;
//              // DEBUG
//              System.out.println(
//                  "SPLIT VALUE: "
//                  + "bit " + curBit + ", byte " + curByte 
//                  + ", offsetInByte " + offsetInByte
//                  + ", bitsThisByte = " + bitsThisByte);
//              // END
                int valThisByte = (bVal & KeySelector.UNMASK[bitsThisByte]);
                //b[curByte] &= KeySelector.MASK[bitsThisByte];
                b[curByte] |= valThisByte;
                
                int valNextByte = (bVal & KeySelector.MASK[bitsThisByte]) 
                                    << 3;
                //b[curByte+1] &= (KeySelector.MASK[5 - bitsThisByte] 
                //                    << (3 + bitsThisByte));
                b[curByte+1] |= valNextByte;
            }
            curBit += 5;
        }    
    } 
    /** exhaustive test */
    public void testBitSelection () {
        // set up 32 test keys
        for (int i = 0; i < 32; i++) {        
            int [] bitOffsets = {
                (i   % 32), (i+1 % 32), (i+2 % 32), (i+3 % 32), 
                (i+4 % 32), (i+5 % 32), (i+6 % 32), (i+7 % 32)  };
            setBitOffsets (keys[i], bitOffsets);
       }
        ks = new KeySelector(m, k, bOff, wOff);   // default m=20, k=8
        for (int i = 0; i < 32; i++) {
            ks.getOffsets(keys[i]);
            for (int j = 0; j < k; j++) {
                assertEquals(
                    "key " + i + ", func " + j + " returns wrong value",
                        (i + j) % 32, bOff[j] );
            }
        }
    }  
    /** 
     * Set the word selectors, which are (m-5)-bit values.
     * @param b   key, expected to be at least 20 bytes long
     * @param val array of key values, expected to be k long
     */
    private void setWordOffsets (byte[] b, int[] val, 
                                            final int m, final int k) {
        int bLen = b.length;
        int vLen = val.length;
        int stride = m - 5;     // number of bits in word selector

        int curBit = k * 5;     // position beyond the bit selectors
        int curByte;
        for (int i = 0; i < vLen; i++) {
            // force value within range
            int wVal = val[i] & KeySelector.UNMASK[stride];
            int bitsToGo = stride;
            curByte  = curBit / 8;
            int offsetInByte = curBit - (curByte * 8);

//          // DEBUG
//          System.out.println(
//              "hash " + i + ": bit " + curBit + ", byte " + curByte
//              + "; inserting " + itoh(wVal) + " at offset " + offsetInByte
//              + "\n    next three bytes     are " 
//              + btoh(b[curByte])
//              + ( curByte < 19 ? 
//                  " " + btoh(b[curByte+1]) : "" )
//              + ( curByte < 18 ? 
//                  " " + btoh(b[curByte+2]) : "" )
//          );
//          // END

            if (offsetInByte == 0) {
                // aligned
                if (bitsToGo >= 8) {
                    // first of two bytes
                    b[curByte] = (byte)(wVal & KeySelector.UNMASK[8]);
                    wVal >>= 8;
                    bitsToGo -= 8;
                    // second byte
                    b[curByte + 1] |= (wVal & KeySelector.UNMASK[bitsToGo])
                                            << (8 - bitsToGo);
                } else {
                    // only one byte affected
                    b[curByte] |= wVal << (8 - bitsToGo);
                }
            } else {
                // not starting at byte boundary
                if (bitsToGo < (8 - offsetInByte)) {
                    // CASE 1: it all fits in the first byte
                    b[curByte] |= wVal << (offsetInByte - bitsToGo);
                } else {
                    int bitsFirstByte = 8 - offsetInByte;
                    // first byte
                    b[curByte] |= wVal & KeySelector.UNMASK[bitsFirstByte];
                    bitsToGo -= bitsFirstByte;
                    wVal >>= bitsFirstByte;

                    // second byte
                    if (bitsToGo < 8) {
                        // CASE 2: it doesn't fill the second byte
                        b[curByte + 1] |= wVal << (8 - bitsToGo);
                    } else {
                        // CASE 3: it fills the second byte 
                        bitsToGo -= 8;
                        b[curByte + 1] = (byte)(0xff & wVal);
                        if (bitsToGo > 0) {
                            // CASE 4: it puts some bits in a third byte
                            wVal >>= 8;
                            b[curByte + 2] |= (wVal << (8 - bitsToGo));
                        }
                    }
                }
            }
//          // DEBUG
//          System.out.println("    next three bytes are now " 
//              + btoh(b[curByte])
//              + ( curByte < 19 ? 
//                  " " + btoh(b[curByte+1]) : "" )
//              + ( curByte < 18 ? 
//                  " " + btoh(b[curByte+2]) : "" )
//          );
//          // END
            curBit += stride;
        }    
    } 
    public void doTestWordSelection(int m, int k, int numKeys) {
        Random rnd = new Random(new Date().getTime());
        int numWordSel = 1 << (m - 5);
        int[][] wordOffsets = new int [numKeys][8];
        // set up the test keys
        keys = new byte[numKeys][20];
        for (int i = 0; i < numKeys; i++) {
            for (int j = 0; j < 20; j++) {
                keys[i][j] = 0;
            }
        }
        for (int i = 0; i < numKeys; i++) {        
            for (int j = 0; j < k; j++) {
                // up to 2^15 32-bit words in a 2^20 bit array
                wordOffsets[i][j] = rnd.nextInt(numWordSel);
            }
            setWordOffsets (keys[i], wordOffsets[i], m, k);
       }
        ks = new KeySelector(m, k, bOff, wOff);   // default m=20, k=8
        for (int i = 0; i < numKeys; i++) {
            ks.getOffsets(keys[i]);
            for (int j = 0; j < k; j++) {
                assertEquals(
                    "key " + i + ", func " + j + " returns wrong value",
                        wordOffsets[i][j], wOff[j] );
            }
        } 
    } 
   
    public void testWordSelection () {
        doTestWordSelection (20, 8, 32);    // default values, succeeds
        doTestWordSelection (14, 8, 32);    // stride = 9
        doTestWordSelection (13, 8, 32);    // stride = 8
        doTestWordSelection (12, 8, 32);    // stride = 7
    }
    // DEBUG METHODS ////////////////////////////////////////////////
    String itoh (int i) {
        return BloomSHA1.itoh(i);
    }
    String btoh (byte b) {
        return BloomSHA1.btoh(b);
    }
}
