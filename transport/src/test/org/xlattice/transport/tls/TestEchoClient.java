/* TestEchoClient.java */
package org.xlattice.transport.tls;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Random;
import java.net.ServerSocket;

import junit.framework.*;

import org.xlattice.Transport;
import org.xlattice.crypto.tls.TlsConst;
import org.xlattice.transport.tls.Tls;
import org.xlattice.util.StringLib;
import org.xlattice.util.NonBlockingLog;

/**
 * Tests software using TlsConnector (EchoClient) against SimpleTlsServer.
 *
 * @author Jim Dixon
 */

public class TestEchoClient extends TestCase implements TlsConst {

    private final Random rng;
    
    /* client/server private/public file names */
    private final String cPrivName;
    private final String cPubName;
    private final String sPrivName;
    private final String sPubName;
    
    private SimpleTlsServer server;
    
    private final InetAddress serverHost;
    private final int serverPort = 9999;
    private Thread serverThread;
    private ServerSocket serverSocket;

    private        EchoClient   client;
    
    private final InetAddress   clientHost;
    private final int           clientPort = 9998;
    
    private final String        clientPassphrase;
    private final String        serverPassphrase;

    private Thread clientThread;
    
    private Inet4Address addr;
    
    // LOGGING ////////////////////////////////////////////
    protected static NonBlockingLog debugLog 
                            = NonBlockingLog.getInstance("simple.log");
    protected void DEBUG_MSG(String s) {
        debugLog.message("TestEchoClient: " + s);
    }
    // CONSTRUCTORS /////////////////////////////////////////////////
    // XXX 2008-02-29 THIS HANGS 
    public TestEchoClient (String name)          throws Exception {
        super(name);
        rng = new Random();
        
        // these are used as a convenience 
        String serverIP = System.getProperty("STUN_PRIMARY_ADDR");
        String clientIP = System.getProperty("STUN_SECONDARY_ADDR");

        serverHost = InetAddress.getByName(serverIP);
        clientHost = InetAddress.getByName(clientIP);

        cPrivName = System.getProperty("client.private.keystore.name");
        cPubName  = System.getProperty("client.public.keystore.name");
        sPrivName = System.getProperty("server.private.keystore.name");
        sPubName  = System.getProperty("server.public.keystore.name");
   
        clientPassphrase = System.getProperty("client.password");
        serverPassphrase = System.getProperty("server.password");
    }
    
    public void setUp () {
        server = null;
        client = null; 
    }
    public void tearDown() {
        if (server != null) {
            try { 
                server.close();
            } catch (Throwable t) { /* ignore */ }
        }
        if (serverSocket != null && !serverSocket.isClosed()) {
            try { serverSocket.close(); } catch (Throwable t) {}
        }
        if (client != null) {
            try { 
                client.close(); 
            } catch (Throwable t) { /* ignore */ }
        }
    }

    public void testClientConstructor()         throws Exception {
        int level =  ANONYMOUS_TLS;
        client = new EchoClient (clientHost, 
                    serverHost, serverPort, level,
                    sPrivName, serverPassphrase, 
                    cPubName,  clientPassphrase);
        assertNotNull(client);
        while (!client.isRunning()) {
            Thread.currentThread().sleep(1);
        }
        Thread clientThread = client.getThread();
        assertTrue( clientThread.isAlive() );

        client.close();
        assertFalse( client.isRunning() );
        assertFalse( clientThread.isAlive() );
    } // GEEP

    public void doTlsTest(int level)                throws Exception {

        // start server ///////////////////////////////////
        server = new SimpleTlsServer (serverHost, serverPort, level,
                    sPrivName, serverPassphrase, 
                    cPubName,  clientPassphrase);
        while (!server.isRunning()) {
            Thread.currentThread().sleep(50);
        }
        
        Thread serverThread = server.getThread();
        assertTrue( serverThread.isAlive() );
        serverSocket = server.getServerSocket();
        assertNotNull(serverSocket);
        // XXX NOT an SSLServerSocket
//      String[] ciphers = serverSocket.getEnabledCipherSuites();
//      if (level == ANONYMOUS_TLS) {
//          assertEquals(1, ciphers.length);
//          assertEquals(TLS_ANONYMOUS_CIPHERS[0], ciphers[0]);
//      }
        
        // start client ///////////////////////////////////
        client = new EchoClient (clientHost, 
                    serverHost, serverPort, level,
                    sPrivName, serverPassphrase, 
                    cPubName,  clientPassphrase);
        while (!client.isRunning()) {
            Thread.currentThread().sleep(1);
        }
        Thread clientThread = client.getThread();
        assertTrue( clientThread.isAlive() );
        
        // play ball! /////////////////////////////////////
        int count = 5 + rng.nextInt(12);                      // so up to 16
        byte[][] messages = new byte[count][];
        for (int i = 0; i < count; i++) {
            messages[i] = new byte[ 16 + rng.nextInt(113) ];  // up to 128
            rng.nextBytes( messages[i] );
            client.enqueueMessage ( messages[i] );
        }
        // unnecessary, but
        byte[] endMarker = "quit".getBytes();
        System.arraycopy(endMarker, 0, messages[count - 1], 0, 
                endMarker.length);
        byte[][] responses = new byte[count][];

        int n;
        int limit = 100;
        for (n = 0; n < limit && client.sizeResponses() < count; n++) {
            Thread.currentThread().sleep(100);
        }
        if (n >= limit) {
            String msg = "timed out waiting for responses from server";
            DEBUG_MSG(msg);
            fail(msg);
        } else {
            for (int i = 0; i < count; i++) {
                byte[] response = client.dequeueResponse();
                assertNotNull (response);
                assertEquals( messages[i].length, response.length);
                for (int j = 0; j < messages[i].length; j++)
                    assertEquals(messages[i][j], response[j]);
            }
        }
        // shut down //////////////////////////////////////
        client.close();
        assertFalse( client.isRunning() );
        assertFalse( clientThread.isAlive() );
        server.close();
        assertFalse( server.isRunning() );
        assertFalse( serverThread.isAlive() );
    } // GEEP
  
    /** 
     * Test with neither client nor server using dig cert.  May
     * be OK with STUN.
     */
    public void testAnonymousTLS()              throws Exception {
        doTlsTest(ANONYMOUS_TLS);
    } // FOO

    /** 
     * Server accepts dig cert without CA signature.  Should be
     * OK with STUN.
     */
    public void testServerWithUnsignedCert()    throws Exception {
        doTlsTest( ANY_SERVER_CERT);
    }

    /**
     * Both client and server have dig certs without CA signature:
     * self-signed certs.  Presumably would be used with XLattice
     * peer-to-peer applications.
     */
    public void testBothWithUnsignedCerts()     throws Exception { 
        doTlsTest( ANY_CLIENT_CERT | ANY_SERVER_CERT );
    } 
}
