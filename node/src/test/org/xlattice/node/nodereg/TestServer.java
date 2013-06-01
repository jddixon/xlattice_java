/* TestServer.java */
package org.xlattice.node.nodereg;

import java.io.File;
import java.util.Date;
import java.util.Random;

import junit.framework.*;

import org.xlattice.DigSigner;
import org.xlattice.SigVerifier;
import org.xlattice.crypto.RSAKey;
import org.xlattice.crypto.RSAPublicKey;

/**
 * Tests the node registry server configuration code.
 *
 * @author Jim Dixon
 */
public class TestServer extends TestCase {

    private RSAPublicKey pubkey;
    private DigSigner    signer;
    private SigVerifier  verifier;

    private Random rng = new Random ( new Date().getTime() );

    public TestServer (String name) {
        super(name);
    }

    public void setUp () {
        pubkey   = null;
        signer   = null;
        verifier = null;
    }
    private void removeConfigFile () {
        File xmlFile = new File(Server.CONFIG_FILE);
        if (xmlFile.exists()) {
            if (!xmlFile.delete())           // oooo, we're wild folks
                System.err.println (Server.CONFIG_FILE
                        + " exists but I can't delete it");
        }
    }
    private void signAndVerify()                throws Exception {
        byte[] b = new byte[32];
        rng.nextBytes(b);
        signer.update(b);
        byte[] digSig = signer.sign();

        verifier.update(b);
        if (! verifier.verify(digSig) )
            fail("what the signer signed the verifier doesn't verify!");
    }
    public void testWithNoConfig()              throws Exception {
        removeConfigFile ();
        pubkey = Server.getPublicKey();
        assertNotNull(pubkey);
        signer = Server.getSigner();
        assertNotNull(signer);
        verifier = Server.getVerifier();
        assertNotNull(verifier);
        signAndVerify();
        // THIS SHOULD LEAVE A GOOD CONFIGURATION FILE
    }
    /**
     * Given a configuration file, run the same tests.
     * 
     * XXX This does not actually verify that the configuration file
     * XXX was good.  Add a boolean getter to System.java.
     */
    public void testWithConfigFile ()           throws Exception {
        File xmlFile = new File(Server.CONFIG_FILE);
        assertTrue (xmlFile.exists());

        pubkey = Server.getPublicKey();
        assertNotNull(pubkey);
        signer = Server.getSigner();
        assertNotNull(signer);
        verifier = Server.getVerifier();
        assertNotNull(verifier);
        signAndVerify();
    }
   
}
