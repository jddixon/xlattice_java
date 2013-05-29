/* UserName.java */
package org.xlattice.protocol.stun;

import java.net.Inet4Address;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.Date;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;

import org.xlattice.CryptoException;

/**
 * Serialized, a UserName looks like
 *
 *     4B      4B    2B      6B           20B
 *  +---+---+---+---+---+---+---+---+---+- - -+---+
 *  |   t   |  addr |port|  salt    |  HMAC-SHA1  |
 *  +---+---+---+---+---+---+---+---+---+- - -+---+
 * 
 * where <b>t</b> is the time rounded down to the nearest ten
 * minutes, <b>addr</b> is the IPv4 address, and <b>port</b> is
 * the port number; all three quantities are in big-endian form.
 * <b>salt</b> is a 6-byte random number and <b>HMAC-SHA1</b> is
 * computed over these four fields (16 bytes in total) using the
 * usernameSecret.
 *
 * A STUN UserName formatted like this is suitable for use in a
 * stateless server.
 *
 * @author Jim Dixon
 */
public class UserName {

    public static final int BODY_LENGTH = 16;
    public static final int HMAC_LENGTH = 20;

    // INSTANCE VARIABLES ///////////////////////////////////////////
    public final long ms;
    public final Inet4Address addr;
    public final int port;

    // CONSTRUCTORS /////////////////////////////////////////////////
    public UserName(Inet4Address addr, int port) {
        this( new Date().getTime(), addr, port);
    }
    private UserName(long ms, Inet4Address addr, int port) {
        this.ms = ms;
        if (addr == null)
            throw new IllegalArgumentException("null IPv4 address");
        this.addr = addr;
        if ( port < 0 || 65535 < port)
            throw new IllegalArgumentException(
                    "port number out of range: " + port);
        this.port = port;
    }
    // SERIALIZATION ////////////////////////////////////////////////
    public static UserName decode (SecretKey key, 
                    byte[]msg, int offset) throws CryptoException {
        if (!verify(key, msg, offset))
            throw new CryptoException("UserName HMAC verification failed");
    
        // the 4B time field holds a time in seconds (rounded to the
        // nearest ten minutes), so multiply by 1000 to get ms
        long ms = (long)( ((0xff & msg[offset++]) << 24)
                        | ((0xff & msg[offset++]) << 16)
                        | ((0xff & msg[offset++]) <<  8)
                        | ((0xff & msg[offset++])      )) * 1000L;
       
        
        byte[] addr = new byte[4];
        System.arraycopy(msg, offset, addr, 0, 4);
        Inet4Address ipV4 = null;
        try { 
            ipV4 = (Inet4Address)Inet4Address.getByAddress(addr);
        } catch (java.net.UnknownHostException uhe) {
            /* assume impossible from byte[] address */
        }
        offset += 4;

        int port =  ((0xff & msg[offset++]) << 8) 
                   | (0xff & msg[offset++]);

        return new UserName (ms, ipV4, port);
    }
    /**
     * Create a username and write it onto a buffer.
     *
     * @param addr   IPv4 address
     * @param port   port number
     * @param rng    random number generator (should be SecureRandom)
     * @param key    UserName secret key used for HMACs
     * @param msg    buffer to write into
     * @param offset where to start writing
     */
    private static void encode (long ms, 
            Inet4Address addr, int port, 
            Random rng, SecretKey key,
            byte[] msg, int offset)         throws CryptoException {
        if (offset + BODY_LENGTH + HMAC_LENGTH > msg.length)
            throw new IllegalArgumentException(
                    "buffer too short for UserName");
        final int initialOffset = offset;
        
        // 4B time field //////////////////////////////////
        long sec = (ms / 600000L) * 600L;
        msg[offset++] = (byte)(sec >> 24);
        msg[offset++] = (byte)(sec >> 16);
        msg[offset++] = (byte)(sec >>  8);
        msg[offset++] = (byte)(sec      );
        
        // 4B IP address //////////////////////////////////
        System.arraycopy(addr.getAddress(), 0, msg, offset, 4);
        offset += 4;
        
        // 2B big-endian port /////////////////////////////
        msg[offset++] = (byte)(port >> 8);
        msg[offset++] = (byte) port;
        
        // 6B salt ////////////////////////////////////////
        byte[] salt = new byte[6];
        rng.nextBytes(salt);
        System.arraycopy(salt, 0, msg, offset, 6);
        offset += 6;
        
        // 20B HMAC-SHA1 //////////////////////////////////
        try {
            Mac mac = Mac.getInstance(key.getAlgorithm());
            mac.init(key);
            mac.update (msg, initialOffset, BODY_LENGTH);
            mac.doFinal (msg, offset); 
        } catch (NoSuchAlgorithmException nsae) {
            throw new CryptoException ( nsae.toString() );
        } catch (InvalidKeyException ike) {
            throw new CryptoException ( ike.toString() );
        } catch (ShortBufferException sbe) {
            throw new IllegalStateException("impossible: " 
                    + sbe.toString());
        }
    }
    public void encode (Random rng, SecretKey key, byte[] msg, int offset) 
                                            throws CryptoException {
        encode (ms, addr, port, rng, key, msg, offset);
    }
    public static void encode ( 
            Inet4Address addr, int port, 
            Random rng, SecretKey key,
            byte[] msg, int offset)         throws CryptoException {
        encode ( new Date().getTime(), addr, port, rng, key, msg, offset);
    }

    /**
     * 
     */
    public static byte[] generate (Inet4Address addr, int port, 
            Random rng, SecretKey key)       throws CryptoException {
        byte[] buffer = new byte[ BODY_LENGTH + HMAC_LENGTH ];
        encode (addr, port, rng, key, buffer, 0);
        return buffer;
    }
    public static boolean verify(SecretKey key, byte[] msg, int offset) 
                                            throws CryptoException {
        if (offset + BODY_LENGTH + HMAC_LENGTH > msg.length)
            return false;
        byte[] hmac;
        try {
            Mac mac = Mac.getInstance(key.getAlgorithm());
            mac.init(key);
            mac.update (msg, offset, BODY_LENGTH);
            hmac = mac.doFinal (); 
        } catch (NoSuchAlgorithmException nsae) {
            throw new CryptoException ( nsae.toString() );
        } catch (InvalidKeyException ike) {
            throw new CryptoException ( ike.toString() );
        } 
        if (hmac.length != HMAC_LENGTH)
            throw new IllegalStateException("invalid HMAC length: " 
                    + hmac.length);
        offset += BODY_LENGTH;
        for (int i = 0; i < HMAC_LENGTH; i++)
            if ( msg[offset++] != hmac[i] )
                return false;
        return true;
    }
}
