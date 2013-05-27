/* TestKey64Coder.java */
package org.xlattice.crypto;

import java.math.BigInteger;
import java.util.Date;
import java.util.Random;

/**
 * @author Jim Dixon
 **/

import junit.framework.*;
import org.xlattice.util.Base64Coder;

public class TestKey64Coder extends TestCase {

    private String s, s2, encoded, expected;
    /** modulus */
    private BigInteger n;
    private byte[] b;
    private int i;
    private Base64Coder bCoder = new Base64Coder();     // less typing
    private Key64Coder    kCoder; 
    
    private Random rng = new Random ( new Date().getTime() );
    
    public final static String EOL = Key64Coder.CRLF;

    public TestKey64Coder (String name) {
        super(name);
    }

    public void setUp () {
        s = s2 = encoded = expected = null;
        n = null; 
        b = null;
        kCoder = new Key64Coder();
    }

    public void testExceptions()                throws Exception {
        b = new byte[21];
        rng.nextBytes(b);
        encoded = bCoder.encode(b);
        try {
            s = kCoder.prefixEncodeAndFold(null, b, 3);
            fail ("expected null prefix exception");
        } catch (IllegalArgumentException e) { /* success */ }
        try {
            s = kCoder.prefixEncodeAndFold("rsa", null, 3);
            fail ("expected null byte array exception");
        } catch (IllegalArgumentException e) { /* success */ }
        try {
            s = kCoder.prefixEncodeAndFold("rsa", b, -1);
            fail ("expected illegal int value exception");
        } catch (IllegalArgumentException e) { /* success */ }
    }
    public void testOneLiners()                throws Exception {
        b = new byte[21];
        rng.nextBytes(b);
        encoded = bCoder.encode(b);
        s = kCoder.prefixEncodeAndFold ("rsa", b, 3);
        expected = "rsa " + encoded + " 3";
        assertEquals (expected, s);

        b = new byte[51];
        rng.nextBytes(b);
        encoded = bCoder.encode(b);
        s = kCoder.prefixEncodeAndFold ("rsa", b, 3);
        expected = "rsa " + encoded + " 3";
        assertEquals (expected, s);
    }
    public void testTwoLiners ()                throws Exception {
        b = new byte[54];
        rng.nextBytes(b);
        encoded = bCoder.encode(b);
        s = kCoder.prefixEncodeAndFold ("rsa", b, 3);
        // first space for folding, second for delimiter
        expected = "rsa " + encoded + EOL + "  3";
        assertEquals (expected, s);

        b = new byte[57];
        rng.nextBytes(b);
        encoded = bCoder.encode(b);
        s = kCoder.prefixEncodeAndFold ("rsa", b, 3);
        expected = "rsa " + encoded.substring(0, encoded.length() - 4) 
                    + EOL + " " 
                   + encoded.substring(encoded.length() - 4) + " 3";
        assertEquals (expected, s);
    }
    public void testMoreTwoLiners ()            throws Exception {
        b = new byte[51];
        rng.nextBytes(b);
        encoded = bCoder.encode(b);
        s = kCoder.prefixEncodeAndFold ("rsa", b, 65537);
        // first space for folding, second for delimiter
        expected = "rsa " + encoded + EOL + "  65537";
        assertEquals (expected, s);

        b = new byte[54];
        rng.nextBytes(b);
        encoded = bCoder.encode(b);
        s = kCoder.prefixEncodeAndFold ("rsa", b, 65537);
        expected = "rsa " + encoded + EOL + "  65537";
        assertEquals (expected, s);

        b = new byte[57];
        rng.nextBytes(b);
        encoded = bCoder.encode(b);
        s = kCoder.prefixEncodeAndFold ("rsa", b, 65537);
        expected = "rsa " + encoded.substring(0, encoded.length() - 4) 
                    + EOL + " "
                    + encoded.substring(encoded.length() - 4) + " 65537";
        assertEquals (expected, s);
    }
    public void testRSAPublicKeys()             throws Exception {
        RSAPublicKey pubkey;
        RSAPublicKey pubkey2;
        BigInteger n;
        BigInteger e = new BigInteger("3"); 
        
        // ONE LINERS /////////////////////////////////////
        b = new byte[21];
        rng.nextBytes(b);
        n = new BigInteger(b);
        
        encoded = bCoder.encode(b);
        s = kCoder.prefixEncodeAndFold ("rsa", b, 3);
        expected = "rsa " + encoded + " 3";
        assertEquals (expected, s);
        
        pubkey = new RSAPublicKey (n, e);
        s2 = kCoder.encodeRSAPublicKey (pubkey);
        assertEquals (expected, s2);
        pubkey2 = kCoder.decodeRSAPublicKey(s2);
        assertTrue (n.equals(pubkey2.getModulus()));
        assertTrue (e.equals(pubkey2.getExponent()));

        // TWO LINERS /////////////////////////////////////
        b = new byte[54];
        rng.nextBytes(b);
        n = new BigInteger(b);
        
        encoded = bCoder.encode(b);
        s = kCoder.prefixEncodeAndFold ("rsa", b, 3);
        expected = "rsa " + encoded + EOL + "  3";
        assertEquals (expected, s);

        pubkey = new RSAPublicKey (n, e);
        s2 = kCoder.encodeRSAPublicKey (pubkey);
        assertEquals (expected, s2);
        pubkey2 = kCoder.decodeRSAPublicKey(s2);
        assertTrue (n.equals(pubkey2.getModulus()));
        assertTrue (e.equals(pubkey2.getExponent()));

        /////////////////////
        b = new byte[57];
        rng.nextBytes(b);
        n = new BigInteger(b);
        
        encoded = bCoder.encode(b);
        s = kCoder.prefixEncodeAndFold ("rsa", b, 3);
        expected = "rsa " + encoded.substring(0, encoded.length() - 4) 
                    + EOL + " " 
                   + encoded.substring(encoded.length() - 4) + " 3";
        assertEquals (expected, s);
        
        pubkey = new RSAPublicKey (n, e);
        s2 = kCoder.encodeRSAPublicKey (pubkey);
        assertEquals (expected, s2);
        pubkey2 = kCoder.decodeRSAPublicKey(s2);
        assertTrue (n.equals(pubkey2.getModulus()));
        assertTrue (e.equals(pubkey2.getExponent()));

        // MULTI-LINERS ///////////////////////////////////
        for (int i = 0; i < 16; i++) {
            b = new byte[128];
            rng.nextBytes(b);
            n = new BigInteger(b);
            e = new BigInteger("65537"); 
            
            pubkey = new RSAPublicKey (n, e);
            s2 = kCoder.encodeRSAPublicKey (pubkey);
            pubkey2 = kCoder.decodeRSAPublicKey(s2);
            assertTrue (n.equals(pubkey2.getModulus()));
            assertTrue (e.equals(pubkey2.getExponent()));
        }
    }
}
