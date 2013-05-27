/* RSAKeyGen.java */
package org.xlattice.crypto;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.spec.RSAPrivateCrtKeySpec;

/**
 * @author Jim Dixon
 **/

import java.util.Date;
import java.util.Random;

import org.xlattice.CryptoException;
import org.xlattice.Key;

/**
 * Factory for generating RSA keys.
 *
 * XXX Provide a getInstance() method?
 */
public class RSAKeyGen implements KeyGen {

    /** generates RSA keys */
    private final KeyPairGenerator keyGen;
    /** crypto-quality random number generatory */
    private final SecureRandom rng;
    /** translates between JCA keys and key specs */
    private final KeyFactory jcaFactory;
    
    /**
     * Create an RSA key factory with a particular key size. 
     */
    public RSAKeyGen (int keySize)              throws CryptoException {
        try {
            keyGen      = KeyPairGenerator.getInstance("rsa");
            rng         = SecureRandom.getInstance("SHA1PRNG");
            jcaFactory  = KeyFactory.getInstance("rsa");
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new CryptoException (
                    "INTERNAL ERROR: can't find RSA or SHA1PRNG algorithm");
        }
        byte[] seed = new byte[128];
        // slurp up some low grade randomness
        new Random(new Date().getTime()).nextBytes(seed);
        rng.setSeed(seed);
        keyGen.initialize(keySize, rng);
    }
    /**
     * RSA key generator with default key size of 1024 bits.
     */
    public RSAKeyGen ()                         throws CryptoException {
        this (1024);
    }
    /**
     * @return algorithm name, in case you forget 
     */
    public String algorithm () {
        return "rsa";
    }
    /**
     * Generate an RSA key with the specified number of bits in the
     * modulus n.
     */
    public Key generate()                   throws CryptoException {
        KeyPair pair = keyGen.generateKeyPair();
        java.security.PrivateKey jcaPrivKey = pair.getPrivate();
        RSAPrivateCrtKeySpec privSpec;
        try {
            privSpec 
            = (RSAPrivateCrtKeySpec)jcaFactory.getKeySpec(
                                    jcaPrivKey, RSAPrivateCrtKeySpec.class);
        } catch (java.security.spec.InvalidKeySpecException ikse) {
            throw new CryptoException ("internal error? - " + ikse);
        }
        return new RSAKey (
            privSpec.getPrimeP(),         privSpec.getPrimeQ(),
            privSpec.getPublicExponent(), privSpec.getPrivateExponent());
    }

    public void initialize(int keysize) {
        keyGen.initialize (keysize, rng);
        
    }
    public void initialize() {
        initialize(1024);
    }
}
