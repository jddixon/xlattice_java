/* Password.java */
package org.xlattice.protocol.stun;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.SecretKey;

import org.xlattice.CryptoException;

/**
 * In this implementation, the STUN password is calculated directly
 * from the encoded UserName, which is a 36-byte value including an
 * HMAC generated using the UserName secret key.  The password is
 * an HMAC-SHA1 calculated using the Password secret key.  We 
 * assume that in the SharedSecretResponse the UserName attribute
 * always precedes the Password attribute.
 *
 * @author Jim Dixon
 */
public class Password {

    public final static int LENGTH = 20;    // because it's from SHA1
    
    // CONSTRUCTORS /////////////////////////////////////////////////
    private Password() { }

    // SERIALIZATION ////////////////////////////////////////////////
    /**
     * Given the second STUN secret key and an encoded UserName,
     * returns the corresponding password, an HMAC-SHA1 computed
     * from the UserName using the secret.
     *
     * @param  key              STUN password key
     * @param  encodedUserName  byte array holding UserName
     * @return password         HMAC-SHA1 byte array
     */
    public static byte[] generate (SecretKey key, byte [] encodedUserName) 
                                                throws CryptoException {
        if (key == null)
            throw new IllegalArgumentException("null SecretKey");
        if (encodedUserName == null)
            throw new IllegalArgumentException("null UserName binary");
        if (encodedUserName.length 
                        != (UserName.BODY_LENGTH + UserName.HMAC_LENGTH))
            throw new IllegalArgumentException( 
                    "encoded UserName has wrong length: " 
                    + encodedUserName.length);
        byte[] data = null;
        try {
            Mac mac = Mac.getInstance(key.getAlgorithm());
            mac.init(key);
            data = mac.doFinal (encodedUserName); 
        } catch (NoSuchAlgorithmException nsae) {
            throw new CryptoException ( nsae.toString() );
        } catch (InvalidKeyException ike) {
            throw new CryptoException ( ike.toString() );
        }
        return data;
    }
    /**
     * @param key              STUN password key
     * @param encodedUserName  byte array holding UserName
     * @param password         byte array holding password
     */
    public static boolean verify(SecretKey key, byte[] encodedUserName,
                                byte[] password)  throws CryptoException {
        if (password.length != UserName.HMAC_LENGTH)
            return false;
        byte[]data = generate(key, encodedUserName);
        for (int i = 0; i < UserName.HMAC_LENGTH; i++)
            if (data[i] != password[i])
                return false;
        return true;
    }
}
