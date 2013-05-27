/* RSAPublicKey.java */
package org.xlattice.crypto;

import java.math.BigInteger;
import org.xlattice.PublicKey;

/**
 *
 * @author Jim Dixon
 */
public class RSAPublicKey implements PublicKey {

    /** The public exponent e. */
    private final BigInteger e_;             
    
    /** The modulus n. */
    private final BigInteger n_;

    /** Whether the hashCode is cached. */
    private boolean hashIsCached;
    private int     cachedHash;

    // CONSTRUCTORS /////////////////////////////////////////////////
    /**
     * Create a public key with the modulus and exponent passed. 
     * These are cloned to improve security.
     *
     * @param n modulus 
     * @param e public exponent (usually 3, 17, or 2^16 + 1)
     */
    public RSAPublicKey (BigInteger n, BigInteger e) {
        n_ = new BigInteger(n.toByteArray());
        e_ = new BigInteger(e.toByteArray());
    }
    public RSAPublicKey (RSAPublicKey pubKey) {
        this ( pubKey.getModulus(), pubKey.getExponent() );
    }
    // PROPERTIES ///////////////////////////////////////////////////
    public BigInteger getModulus () {
        return n_;
    }

    public BigInteger getExponent () {
        return e_;
    }
    // INTERFACE PublicKey //////////////////////////////////////////
    public boolean equals (Object o) {
        if (o == null)
            return false;
        if (!(o instanceof RSAPublicKey))
            return false;
        RSAPublicKey otherKey = (RSAPublicKey) o;
        if (this == otherKey)
            return true;
        return otherKey.getModulus().equals(n_) 
            && otherKey.getExponent().equals(e_);
    }
    /**
     * Byte 0 ignored because it seems to always be 1.
     */
    public int hashCode () {
        if (hashIsCached)
            return cachedHash;

        byte[] myN = n_.toByteArray();
        int lenN   = myN.length;
        if (lenN > 5) 
            lenN = 5;
        byte[] myE = e_.toByteArray();
        int lenE   = myE.length;        // commonly 3
        if (lenE > 5)
            lenE = 5;
        byte[] b   = new byte[4];
        int common = lenN > lenE ? lenE : lenN;
        int i;
        for (i = 1; i < common; i++)
            b[i - 1] = (byte) (myN[i] ^ myE[i]);
        if (common < 5) {
            if (lenN >= lenE) {
                for ( ; i < lenN; i++)
                    b[i - 1] = myN[i];
            } else {
                for ( ; i < lenE; i++)
                    b[i - 1] = myE[i];
            }
            for ( ; i < 5; i++)
                b[i - 1] = 0;
        }
        cachedHash =   
                ((0xff & b[0]) << 24) | ((0xff & b[1]) << 16) |
                ((0xff & b[2]) <<  8) | ((0xff & b[3])      ) ;
        hashIsCached = true;
        return cachedHash;
    }
    // Key INTERFACE ////////////////////////////////////////////////
    public String algorithm() {
        return "rsa";
    }
}
