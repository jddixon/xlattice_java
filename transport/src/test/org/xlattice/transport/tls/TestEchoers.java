/* TestEchoers.java */
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
 * Tests software using EchoClient against EchoServer.
 *
 * @author Jim Dixon
 */

public class TestEchoers extends TestCase implements TlsConst {

    private final Random rng;
    
    /* client/server private/public file names */
    private final String cPrivName;
    private final String cPubName;
    private final String sPrivName;
    private final String sPubName;
    
    private       EchoServer server;
    
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
        debugLog.message("TestEchoers: " + s);
    }
    // CONSTRUCTORS /////////////////////////////////////////////////
    // XXX 2008-02-29 THIS HANGS 
    public TestEchoers (String name)          throws Exception {
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
        if (server != null) 
            try { 
                server.close();
            } catch (Throwable t) { /* ignore */ }
        if (serverSocket != null && !serverSocket.isClosed())
            try { serverSocket.close(); } catch (Throwable t) {}
        if (client != null) 
            try { 
                client.close(); 
            } catch (Throwable t) { /* ignore */ }
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
    } 

    public void doTlsTest(int level)                throws Exception {

        // start server ///////////////////////////////////
        server = new EchoServer (serverHost, serverPort, level,
                    sPrivName, serverPassphrase, 
                    cPubName,  clientPassphrase);
        while (!server.isRunning()) {
            Thread.currentThread().sleep(50);
        }
        
        Thread serverThread = server.getThread();
        assertTrue( serverThread.isAlive() );
        serverSocket = server.getServerSocket();
        assertNotNull(serverSocket);
        
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

        int limit = 128;        // longest time seen was 4100 ms
        int MS    = 100;
        int n;
        for (n = 0; n < limit && client.sizeResponses() < count; n++) {
            Thread.currentThread().sleep(MS);
        }
        if (n >= limit) {
            String msg = "timed out waiting " + (n * MS) 
                        + " ms for responses from server; expected " 
                        + count + " but "
                        + client.sizeResponses() + " received";
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
