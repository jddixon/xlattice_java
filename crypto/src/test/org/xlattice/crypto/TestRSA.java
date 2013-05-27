/* TestRSA.java */
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

public class TestRSA extends TestCase {

    final private KeyPairGenerator keyGen;

    private PrivateKey privKey;
    private java.security.PublicKey pubKey;
    
    public TestRSA (String name) {
        super(name);

        byte[] seed = new byte[20]; // just a dash of randomness
        SecureRandom rng;
        try {
            keyGen = KeyPairGenerator.getInstance("rsa");
            rng    = SecureRandom.getInstance("SHA1PRNG");
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException (
                    "Java libraries not installed correctly? - " + e);
        }
        new Random(new Date().getTime()).nextBytes(seed);
        rng.setSeed(seed);
        keyGen.initialize(1024, rng);
    }

    public void setUp () {
    }

    /**
     * Exercises methods of constructing JCA RSA keys.
     */
    public void testJcaRsaKeyClasses()          throws Exception {
        java.security.KeyPair pair = keyGen.generateKeyPair();
        PrivateKey jcaPrivKey = pair.getPrivate();
        java.security.PublicKey  jcaPubKey  = pair.getPublic();

        // --- save the keys for later use ---
        privKey = jcaPrivKey;
        pubKey  = jcaPubKey;

        // --- end save the keys -------------

        // The JCA docs say nothing about this, but verify that
        // the PrivateKey instance contains full information about
        // the RSA key. [Later note: just print the PrivateKey
        // instance - it's all there.]
        KeyFactory factory = KeyFactory.getInstance("rsa");
        RSAPrivateCrtKeySpec privSpec
            = (RSAPrivateCrtKeySpec)factory.getKeySpec(
                jcaPrivKey, RSAPrivateCrtKeySpec.class);
        BigInteger p = privSpec.getPrimeP();
        BigInteger q = privSpec.getPrimeQ();
        BigInteger modulus = privSpec.getModulus();
        BigInteger e = privSpec.getPublicExponent();
        BigInteger d = privSpec.getPrivateExponent();
        BigInteger expP = privSpec.getPrimeExponentP();
        BigInteger expQ = privSpec.getPrimeExponentQ();
        BigInteger c = privSpec.getCrtCoefficient();

        // let's verify how things work -----------------------------
        BigInteger n = p.multiply(q);
        assertTrue (n.equals(modulus));
        // d mod (p - 1)
        assertTrue (expP.equals( d.mod(p.subtract(BigInteger.ONE))));
        // d mod (q - 1)
        assertTrue (expQ.equals( d.mod(q.subtract(BigInteger.ONE))));
        // CRT coefficient is q^-1 mod p)
        assertTrue (c.equals (q.modInverse(p)));
        // end 'how things work' ------------------------------------

        // Since the private key contains all of this information,
        // why does the JCA KeyPair constructor look like this??  The
        // second argument is all that is necessary.
        java.security.KeyPair pair2
                = new java.security.KeyPair (jcaPubKey, jcaPrivKey);

        // there is no equals() for KeyPairs, and equals() for the
        // specs just tests that the objects are the same, so we have
        // to do it bit by bit
        PrivateKey jcaPrivKey2 = pair2.getPrivate();
        RSAPrivateCrtKeySpec privSpec2
            = (RSAPrivateCrtKeySpec)factory.getKeySpec(
                jcaPrivKey2, RSAPrivateCrtKeySpec.class);
        assertTrue (n.equals(privSpec2.getModulus()));
        assertTrue (d.equals(privSpec2.getPrivateExponent()));
        assertTrue (e.equals(privSpec2.getPublicExponent()));

        // building an RSA key from the key materials; I don't see
        // why this should work; there isn't enough information to
        // make a proper private key spec
        RSAPrivateKeySpec privSpec3 = new RSAPrivateKeySpec (n, d);
        PrivateKey        privKey3  = factory.generatePrivate(privSpec3);
        RSAPublicKeySpec  pubSpec3  = new RSAPublicKeySpec  (n, e);
        java.security.PublicKey
                          pubKey3   = factory.generatePublic(pubSpec3);
        KeyPair pair3 = new KeyPair (pubKey3, privKey3);

        // ... and it doesn't:
        try {
            // this caused an ArrayIndexOutOfBoundsException: 0
            RSAPrivateCrtKeySpec crtSpec3
                = (RSAPrivateCrtKeySpec)factory.getKeySpec(
                                privKey3, RSAPrivateCrtKeySpec.class);
        } catch (ArrayIndexOutOfBoundsException oob) {
            /* ignore, expected */
        } catch (java.security.spec.InvalidKeySpecException ikse) {
            /* new error with Java 1.5 */
        }
        try {
            PrivateKey jcaPrivKey3 = pair3.getPrivate();
            // this caused the same ArrayIndexOutOfBoundsException: 0
            RSAPrivateCrtKeySpec crtSpec3
                = (RSAPrivateCrtKeySpec)factory.getKeySpec(
                                    jcaPrivKey3, RSAPrivateCrtKeySpec.class);
        } catch (ArrayIndexOutOfBoundsException oob) {
            /* ignore, expected */
        } catch (java.security.spec.InvalidKeySpecException ikse) {
            /* new error with Java 1.5 */
        }

        // OK, let's try building a private key from the CRT spec
        RSAPrivateCrtKeySpec privSpec4 = new RSAPrivateCrtKeySpec (
                                        n, e, d, p, q, expP, expQ, c);
        PrivateKey privKey4 = factory.generatePrivate(privSpec4);
        KeyPair pair4 = new KeyPair (pubKey3, privKey4);
        PrivateKey privKey5 = pair4.getPrivate();
        // my suspicion is that these will be the same reference
        assertTrue (privKey4 == privKey5);
        // ... yes, they were

        RSAPrivateCrtKeySpec crtSpec5
            = (RSAPrivateCrtKeySpec)factory.getKeySpec(
                                privKey5, RSAPrivateCrtKeySpec.class);
        assertTrue (n.equals(crtSpec5.getModulus()));
        assertTrue (d.equals(crtSpec5.getPrivateExponent()));
        assertTrue (e.equals(crtSpec5.getPublicExponent()));

        // Conclusion: not all PrivateKeys are equal.  In fact those
        // built from the RSAPrivateKeySpec (n, d) constructor are
        // useless, and any KeyPairs build from such PrivateKeys are
        // equally useless.
        
        // AFTER FURTHER THOUGHT: need to check whether the key
        // built from RSAPrivateKeySpec (n, d) can be used to encrypt
        // anything.  It should work; n and d should be all you need.
        // But I would expect it to be slow.
    }
}
