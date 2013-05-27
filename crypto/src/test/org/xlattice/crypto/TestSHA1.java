/* TestSHA1.java */
package org.xlattice.crypto;

import java.security.MessageDigest;
import java.util.Date;
import java.util.Random;

/**
 * @author Jim Dixon
 **/

import junit.framework.*;

/**
 * Currently XLattice's SHA1Digest is just the thinnest of wrappers
 * around the Java Cryptography Architecture (JCA) MessageDigest.  This
 * code is intended to test both that wrapper and eventually XLattice's
 * own version of SHA1.
 */
public class TestSHA1 extends TestCase {

    static final int LEN1 = 40;
    static final int LEN2 = 80;
   
    MessageDigest jca;
    Digest digest;
    byte [] data1;
    byte [] data2;
    byte [] data12;
    Random rng = new Random ( new Date().getTime() );
    
    public TestSHA1 (String name) {
        super(name);
    }

    public void setUp ()                      throws Exception  {
        jca = MessageDigest.getInstance("SHA-1");
        digest = new SHA1Digest();
    }

    public void checkSameBytes( byte[] left, byte[] right) {
        if (left == null)
            fail ("left byte array is null");
        if (right == null)
            fail ("right byte array is null");
        assertEquals (left.length, right.length);
        for (int i = 0; i < left.length; i++)
            assertEquals (left[i], right[i]);
    }
    public void testBasics()                    throws Exception {
        assertEquals (20, digest.length());

        byte[] result1;
        byte[] result2;
        byte[] result12;
        // 
        for (int k = 0; k < 16; k++) {
            data1  = new byte[LEN1];
            data2  = new byte[LEN2];
            data12 = new byte[LEN1 + LEN2];
            rng.nextBytes(data1);
            rng.nextBytes(data2);
            // data12 is concatenation of data1 and data2
            int i;
            for (i = 0; i < LEN1; i++)
                data12[i] = data1[i];
            for (     ; i < LEN1 + LEN2; i++)
                data12[i] = data2[i - LEN1];  
            
            // the random 40-byte data array
            digest.update(data1);
            result1 = digest.digest();
            assertEquals(20, result1.length);
            checkSameBytes (result1, jca.digest(data1));
            // random 80-byte data array
            result2 = digest.digest(data2);
            checkSameBytes (result2, jca.digest(data2));
   
            // confirm that if you process first 40 bytes, then 80 bytes,
            // it's the same as processing all 120 bytes at once
            digest.update(data1);
            digest.update(data2);
            result12 = digest.digest();
            assertEquals(20, result12.length);
            checkSameBytes (result12, jca.digest(data12));
        } 
    }
}
