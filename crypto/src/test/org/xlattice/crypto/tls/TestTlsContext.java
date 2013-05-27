/* TestTlsContext.java */
package org.xlattice.crypto.tls;

import static javax.net.ssl.SSLEngineResult.HandshakeStatus.*;

import java.io.IOException;
import java.security.*;
import javax.net.ssl.*;
import java.nio.*;

import junit.framework.*;

/**
 *
 * @author Jim Dixon
 */
public class TestTlsContext      extends TestCase implements TlsConst {

    private final SecureRandom rng;
    private final String ksFileName;
    private final char[] passphrase;
    private final char[] badPassphrase = "wrong".toCharArray();
    private TlsContext context;
    
    public TestTlsContext(String name)          throws Exception {
        super(name);
        rng = new SecureRandom();
        ksFileName 
                = System.getProperty("server.private.keystore.name");
        passphrase 
                = System.getProperty("server.password").toCharArray();
    }

    public void setUp ()                        throws Exception {
    }
    public void tearDown()                      throws Exception {
        context = null;
    }
    public void testConstructors()              throws Exception {
        try { 
            context = new TlsContext ("FOO", ANONYMOUS_TLS,
                    ksFileName, passphrase,
                    rng, "host name hint", 42);
            fail("constructor accepted FOO as protocol name");
        } catch (NoSuchAlgorithmException nsae) { /* success */ }
        try { 
            context = new TlsContext ("TLS", ANONYMOUS_TLS,
                    "NoSuchFile", passphrase,
                    rng, "host name hint", 42);
            fail("constructor accepted non-existent KeyStore file name");
        } catch (IOException ioe) { /* success */ }
        try { 
            context = new TlsContext ("TLS", ANONYMOUS_TLS,
                    ksFileName, badPassphrase,
                    rng, "host name hint", 42);
            fail("constructor accepted invalid passphrase");
        } catch (IOException ioe) { /* success */ }
        
        context = new TlsContext ("TLS", 47999, 
                    ksFileName, passphrase,
                    rng, "host name hint", 42);
        assertEquals( 47999,    context.getLevel() );
        assertEquals( "TLS",    context.getProtocol() );
        assertNotNull( context.getKeyStore() );
        assertEquals( rng,      context.getRNG() );
        assertEquals( "host name hint", context.getHostHint() );
        assertEquals( 42,       context.getPortHint() );
    }
}
