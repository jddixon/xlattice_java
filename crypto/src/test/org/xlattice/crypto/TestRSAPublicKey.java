/* TestRSAPublicKey.java */
package org.xlattice.crypto;

import java.math.BigInteger;
import java.security.*;
import java.security.spec.*;
import java.util.Date;
import java.util.Random;

/**
 * @author Jim Dixon
 **/

import junit.framework.*;

import org.xlattice.CryptoException;

/**
 * Minimalist set of tests of RSAPublicKey.
 */
public class TestRSAPublicKey extends TestCase {

    private RSAKeyGen keyGen;
    private Random rng = new Random( new Date().getTime() );

    private RSAKey key;
    private RSAPublicKey pubkey;
    
    public TestRSAPublicKey (String name) {
        super(name);

        try {
            keyGen = new RSAKeyGen();
        } catch (CryptoException e) {
            System.err.println("problem creating RSAKeyGen: " + e);
        }
    }

    public void setUp () {
        key    = null;
        pubkey = null;
    }
    public void testEqualsAndHashCode()         throws Exception {
        key = (RSAKey) keyGen.generate();
        assertNotNull(key);

        pubkey = (RSAPublicKey)key.getPublicKey();
        assertFalse(pubkey.equals(null));
        assertTrue (pubkey.equals(pubkey));

        RSAKey key2 = (RSAKey) keyGen.generate();
        assertFalse (key.equals(key2));         // most improbable
        RSAPublicKey pubkey2 = (RSAPublicKey)key2.getPublicKey();
        assertFalse(pubkey2.equals(pubkey));
        assertTrue (pubkey2.equals(pubkey2));

        // these tests will fail at least once every 4 billion times
        assertFalse (pubkey.hashCode() == 0);
        assertFalse (pubkey.hashCode() == pubkey2.hashCode());
    }
}
