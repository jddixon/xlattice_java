/* TestPassword.java */
package org.xlattice.protocol.stun;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Random;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import junit.framework.*;

import org.xlattice.util.StringLib;

/**
 * Tests whether STUN Passwords are constructed correctly.
 * 
 * @author Jim Dixon
 */

public class TestPassword extends TestCase {

    public final static int MSG_LEN = 512;

    private Random rng = new Random();
    private KeyGenerator keyGen;

    private byte[] userName;
    private byte[] password;
    
    public TestPassword (String name)           throws Exception{
        super(name);
        keyGen = KeyGenerator.getInstance("HmacSHA1");
    }

    public void setUp () {
        password = null;
        userName = null;
    }

    /**
     * Creates a random IPv4 address.
     */
    private Inet4Address addrGen()              throws Exception {
        byte[] addr = new byte[4];
        for (int i = 0; i < 4; i++)
            addr[i] = (byte) rng.nextInt(256);
        return (Inet4Address)Inet4Address.getByAddress(addr);
    }
    public void testHMACs()                 throws Exception {
        Inet4Address addr;
        SecretKey key;
        int port, offset;
        
        for (int i = 0; i < 16; i++) {
            addr = addrGen();
            port = rng.nextInt(65536);
            SecretKey uKey = keyGen.generateKey();
            SecretKey pKey = keyGen.generateKey();
            userName = new byte[UserName.BODY_LENGTH + UserName.HMAC_LENGTH];
            UserName.encode(addr, port, rng, uKey, userName, 0);
            assertTrue (UserName.verify(uKey, userName, 0));
            password = Password.generate(pKey, userName);
            assertTrue (Password.verify(pKey, userName, password));
        }
    }
}
