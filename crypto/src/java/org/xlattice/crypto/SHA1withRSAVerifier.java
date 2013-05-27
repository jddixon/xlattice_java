/* SHA1withRSAVerifier.java */
package org.xlattice.crypto;

import java.security.KeyFactory;
import java.security.Signature;
import java.security.spec.RSAPublicKeySpec;

/**
 * @author Jim Dixon
 **/

import org.xlattice.CryptoException;
import org.xlattice.PublicKey;
import org.xlattice.SigVerifier;

/**
 *
 */
public class SHA1withRSAVerifier extends SigVerifier {

    private static SHA1withRSAVerifier INSTANCE;
    private static Signature jcaSig;
    private static KeyFactory factory;
    
    // CONSTRUCTORS /////////////////////////////////////////////////
    public SHA1withRSAVerifier()            throws CryptoException {
        if (jcaSig == null) {
            try {
                jcaSig  = Signature.getInstance("SHA1withRSA");
                factory = KeyFactory.getInstance("rsa");
            } catch (java.security.NoSuchAlgorithmException nsae) {
                throw new CryptoException("runtime libraries missing? - "
                        + nsae);
            }
        }
    }
    
    // SigVerifier INTERFACE ////////////////////////////////////////
    public String getAlgorithm() {
        return "SHA1withRSA";
    }
    public void init (PublicKey key)   throws CryptoException {
        if (!(key instanceof RSAPublicKey))
            throw new CryptoException("key is not an RSAPublicKey");
        RSAPublicKey key_ = (RSAPublicKey) key;
        java.security.PublicKey jcaPubKey;
        try {
            jcaPubKey = factory.generatePublic (
                            new RSAPublicKeySpec(
                                key_.getModulus(), key_.getExponent()));
        } catch (java.security.spec.InvalidKeySpecException e) {
            throw new CryptoException ( "INTERNAL ERROR? - " + e);
        }
        try {
            jcaSig.initVerify(jcaPubKey);
        } catch (java.security.InvalidKeyException e) {
            throw new CryptoException ( "INTERNAL ERROR? - " + e);
        }
    }
    public SigVerifier update (byte[] data) throws CryptoException {
        if (data == null)
            throw new CryptoException("null data array");
        try {
            jcaSig.update(data);
        } catch (java.security.SignatureException e) {
            e.printStackTrace();
            throw new CryptoException ("INTERNAL ERROR? - " + e);
        }
        return this;
    }
    public SigVerifier update (byte[] data, int offset, int len) 
                                            throws CryptoException {
        if (data == null)
            throw new CryptoException("null data array");
        try {
            jcaSig.update(data, offset, len);
        } catch (java.security.SignatureException e) {
            e.printStackTrace();
            throw new CryptoException ("INTERNAL ERROR? - " + e);
        }
        return this;
    }
    public boolean verify(byte[] data)      throws CryptoException {
        boolean b;
        try {
            b = jcaSig.verify(data);
        } catch (java.security.SignatureException e) {
            throw new CryptoException ("INTERNAL ERROR? - " + e);
        }
        return b;
    }
    public boolean verify(byte[] data, int offset, int len)
                                            throws CryptoException {
        boolean b;
        try {
            b = jcaSig.verify(data, offset, len);
        } catch (java.security.SignatureException e) {
            throw new CryptoException ("INTERNAL ERROR? - " + e);
        }
        return b;
    }
}
