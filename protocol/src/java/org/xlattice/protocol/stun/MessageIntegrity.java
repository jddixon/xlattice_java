/* MessageIntegrity.java */
package org.xlattice.protocol.stun;

import java.io.IOException; 
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;

import org.xlattice.CryptoException;
import org.xlattice.protocol.TLV16;

/** 
 * Attribute added at the end of an authenticated STUN message.
 * This is a TLV16 of fixed length.
 *
 * @author Jim Dixon
 */
public class MessageIntegrity extends ValueAttr {

    public static final int HMAC_LENGTH = 20;
    public static final int MESSAGE_INTEGRITY_LENGTH = 24;

    public static final byte[] NULL_DIGEST = new byte[HMAC_LENGTH];
    
    /**
     * Constructs a MessageIntegrity attribute object with a dummy
     * digest.  The digest is calculated after the StunMsg is 
     * serialized and then written over the value passed here.
     */
    public MessageIntegrity (byte[] digest) {
        super (MESSAGE_INTEGRITY, digest);
        if (digest.length  != HMAC_LENGTH)
            throw new IllegalArgumentException (
                "MESSAGE_INTEGRITY length must be "
                + HMAC_LENGTH + " but is "
                + digest.length);
    }
    public MessageIntegrity () {
        super (MESSAGE_INTEGRITY, NULL_DIGEST);
    }
    /**
     * This MessageIntegrity attribute has been extracted from
     * the serialized message passed.  Verify that the HMAC is
     * correct.  
     *
     * @param msg  serialized StunMsg
     * @param key  client password used to verify the HMAC
     * @return whether verification succeeded
     */
    protected boolean verify (byte[] msg, SecretKey key) 
                                            throws CryptoException {
        return verify (msg, msg.length, key);
    }
    protected boolean verify (byte[] msg, int len, SecretKey key) 
                                            throws CryptoException {
        if (len <= 0 || msg.length < len)
            throw new IllegalArgumentException(
                "message length extends beyond end of buffer: " + len);
        byte[] myHMAC = null;
        int offset = len - MESSAGE_INTEGRITY_LENGTH;
        try {
            Mac mac = Mac.getInstance(key.getAlgorithm());
            mac.init(key);
            mac.update (msg, 0, offset);
            myHMAC = mac.doFinal (); 
        } catch (NoSuchAlgorithmException nsae) {
            throw new CryptoException ( nsae.toString() );
        } catch (InvalidKeyException ike) {
            throw new CryptoException ( ike.toString() );
        }
        if (value.length != myHMAC.length)
            return false;
        for (int i = 0; i < HMAC_LENGTH; i++)
            if (value[i] != myHMAC[i])
                return false;
        return true;
    }
    /**
     * Sets the value of the HMAC at the end of a serialized 
     * StunMsg using the client password as a SecretKey.
     *
     * @param msg  serialized StunMsg
     * @param len  true length of the message, including header
     * @param key  the client password used to generate the HMAC
     */
    protected static void setHMAC(byte[] msg, int len, SecretKey key)
                                        throws CryptoException {
        if (msg == null) 
            throw new IllegalArgumentException("null STUN message");
        if (len <= 0 || msg.length < len)
            throw new IllegalArgumentException(
                    "message length beyond buffer: " + len);
        if (len <= StunMsg.HEADER_LENGTH + MESSAGE_INTEGRITY_LENGTH)
            throw new IllegalArgumentException(
                    "StunMsg too short to have MessageIntegrity attribute");
       
        int offset = len - MESSAGE_INTEGRITY_LENGTH;
        TLV16 tlv = null;
        try {
            tlv = TLV16.decode(msg, offset);
        } catch (IOException ioe) {
            throw new CryptoException("INTERNAL ERROR: unexpected "
                    + ioe.toString());
        }
        if (tlv.type != MESSAGE_INTEGRITY)
            throw new IllegalArgumentException(
                    "expected MessageIntegrity as last attribute");
        
        // 20B HMAC-SHA1 //////////////////////////////////
        try {
            Mac mac = Mac.getInstance(key.getAlgorithm());
            mac.init(key);
            mac.update (msg, 0, offset);
            mac.doFinal (msg, offset + 4);  // don't overwrite TL !
        } catch (NoSuchAlgorithmException nsae) {
            throw new CryptoException ( nsae.toString() );
        } catch (InvalidKeyException ike) {
            throw new CryptoException ( ike.toString() );
        } catch (ShortBufferException sbe) {
            throw new IllegalStateException("impossible: " 
                    + sbe.toString());
        } 
    }
    /**
     * Length defaults to actual buffer length, which is not the
     * same as the length returned by msg.length().  You need to
     * add StunMsg.HEADER_LENGTH.
     */
    protected static void setHMAC(byte[] msg, SecretKey key) 
                                        throws CryptoException {
        setHMAC(msg, msg.length, key);
    }
}
