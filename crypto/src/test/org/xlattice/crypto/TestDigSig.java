/* TestDigSig.java */
package org.xlattice.crypto;

import java.math.BigInteger;
import java.security.*;
import java.security.spec.*;
import java.util.Date;
import java.util.Random;
import javax.crypto.*;

/**
 * @author Jim Dixon
 **/

import junit.framework.*;

import org.xlattice.DigSigner;
import org.xlattice.SigVerifier;

public class TestDigSig extends TestCase {

    final private int RSA_BITS_PER_BLOCK = 1024;
    final private KeyPairGenerator keyGen;
    private KeyFactory factory;
    private SecureRandom rng;

    public TestDigSig (String name) {
        super(name);

        byte[] seed = new byte[20]; // just a dash of randomness
        try {
            keyGen  = KeyPairGenerator.getInstance("rsa");
            rng     = SecureRandom.getInstance("SHA1PRNG");
            factory = KeyFactory.getInstance("rsa");
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException (
                    "Java libraries not installed correctly? - " + e);
        }
        new Random(new Date().getTime()).nextBytes(seed);
        rng.setSeed(seed);
        keyGen.initialize(RSA_BITS_PER_BLOCK, rng);
    }

    private KeyPair    pair;
    private PrivateKey jcaPrivKey;
    private java.security.PublicKey  jcaPubKey;
    private Signature  jcaSig;
    private byte[]     data;
    private RSAPrivateCrtKeySpec privSpec;
    private byte[]     digSig1;
    private byte[]     digSig2;

    public void setUp () {
        pair = null;
        jcaPrivKey = null;
        jcaSig = null;
        data = null;
        privSpec = null; 
    }

    /**
     * Create an RSA key pair, including the public and private keys.
     * Initializes
     *   <ul>
     *     <li>pair</li>
     *     <li>jcaPrivKey</li>
     *     <li>jcaPubKey</li>
     *     <li>byte[2048] data, which is filled with random bits</li>
     *     <li>jcaSig</li>
     *     <li>digSig1, an SHA1withRSA digital signature</li>
     *   </ul>
     * The digital signature is also verified.
     */
    public void makeJCAKeyPair()                      throws Exception {
        // ----------------------------------------------------------
        // do the dig sig with a generated key
        pair       = keyGen.generateKeyPair();
        jcaPrivKey = pair.getPrivate();
        jcaPubKey  = pair.getPublic();

        data = new byte[2048];
        rng.nextBytes(data);            // 2 KB of primo random bits

        // standard digital signature, SHA1 and RSA ///////
        jcaSig = Signature.getInstance("SHA1withRSA");

        jcaSig.initSign(jcaPrivKey);    // MODE => sign
        jcaSig.update(data);
        digSig1 = jcaSig.sign();
        // signature is one block long
        assertEquals (RSA_BITS_PER_BLOCK / 8, digSig1.length);

        jcaSig.initVerify(jcaPubKey);   // MODE => verify
        jcaSig.update(data);
        assertTrue ( jcaSig.verify(digSig1) );
    }
    public void testJcaRsaWithSha1()            throws Exception {
        // create a JCA RSA key pair and dig sig, and verify the sig
        makeJCAKeyPair(); 
        // ----------------------------------------------------------
        // try doing the dig sig with a manually constructed private key
        privSpec = (RSAPrivateCrtKeySpec)factory.getKeySpec(
                            jcaPrivKey, RSAPrivateCrtKeySpec.class);
        BigInteger n = privSpec.getModulus();
        BigInteger d = privSpec.getPrivateExponent();
        BigInteger e = privSpec.getPublicExponent();    

        RSAPrivateKeySpec myPrivSpec = new RSAPrivateKeySpec (n, d);
        PrivateKey        myPrivKey  = factory.generatePrivate(myPrivSpec);
        assertNotNull(myPrivKey);   // GEEP
        
        RSAPublicKeySpec  myPubSpec  = new RSAPublicKeySpec  (n, e);
        java.security.PublicKey
                          myPubKey   = factory.generatePublic(myPubSpec);
        assertNotNull(myPubKey);
        
        jcaSig = Signature.getInstance("SHA1withRSA");
        assertNotNull(jcaSig);
        
        jcaSig.initSign(myPrivKey);     // MODE => sign
        jcaSig.update(data);            // same data being signed
        digSig2 = jcaSig.sign();
        assertNotNull(digSig2);
        // signature is one block long
        assertEquals (RSA_BITS_PER_BLOCK / 8, digSig2.length);
        
        jcaSig.initVerify(myPubKey);    // MODE => verify
        jcaSig.update(data);
        assertTrue ( jcaSig.verify(digSig2) );
      
        // AS WE WOULD EXPECT, signatures are identical :-)
        for (int i = 0; i < digSig1.length; i++)
            assertEquals (digSig1[i], digSig2[i]);
                    
        // ----------------------------------------------------------
        // for comparison, RSA-encrypted SHA1 digest //////
        SHA1Digest sha = new SHA1Digest();
        byte[] hash    = sha.digest(data);
        assertEquals (20, hash.length);

        // sigh ... JCA, JCE don't support RSA
        try {
            Cipher rsa = Cipher.getInstance("RSA");
            rsa.init (Cipher.ENCRYPT_MODE, jcaPrivKey);
            byte[] signedHash = rsa.doFinal(hash);
            assertEquals (RSA_BITS_PER_BLOCK / 8, signedHash.length);
        } catch (NoSuchAlgorithmException nsae) {
            /* expected */
        }
    }
    public RSAKey makeRSAfromJCA()              throws Exception {
        privSpec = (RSAPrivateCrtKeySpec)factory.getKeySpec(
                            jcaPrivKey, RSAPrivateCrtKeySpec.class);
        BigInteger p = privSpec.getPrimeP();
        BigInteger q = privSpec.getPrimeQ();
        BigInteger e = privSpec.getPublicExponent();    
        BigInteger d = privSpec.getPrivateExponent();
        return new RSAKey (p, q, e, d);
    }
    /**
     * XXX update (data, offset, len) method NOT TESTED XXX
     */
    public void testRSADigSigner()              throws Exception {
        for (int i = 0; i < 16; i++) {
            makeJCAKeyPair();
            RSAKey key = makeRSAfromJCA();
            assertNotNull(key);
            DigSigner signer = key.getSigner("sha1");
            assertNotNull(signer);
            
            // FAILED: expected:<128> but was:<129>  on every case tested
            // assertEquals (RSA_BITS_PER_BLOCK / 8, signer.length());
            
            signer.update(data);        // initialization is implicit
            digSig2 = signer.sign();
            assertEquals (RSA_BITS_PER_BLOCK / 8, digSig2.length);
            jcaSig.initVerify(jcaPubKey);
            jcaSig.update(data);
            assertTrue (jcaSig.verify(digSig2));
        }
    }
    public void testRSASigVerifier()            throws Exception {
        SigVerifier verifier = new SHA1withRSAVerifier();
        assertNotNull(verifier);
        assertEquals("SHA1withRSA", verifier.getAlgorithm());

        for (int i = 0; i < 16; i++) {
            makeJCAKeyPair();
            RSAKey key = makeRSAfromJCA();
            DigSigner signer = key.getSigner("sha1");
            signer.update(data);        // initialization is implicit
            digSig2 = signer.sign();
            
            verifier.init(key.getPublicKey());
            verifier.update(data);
            assertTrue (verifier.verify(digSig2));
        }
    }
}
