/* TestUserName.java */
package org.xlattice.protocol.stun;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Date;
import java.util.Random;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import junit.framework.*;

import org.xlattice.util.StringLib;

/**
 * Tests whether STUN UserNames are constructed correctly.
 * 
 * @author Jim Dixon
 */

public class TestUserName extends TestCase {

    public final static int MSG_LEN = 512;

    private Random rng = new Random();
    private KeyGenerator keyGen;

    private byte[] msg;
    
    public TestUserName (String name)           throws Exception{
        super(name);
        keyGen = KeyGenerator.getInstance("HmacSHA1");
    }

    public void setUp () {
        msg = new byte[MSG_LEN];
    }

    private Inet4Address addrGen()              throws Exception {
        byte[] addr = new byte[4];
        for (int i = 0; i < 4; i++)
            addr[i] = (byte) rng.nextInt(256);
        return (Inet4Address)Inet4Address.getByAddress(addr);
    }
    public void testStaticEncodeDecode()        throws Exception {
        Inet4Address addr;
        SecretKey key;
        int port, offset;
        
        for (int i = 0; i < 16; i++) {
            addr = addrGen();
            port = rng.nextInt(65536);
            key = keyGen.generateKey();
            offset = rng.nextInt ( MSG_LEN 
                    - (UserName.BODY_LENGTH + UserName.HMAC_LENGTH));
           
            // XXX Once every zillion test runs this will be off
            long msNowRounded = 600000L * ((new Date().getTime())/600000L);
            UserName.encode(addr, port, rng, key, msg, offset);

            assertTrue (UserName.verify(key, msg, offset));
            UserName uName = UserName.decode(key, msg, offset);
            assertEquals (msNowRounded, uName.ms);
           
            byte[] myBinaryAddr      = addr.getAddress();
            byte[] decodedBinaryAddr = uName.addr.getAddress();
            assertEquals(myBinaryAddr.length, decodedBinaryAddr.length);
            for (int j = 0; j < 4; j++)
                assertEquals(myBinaryAddr[j], decodedBinaryAddr[j]);
            
            assertEquals (port, uName.port);
        }
    }
}
