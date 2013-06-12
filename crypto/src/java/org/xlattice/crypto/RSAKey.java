/* RSAKey.java */
package org.xlattice.crypto;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.RSAPrivateCrtKeySpec;

import org.xlattice.CryptoException;
import org.xlattice.DigSigner;      // XXX experiment
import org.xlattice.Key;
import org.xlattice.PublicKey;

/**
 * An RSA key. This includes the essential RSA key materials (n, e, d)
 * and also the Chinese Remainder Theorem (CRT) values.  
 * 
 * This implementation does not make the private key separately 
 * available.
 * 
 * XXX This code is anything but efficient.
 *
 *
 * @author Jim Dixon
 **/

public class RSAKey implements Key {

    /** length of the key in bytes */
    private final int keyBytes;
    
    /** 
     * The public exponent e, normally 3, 17, or 65537.  
     *
     * e must be prime relative to [p-1]*[q-1]
     */
    private final BigInteger e_;             
    
    /** 
     * The modulus n.  Together with e, this is the public key.
     * The product of primes p and q.
     */
    private final BigInteger n;             // product of primes p and q

    /** 
     * The private exponent d, e^-1 mod ([p-1]*[q-1]).
     * Together with n, this is the (logical) private final key.
     */
    private final BigInteger d_;

    // values for Chinese Remainder Theorem ///////////////
    // these are here to aid in calculations, but must be kept
    // secret

    /** first prime factor of n */
    private final BigInteger p_;
    /** second prime factor of n */
    private final BigInteger q_;
    // exponent d mod (p-1)
    private final BigInteger dp;
    // exponent d mod (q-1)
    private final BigInteger dq;
    // CRT coefficient, q^-1 mod p
    private final BigInteger c;
  
    // JCA RSA PrivateKey
    private final PrivateKey jcaPrivKey;
    
    // CONSTRUCTORS /////////////////////////////////////////////////
    /** Pedantically implement the no-arg constructor. */
    private RSAKey () throws CryptoException {
        n = e_ = d_ = p_ = q_ = dp = dq = c = null;
        jcaPrivKey = null;
        keyBytes = -1;
    }
    /**
     * Construct an RSA key from the values passed.  The values passed
     * allow easy construction of the public and private keys.  The
     * public key is a pair of numbers, the modulus n and the public
     * exponent e.  n is the product of two prime numbers, p and q.
     * The private key consists of the modulus n and the private 
     * exponent d.
     *
     * The parameters are cloned to prevent their being tampered with.
     *
     * @param p prime number, a factor of the modulus
     * @param q prime number, the other factor of the modulus
     * @param e public exponent
     * @param d private exponent
     */
    public RSAKey (BigInteger p, BigInteger q, BigInteger e, BigInteger d) 
                                                throws CryptoException {
        p_ = new BigInteger(p.toByteArray());
        q_ = new BigInteger(q.toByteArray());
        e_ = new BigInteger(e.toByteArray());
        d_ = new BigInteger(d.toByteArray());

        n  = p_.multiply(q_);
        // XXX This doesn't work; on every occasion tested where I expected
        // a length of 128 it was actually 129.  Nevertheless tests 
        // otherwise succeed.
        keyBytes = n.toByteArray().length;
        dp = d_.mod(p_.subtract(BigInteger.ONE));
        dq = d_.mod(q_.subtract(BigInteger.ONE));
        c  = q_.modInverse(p_); 
        RSAPrivateCrtKeySpec spec = new RSAPrivateCrtKeySpec (
                                            n, e_, d_, p_, q_, dp, dq, c);
        KeyFactory factory;
        try {
            factory = KeyFactory.getInstance("rsa");
            jcaPrivKey = factory.generatePrivate(spec);
        } catch (java.security.NoSuchAlgorithmException nse) {
            throw new CryptoException("Java libraries not installed? - "
                    + nse);
        } catch (java.security.spec.InvalidKeySpecException ike) {
            throw new CryptoException("invalid parameter? - "
                    + ike);
        }
    }
    // PROPERTIES ///////////////////////////////////////////////////
    public BigInteger getP () {return p_; }
    public BigInteger getQ () {return q_; }
    public BigInteger getE () {return e_; }
    public BigInteger getD () {return d_; }
    
    // Key INTERFACE ////////////////////////////////////////////////

    /** 
     * Experiment: the digital signature algorithm is implemented 
     * inside the key.  This is likely to conform to actual usage:
     * users are most likely to use only one key for signing 
     * documents.  Only SHA1 digital signatures are currently 
     * supported.
     */
    private class RSASigner implements DigSigner {
        private final String digestName;
        private final String algorithmName;
        private final Signature jcaSig;
     
        /**
         * Create a signer for digital signatures using the 
         * digest algorithm specified.  This defaults to SHA1.
         *
         * @param name of message digest algorithm
         */
        RSASigner (String name)         throws CryptoException {
            if (name == null || name.length() == 0)
                name = "SHA1";
            digestName = name.toUpperCase();
            algorithmName = new StringBuffer(name)
                            .append("withRSA").toString();
            try {
                jcaSig = Signature.getInstance("SHA1withRSA");
            } catch (java.security.NoSuchAlgorithmException nsae) {
                throw new CryptoException(
                        "runtime libraries not installed? - " + nsae);
            }
            try {
                jcaSig.initSign(jcaPrivKey);
            } catch (java.security.InvalidKeyException ike) {
                throw new CryptoException(
                        "corrupt private key? - " + ike);
            }
        }
        public String getAlgorithm() {
            return algorithmName;
        }
        /** 
         * Returns the length of the digital signature in bytes.
         * XXX May need to be revisited ;-)
         *
         * @return the length of the digital signature generated 
         */
        public int length() {
            return keyBytes;
        }
        /**
         * Add the binary data referenced to any already processed by
         * the message digest part of the algorithm.
         */
        public DigSigner update (byte[] data)        throws CryptoException {
            if (data ==  null) 
                throw new CryptoException ("null update data");
            try {
                jcaSig.update(data);
            } catch (java.security.SignatureException se) {
                throw new CryptoException("update error - " + se);
            }
            return RSASigner.this;
        }
        public DigSigner update (byte[] data, int offset, int len)
                                                    throws CryptoException {
            if (data ==  null) 
                throw new CryptoException ("null update data");
            try {
                jcaSig.update(data, offset, len);
            } catch (java.security.SignatureException se) {
                throw new CryptoException("update error - " + se);
            }
            return RSASigner.this;
        }
                                                    
        /**
         * Generate a digital signature and implicitly reset().
         *
         * @return the digital signature as a byte array
         */
        public byte[] sign ()                   throws CryptoException {
            byte[] b;
            try {
                b = jcaSig.sign();
            } catch (java.security.SignatureException se) {
                throw new CryptoException("error signing - " + se);
            }
            return b;
        }
    }
    public String algorithm () {
        return "rsa";
    }
    public DigSigner getSigner(String digestName) 
                                        throws CryptoException {
        return new RSASigner(digestName);
    }
    /**
     * Construct a public key for external use.  The private values
     * are copied for security.
     */
    public PublicKey getPublicKey() {
        return new RSAPublicKey ( 
                new BigInteger(n .toByteArray()), 
                new BigInteger(e_.toByteArray()) );
    }

}
