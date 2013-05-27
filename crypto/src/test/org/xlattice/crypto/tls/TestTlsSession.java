/* TestTlsSession.java */
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
public class TestTlsSession      extends TestCase implements TlsConst {

    private final String ksFileName;
    private final char[] ksPassphrase;
    private final String tsFileName;
    private final char[] tsPassphrase;
    
    private final SecureRandom rng;
    private final char[] badPassphrase = "wrong".toCharArray();
    private TlsContext context;
    private TlsSession session;
    
    public TestTlsSession(String name)          throws Exception {
        super(name);
        rng = new SecureRandom();
        ksFileName 
                = System.getProperty("server.private.keystore.name");
        ksPassphrase 
                = System.getProperty("server.password").toCharArray();
        tsFileName 
                = System.getProperty("client.public.keystore.name");
        tsPassphrase
                = System.getProperty("client.password").toCharArray();
    }

    public void setUp ()                        throws Exception {
    }
    public void tearDown()                      throws Exception {
        context = null;
        session = null;
    }
    public void testConstructors()              throws Exception {
        /* STUB */
    } 


    private void setAuths (int ctxAuth, int sessAuth, 
                              boolean isClient) throws Exception {
        context = new TlsContext ("TLS", ctxAuth,
                    ksFileName, ksPassphrase,
                    rng, "nameOfThisHost", 42);
        session = new TlsSession( context, sessAuth,
                    tsFileName, tsPassphrase, isClient);
    }
    public void testEffectiveAuthLevel()        throws Exception {
        // server that doesn't use dig certs //////////////
        setAuths(ANONYMOUS_TLS, ANONYMOUS_TLS, false);
        assertEquals( ANONYMOUS_TLS,        session.level );
        assertEquals( 1, session.trustManagers.length );
        assertTrue ( TRUST_ANYONE == session.trustManagers[0] );
                
        // server that offers a dig cert to clients but ///
        // doesn't authenticate clients ///////////////////
        setAuths(CA_SIGNED_SERVER_CERT | 0, 
                                        ANONYMOUS_TLS, false);
        assertEquals( CA_SIGNED_SERVER_CERT,session.level );
        assertTrue ( TRUST_ANYONE == session.trustManagers[0] );
        
        // server that does authenticate clients //////////
        setAuths(CA_SIGNED_SERVER_CERT | ANY_CLIENT_CERT,
                                        ANONYMOUS_TLS, false);
        assertEquals( CA_SIGNED_SERVER_CERT | ANY_CLIENT_CERT,    
                                            session.level );

        // client that doesn't expect dig cert from server
        setAuths(ANONYMOUS_TLS, ANONYMOUS_TLS, true);
        assertEquals( ANONYMOUS_TLS,        session.level );
        assertTrue ( TRUST_ANYONE == session.trustManagers[0] );

        // client expects CA_SIGNED dig cert from server, /
        // but doesn't authenticate itself ////////////////
        setAuths(ANONYMOUS_TLS, CA_SIGNED_SERVER_CERT, true);
        assertEquals( CA_SIGNED_SERVER_CERT,session.level );
        
        // client that expects CA_SIGNED dig cert from ////
        // server and authenticates itself ////////////////
        setAuths(ANONYMOUS_TLS, 
                CA_SIGNED_SERVER_CERT | CA_SIGNED_CLIENT_CERT, true);
        assertEquals( CA_SIGNED_SERVER_CERT | CA_SIGNED_CLIENT_CERT, 
                                            session.level);
    }
}
