/* TestMessageIntegrity.java */
package org.xlattice.protocol.stun;

import java.net.Inet4Address;
import java.util.Random;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import junit.framework.*;

import org.xlattice.util.StringLib;

/**
 * Tests MessageIntegrity attribute methods for setting and 
 * verifying the message HMAC.
 *
 * @author Jim Dixon
 */

public class TestMessageIntegrity extends TestCase {

    private Random rng = new Random();
    private byte[] password;
    private SecretKey clientKey;
    private byte[] username;

    private StunMsg msg;
    private byte[]  serialized;

    public TestMessageIntegrity (String name)            throws Exception {
        super(name);
    }

    public void setUp () {
        password   = null;
        username   = null;
        msg        = null;
        serialized = null;
    }

    /**
     * Simplest authenticated binding request: BindingRequest 
     * + UserNameAttr + MessageIntegrity.
     */
    public void testSimpleBindingRequest()      throws Exception {
        for (int i = 0; i < 8; i++) {
            // represents the password obtained from a PasswordAttribute
            password = new byte[20];
            rng.nextBytes(password);
            clientKey = new SecretKeySpec(password, "HmacSHA1");
    
            username = new byte[UserName.BODY_LENGTH + UserName.HMAC_LENGTH];
            rng.nextBytes(username);
            
            msg = new BindingRequest();
            msg.add(new UserNameAttr(username));
            msg.add(new MessageIntegrity());
            assertEquals(2, msg.size());
    
            serialized = new byte[msg.wireLength()];
            msg.encode(serialized);
            MessageIntegrity.setHMAC(serialized, clientKey);
            
            // parse the message and verify the HMAC
            BindingRequest decodedMsg 
                        = (BindingRequest) StunMsg.decode(serialized);
            assertEquals (2,                        decodedMsg.size());
            MessageIntegrity mi = (MessageIntegrity)decodedMsg.get(1);
            assertTrue( mi.verify(serialized, clientKey) );
        } 
    }
}
