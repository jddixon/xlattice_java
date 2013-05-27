/* TestSHA1.java */
package org.xlattice.crypto.digest;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Date;
import java.util.Random;

import junit.framework.*;

import org.xlattice.crypto.Digest;
import org.xlattice.crypto.SHA1Digest;      // wrapper around JCA SHA1
import org.xlattice.util.StringLib;

/**
 * @author <A HREF="mailto:jddixon@users.sourceforge.net">Jim Dixon</A>
 */
public class TestSHA1 extends TestCase {

    static final int LEN1 = 40;
    static final int LEN2 = 80;

    // SHANDONG /////////////////////////////////////////////////////
    /**
     * SHA-1 collision from "Collision Search Attacks on SHA1",
     * Xiaoyun Wang, Yiqun Lisa Yin, Hongbo Yu, 13 February 2005.
     */
    public static final byte[] M0 = toByteArray(
        "132b5ab6" + "a115775f" + "5bfddd6b" + "4dc470eb" +
        "0637938a" + "6cceb733" + "0c86a386" + "68080139" +
        "534047a4" + "a42fc29a" + "06085121" + "a3131f73" +
        "ad5da5cf" + "13375402" + "40bdc7c2" + "d5a839e2");

    public static final byte[] M1 = toByteArray(
        "332b5ab6" + "c115776d" + "3bfddd28" + "6dc470ab" +
        "e63793c8" + "0cceb731" + "8c86a387" + "68080119" +
        "534047a7" + "e42fc2c8" + "46085161" + "43131f21" +
        "0d5da5cf" + "93375442" + "60bdc7c3" + "f5a83982");

    public static final byte[] h1 = toByteArray(
        "9768e739" + "b662af82" + "a0137d3e" + "918747cf" + "c8ceb7d4");

    // PRIVATE MEMBERS //////////////////////////////////////////////
    MessageDigest jca;
    Digest digest;
    byte [] data1;
    byte [] data2;
    byte [] data12;
    Random rng = new Random ( new Date().getTime() );

    public TestSHA1 (String name) {
        super(name);
    }

    public void setUp ()                        throws Exception  {
        jca = MessageDigest.getInstance("SHA-1");
        digest = new SHA1();
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

    /**
     * @param i integer which is being nibbled at
     * @param n nibble index, low-order nibble being index 0
     * @return the value of the n-th nibble of int i
     */
    public int nibble(int i, int n) {
        return 0xf & ( i >> (n * 4) );
    }

    public void testConstants()                 throws Exception {
        /** h0's little symmetries */
        for (int i = 0; i < 8; i++) {
            assertEquals(   7, nibble(SHA1.A0, i) + nibble(SHA1.D0, i));
            assertEquals(0x17, nibble(SHA1.B0, i) + nibble(SHA1.C0, i));
            assertEquals( nibble(SHA1.A0, i), nibble(SHA1.D0, 7 - i));
            assertEquals( nibble(SHA1.B0, i), nibble(SHA1.C0, 7 - i));
        }
        for (int i = 0; i < 8;    ) {
            assertEquals(0xf, nibble(SHA1.E0, i++) + nibble(SHA1.E0, i++));
        }

        /** 2^32 * sqrt(2)/4 */
        assertEquals( (int) (Math.sqrt(2)  * (1 << 30)), SHA1.K0);
        /** 2^32 * sqrt(3)/4 */
        assertEquals( (int) (Math.sqrt(3)  * (1 << 30)), SHA1.K1);
        /** 2^32 * sqrt(5)/4, seen as negative by Java, hence double cast */
        assertEquals( (int) ((long)(Math.sqrt(5)  * (1 << 30))), SHA1.K2);
        /** 2^32 * sqrt(10)/4, seen as negative */
        assertEquals( (int) ((long)(Math.sqrt(10) * (1 << 30))), SHA1.K3);
    }
    public void testConstructors()              throws Exception {
        SHA1 sha1 = new SHA1();
        int val[] = sha1.intResult();
        assertEquals (SHA1.A0, val[0]);
        assertEquals (SHA1.B0, val[1]);
        assertEquals (SHA1.C0, val[2]);
        assertEquals (SHA1.D0, val[3]);
        assertEquals (SHA1.E0, val[4]);

        byte hash[] = sha1.byteResult();
        assertEquals (SHA1.A0, ((0xff & hash[ 0]) << 24)
                             | ((0xff & hash[ 1]) << 16)
                             | ((0xff & hash[ 2]) <<  8)
                             | ((0xff & hash[ 3])      ) );
        assertEquals (SHA1.B0, ((0xff & hash[ 4]) << 24)
                             | ((0xff & hash[ 5]) << 16)
                             | ((0xff & hash[ 6]) <<  8)
                             | ((0xff & hash[ 7])      ) );
        assertEquals (SHA1.C0, ((0xff & hash[ 8]) << 24)
                             | ((0xff & hash[ 9]) << 16)
                             | ((0xff & hash[10]) <<  8)
                             | ((0xff & hash[11])      ) );
        assertEquals (SHA1.D0, ((0xff & hash[12]) << 24)
                             | ((0xff & hash[13]) << 16)
                             | ((0xff & hash[14]) <<  8)
                             | ((0xff & hash[15])      ) );
        assertEquals (SHA1.E0, ((0xff & hash[16]) << 24)
                             | ((0xff & hash[17]) << 16)
                             | ((0xff & hash[18]) <<  8)
                             | ((0xff & hash[19])      ) );
    }
    /**
     * "Federal Information Processing Standards Publication 180-1
     *  1995 April 17 Announcing the Standard for SECURE HASH STANDARD"
     *
     * Confirm that we get the same results as the three examples in 
     * the standard.
     */
    public void testFIPS()                      throws Exception {
        SHA1 sha1 = new SHA1();

        // Appendix A: hash for "abc"
        byte[] abc = new byte[] { (byte)'a', (byte)'b', (byte)'c' };
        byte[] abcDigest = toByteArray(
            "A9993E36" + "4706816A" + "BA3E2571" + "7850C26C" + "9CD0D89D");
        final int FIPS_ABC_LEN = 24;            // bits
        sha1.reset();
        byte[] myDigest = sha1.digest(abc);
        assertNotNull(myDigest);
        assertEquals(myDigest.length, abcDigest.length);
        for (int i = 0; i < myDigest.length; i++)
            assertEquals(abcDigest[i], myDigest[i]);

        // Appendix B: hash for
        //   "abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq"
        // this makes sense only with appropriate charset
        byte[] abcPq
            = "abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq"
                .getBytes();
        assertEquals(56, abcPq.length);
        byte[] abcPqDigest = toByteArray(
            "84983E44" + "1C3BD26E" + "BAAE4AA1" + "F95129E5" + "E54670F1");
        final int FIPS_ABC_PQ_LEN = 448;        // bits, 56 bytes
        sha1.reset();
        myDigest = sha1.digest(abcPq);
        assertNotNull(myDigest);
        for (int i = 0; i < myDigest.length; i++)
            assertEquals(abcPqDigest[i], myDigest[i]);

        // Appendix C: hash for one million 'a'
        byte[] millionDigest = toByteArray(
            "34AA973C" + "D4C4DAA4" + "F61EEB2B" + "DBAD2731" + "6534016F");
        final int FIPS_MILLION_LEN = 8000000;   // bits, of course
        byte[] thousand = new byte[1000];
        for (int i = 0; i < 1000; i++)
            thousand[i] = (byte)'a';
        sha1.reset();
        for (int i = 0; i < 1000; i++)
            sha1.update(thousand);
        myDigest = sha1.digest();
        for (int i = 0; i < myDigest.length; i++)
            assertEquals(millionDigest[i], myDigest[i]);
    }
    public void testShandong()                  throws Exception {
        // System.out.println(StringLib.byteArrayToHex(h1));
        assertEquals ( (byte)0x97, h1[0] );
        assertEquals ( 20, h1.length );

        SHA1 sha1 = new SHA1();
        sha1._cycle(M0, 0, 58, false);
        int [] results0 = sha1.intResult();
        sha1.reset();
        sha1._cycle(M1, 0, 58, false);
        int [] results1 = sha1.intResult();
        assertEquals(results0.length, results1.length);
        // This is the Shandong note's conclusion: there is a collision
        // between the two messages at round 58 (one-based).
        for (int i = 0; i < results0.length; i++)
            assertEquals((char)('A' + i) + " mismatch",
                                    results0[i], results1[i]);

        // In fact rounds 48 - 58 inclusive collide
        for (int k = 48; k < 58; k++) {
            sha1.reset();
            sha1._cycle(M0, 0, k, false);
            results0 = sha1.intResult();
            sha1.reset();
            sha1._cycle(M1, 0, k, false);
            results1 = sha1.intResult();
            for (int i = 0; i < results0.length; i++)
                assertEquals("failure in round " + k,
                        results0[i], results1[i]);
        }
    }
    /**
     * Test (a) that multiple updates have the same effect as a 
     * single update with the same aggregate content and (b) that
     * our SHA1 returns the same results as the JCA SHA1.
     */
    public void testConcat()                    throws Exception {
        assertEquals (20, digest.length());

        byte[] result1;
        byte[] result2;
        byte[] result12;

        // run the same test 16 times with (pseudo-)random content
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
    private byte[] testBuf;

    private final static int UPDATE_COUNT = 10000;
    private final static int UPDATE_SIZE  =  2000;
    
    public long digestPerf(Digest digest)       throws Exception {
        int offset = 0;
        long t0 = System.currentTimeMillis();
        digest.reset();
        for (int i = 0; i < UPDATE_COUNT; i++)
            digest.update(testBuf);
        byte[] hash = digest.digest();
        return System.currentTimeMillis() - t0;
    }
    /**
     * Crude relative performance test.  Not spectacular, but Sun's
     * JCA SHA1 seems to take about 40% longer than XLattice unrolled
     * algorithm - on my test rig, 727 ms vs 511 ms for 10K updates
     * of 2KB each.  Similar results were obtained with longer runs
     * (up to 1 million updates) and larger update sizes (up to 400 000 B).
     *
     * Using System.arraycopy() _worsened) performance by about 5%, so
     * that XLattice's unrolled method was about 35% faster than JCA SHA1.
     */
    public void testPerf()                      throws Exception {
        testBuf = new byte[UPDATE_SIZE];
        rng.nextBytes(testBuf);
        
        long jcaTime   = digestPerf( new SHA1Digest() );
        long fastCycleTime = digestPerf (new SHA1() );
        
        System.out.println(
                "Time for " + UPDATE_COUNT + " SHA1 updates of "
                    + UPDATE_SIZE + " bytes each:"
            + "\n    JCA SHA1 (ms)      = " + jcaTime
            + "\n    XLattice SHA1 (ms) = " + fastCycleTime
        );
    }
    // UTILITIES ////////////////////////////////////////////////////
    public static byte hexNibble (char c) {
        switch (c) {
            case '0':       return 0;
            case '1':       return 1;
            case '2':       return 2;
            case '3':       return 3;
            case '4':       return 4;
            case '5':       return 5;
            case '6':       return 6;
            case '7':       return 7;
            case '8':       return 8;
            case '9':       return 9;
            case 'A':       return 0xA;
            case 'B':       return 0xB;
            case 'C':       return 0xC;
            case 'D':       return 0xD;
            case 'E':       return 0xE;
            case 'F':       return 0xF;
            case 'a':       return 0xa;
            case 'b':       return 0xb;
            case 'c':       return 0xc;
            case 'd':       return 0xd;
            case 'e':       return 0xe;
            case 'f':       return 0xf;
            default:
                throw new IllegalArgumentException("not a hex digit: " + c);
        }
    }
    public static byte[] toByteArray(String s) {
        int len = s.length();
        if (len == 0 | (len % 2) != 0)
            throw new IllegalArgumentException ("invalid String length "
                    + len);
        byte[] buffer = new byte[len/2];        // 2 characters per byte
        for (int i = 0; i < len; /* */ )
            buffer[i / 2] = (byte)( (hexNibble(s.charAt(i++)) << 4)
                                   | hexNibble(s.charAt(i++))      );
        return buffer;
    }
}
