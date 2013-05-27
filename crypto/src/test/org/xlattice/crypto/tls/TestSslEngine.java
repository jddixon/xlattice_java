/* TestSslEngine.java */
package org.xlattice.crypto.tls;

import static javax.net.ssl.SSLEngineResult.HandshakeStatus.*;

import java.io.FileInputStream;
import java.security.*;
import javax.net.ssl.*;
import java.nio.*;

import junit.framework.*;

/**
 * The idea is to build a test case that uses SSLEngine the way
 * that we would like TlsEngine to behave, and then rework it to
 * actually use TlsEngine.  So we rely upon SSLEngine's correct
 * behaviour to build a good test framework, and then use that 
 * solid framework to build TlsEngine.
 * 
 * XXX NOTE that this approach worked on 32 bit processors but failed 
 * XXX on 64 bit AMD processors.
 *
 * Later note: the 'failure' appears to be in the test logic, which does not 
 * allow for the possibility that it may take more than one transmission
 * to send a packet.
 *
 * @author Jim Dixon
 */
public class TestSslEngine      extends TestCase implements TlsConst {

    private final SecureRandom rng;
    
    /* client/server private/public file names */
    private final String cPrivName;
    private final String cPubName;
    private final String sPrivName;
    private final String sPubName;
    
    private final String clientPassphrase;
    private final char[] clientPasswd;
    private final String serverPassphrase;
    private final char[] serverPasswd;

    private       KeyStore clientKeys;
    private       KeyStore clientTrusts;
    private       KeyStore serverKeys;
    private       KeyStore serverTrusts;

    private       KeyManagerFactory kmfC;
    private       KeyManagerFactory kmfS;
    
    private       TrustManagerFactory tmfC;
    private       TrustManagerFactory tmfS;
    
    private       SSLContext clientContext;
    private       SSLContext serverContext;

    private       SSLEngine clientEngine;
    private       SSLEngine serverEngine;

    private       SSLSession clientSession;
    private       SSLSession serverSession;

    /** plaintext buffers */
    private       ByteBuffer aOut, aIn, bOut, bIn;
    /** ciphertext buffers */
    private       ByteBuffer a2b, b2a;

    private       SSLEngineResult clientResult;
    private       SSLEngineResult serverResult;

    private       SSLEngineResult.HandshakeStatus clientHS;
    private       SSLEngineResult.HandshakeStatus serverHS;

    private final ByteBuffer empty = ByteBuffer.allocate(0);

    public TestSslEngine (String name)          throws Exception {
        super(name);

        rng       = new SecureRandom();
        
        cPrivName = System.getProperty("client.private.keystore.name");
        cPubName  = System.getProperty("client.public.keystore.name");
        sPrivName = System.getProperty("server.private.keystore.name");
        sPubName  = System.getProperty("server.public.keystore.name");
   
        clientPassphrase = System.getProperty("client.password");
        clientPasswd     = clientPassphrase.toCharArray();
        serverPassphrase = System.getProperty("server.password");
        serverPasswd     = serverPassphrase.toCharArray();
    }

    public void setUp ()                        throws Exception {
    }
    public void tearDown()                      throws Exception {
    }
    /**
     * Build two engines S and C.  Clock data through, confirm that
     * it gets from S to C successfully, and then back again 
     * successfully.  For fun, write the data as byte[] but check 
     * it as int[], using a view of the ByteBuffer.
     *
     * 
     */
    public void setEmUp()                       throws Exception {
    
        // set up client //////////////////////////////////
        clientKeys = KeyStore.getInstance("JKS");
        clientKeys.load(new FileInputStream(cPrivName), clientPasswd);
        clientTrusts = KeyStore.getInstance("JKS");
        clientTrusts.load(new FileInputStream(sPubName), serverPasswd);
        
        kmfC = KeyManagerFactory.getInstance("SunX509");
        kmfC.init( clientKeys, clientPasswd);
        tmfC = TrustManagerFactory.getInstance("SunX509");
        tmfC.init(clientTrusts);

        clientContext = SSLContext.getInstance("TLS");
        clientContext.init( kmfC.getKeyManagers(), 
                            tmfC.getTrustManagers(), rng);
        clientEngine = clientContext.createSSLEngine("client", 1000);
        clientEngine.setUseClientMode(true);
        clientEngine.setEnabledCipherSuites(TLS_ANONYMOUS_CIPHERS);
        
        clientSession = clientEngine.getSession();
        int appBufSize = clientSession.getApplicationBufferSize();
        int pktBufSize = clientSession.getPacketBufferSize();
        aOut = ByteBuffer.allocate(appBufSize);
        aIn  = ByteBuffer.allocate(appBufSize);
        a2b  = ByteBuffer.allocate(pktBufSize);
        
        // set up server //////////////////////////////////
        serverKeys = KeyStore.getInstance("JKS");
        serverKeys.load(new FileInputStream(sPrivName), serverPasswd);
        serverTrusts = KeyStore.getInstance("JKS");
        serverTrusts.load(new FileInputStream(cPubName), clientPasswd);
        
        kmfS = KeyManagerFactory.getInstance("SunX509");
        kmfS.init( serverKeys, serverPasswd);
        tmfS = TrustManagerFactory.getInstance("SunX509");
        tmfS.init(serverTrusts);

        serverContext = SSLContext.getInstance("TLS");
        serverContext.init( kmfS.getKeyManagers(), 
                            tmfS.getTrustManagers(), rng);
        serverEngine = serverContext.createSSLEngine("server", 70000);
        serverEngine.setUseClientMode(false);
        serverEngine.setEnabledCipherSuites(TLS_ANONYMOUS_CIPHERS);
        serverEngine.setNeedClientAuth(false);

        serverSession = serverEngine.getSession();
        assertEquals(appBufSize, serverSession.getApplicationBufferSize());
        assertEquals(pktBufSize, serverSession.getPacketBufferSize());
        bOut = ByteBuffer.allocate(appBufSize);
        bIn  = ByteBuffer.allocate(appBufSize);
        b2a  = ByteBuffer.allocate(pktBufSize);


    }
    public boolean handshakeStep(final int step)
                                                throws Exception {
        boolean finished = false;
        
        /** client and server handshake status */
        
        // NEED_WRAP means server has more data to send
        if ((serverHS != NEED_WRAP) && 
                ((clientHS ==  NEED_TASK) || (clientHS ==  NEED_WRAP)) ) {
            // A SEND ///////////////////////////////
            a2b.clear();
            clientResult = clientEngine.wrap(empty, a2b);
            assertEquals(SSLEngineResult.Status.OK, 
                                    clientResult.getStatus());
            assertEquals (0, clientResult.bytesConsumed());
            assertTrue ( 
                    step + ": empty -> a2b wrap produced zero bytes",
                    clientResult.bytesProduced() > 0);
            clientHS = clientResult.getHandshakeStatus();
            switch (clientHS) {
                case FINISHED:
                    return true;
                case NEED_UNWRAP:
                    /* normal: waiting for reply from server */
                case NEED_WRAP:
                    /* client has another packet to send */
                    break;
                case NEED_TASK:
                case NOT_HANDSHAKING:
                default:
                    fail("step " + step 
                    + ", after client send: unexpected handshaking status " 
                            + clientHS);
            }
            a2b.flip();
            
            // B RECEIVE ////////////////////////////
            bIn.clear();
            serverResult = serverEngine.unwrap(a2b, bIn);
            assertEquals(SSLEngineResult.Status.OK, 
                                    serverResult.getStatus());
            assertEquals(0, serverResult.bytesProduced());
            serverHS = serverResult.getHandshakeStatus();
            switch (serverHS) {
                case FINISHED:
                    return true;
                case NEED_UNWRAP:
                    /* wait for more data from client*/
                case NEED_WRAP:
                    /* server has data to send */
                    break;
                case NEED_TASK: 
                    Runnable task;
                    while ((task = serverEngine.getDelegatedTask()) != null) 
                        task.run();
                    break;
                case NOT_HANDSHAKING:
            }
        } 

        // NEED_WRAP means more data to send
        if ( (clientHS != NEED_WRAP) &&
             ((serverHS ==  NEED_TASK) || (serverHS ==  NEED_WRAP)) ) {
            // B SEND ///////////////////////////////
            b2a.clear();
            serverResult = serverEngine.wrap(empty, b2a); 
            assertEquals(SSLEngineResult.Status.OK, 
                                    serverResult.getStatus());
            assertEquals (0, serverResult.bytesConsumed());
            assertTrue ("step " + step 
                                + ": empty -> b2a wrap produced zero bytes", 
                    serverResult.bytesProduced() > 0 );
            serverHS = serverResult.getHandshakeStatus();
            switch (serverHS) {
                case FINISHED:
                    /* OK; now let client finish */
                case NEED_UNWRAP:
                    /* normal: waiting for reply from client */
                case NEED_WRAP:
                    /* server has another packet to send */
                    break;
                case NEED_TASK:
                case NOT_HANDSHAKING:
                default:
                    fail("step " + step 
                    + ", after server send: unexpected handshaking status " 
                            + serverHS);
            }
            b2a.flip();
    
            // A RECEIVE ////////////////////////////
            aIn.clear();
            clientResult = clientEngine.unwrap(b2a, aIn);
            assertEquals(SSLEngineResult.Status.OK, 
                                    clientResult.getStatus());
            assertEquals(0, clientResult.bytesProduced());
            clientHS = clientResult.getHandshakeStatus();
            switch (clientHS) {
                case FINISHED:
                    return true;
                case NEED_TASK: 
                    Runnable task;
                    while ((task = clientEngine.getDelegatedTask()) != null) 
                        task.run();
                    break;
                case NEED_UNWRAP:
                    /* need another message from the server */
                    break;
                case NEED_WRAP:
                case NOT_HANDSHAKING:
                    System.out.println("step " + step 
                + ", after client receive: unexpected client handshake status " 
                            + clientHS);
            }
        } 

        return finished;
    }
    public void doHandshake()                   throws Exception {

        // START HANDSHAKE ////////////////////////////////
        clientHS = NEED_WRAP;
        serverHS = NEED_UNWRAP;
        int n;
        boolean finished = false;
        for (n = 1; n < 6 && !finished ; n++)
            finished = handshakeStep(n);
        assertTrue(finished);
        assertEquals( NOT_HANDSHAKING,
                clientEngine.getHandshakeStatus());
        assertEquals( NOT_HANDSHAKING,
                serverEngine.getHandshakeStatus());
    }
    public void doEchoTest()                    throws Exception {
        rng.nextBytes(aOut.array());
        rng.nextBytes(bOut.array());

        // A SEND ///////////////////////////////
        a2b.clear();
        assertEquals(0,                     aOut.position());
        assertEquals(aOut.array().length,   aOut.limit());
        clientResult = clientEngine.wrap(aOut, a2b);
        assertEquals(SSLEngineResult.Status.OK, 
                                clientResult.getStatus());
        assertEquals (aOut.position(), clientResult.bytesConsumed());
        a2b.flip();

        // B RECEIVE ////////////////////////////
        bIn.clear();
        serverResult = serverEngine.unwrap(a2b, bIn);
        assertEquals(SSLEngineResult.Status.OK, 
                                serverResult.getStatus());
        assertEquals(bIn.position(), serverResult.bytesProduced());
        bIn.flip();
        assertEquals (aOut.position(), bIn.limit());
        for (int i = 0; i < aOut.position(); i++) 
            assertEquals(aOut.array()[i],   bIn.array()[i]);

        // B SEND ///////////////////////////////
        bOut.clear();
        bOut.put ( bIn.array() );
        bOut.flip();

        b2a.clear();
        assertEquals(0,                     bOut.position());
        assertEquals(bOut.array().length,   bOut.limit());
        serverResult = serverEngine.wrap(bOut, b2a);
        assertEquals(SSLEngineResult.Status.OK, 
                                serverResult.getStatus());
        assertEquals (bOut.position(), serverResult.bytesConsumed());
        b2a.flip();
        
        // A RECEIVE ////////////////////////////
        aIn.clear();
        clientResult = clientEngine.unwrap(b2a, aIn);
        assertEquals(SSLEngineResult.Status.OK, 
                                clientResult.getStatus());
        assertEquals(bOut.position(), clientResult.bytesProduced());
        aIn.flip();
        assertEquals (bOut.position(), aIn.limit());
        for (int i = 0; i < aIn.limit(); i++) 
            assertEquals(bOut.array()[i],   aIn.array()[i]);
    }
    /**
     * Close both half-circuits (a2b and b2a).  This is a correct
     * order for closing the half-circuits.  Notice the points at
     * which the booleans can be asserted to be true.
     */
    public void shutEmDown()                    throws Exception {
        
        // CLOSE A -> B HALF-CIRCUIT //////////////////////
        
        // sends a close_notify as required by RFC 2246 (TLS)
        clientEngine.closeOutbound();
        a2b.clear();
        clientResult = clientEngine.wrap(empty, a2b);
        assertEquals(SSLEngineResult.Status.CLOSED, 
                                clientResult.getStatus());
        // close_notify is in "netward" buffer 
        assertTrue( clientResult.bytesProduced() > 0 );
        assertTrue( clientEngine.isOutboundDone() );
        a2b.flip();
       
        bIn.clear();
        serverResult = serverEngine.unwrap(a2b, bIn);
        // when the server receives the close_notify, it closes
        assertEquals(SSLEngineResult.Status.CLOSED, 
                                serverResult.getStatus());
        serverEngine.closeInbound();
        assertTrue( serverEngine.isInboundDone() );
        
        // CLOSE B -> A HALF-CIRCUIT //////////////////////
        serverEngine.closeOutbound();
        b2a.clear();
        serverResult = serverEngine.wrap(empty, b2a);
        assertEquals(SSLEngineResult.Status.CLOSED, 
                                serverResult.getStatus());
        assertTrue( serverResult.bytesProduced() > 0 );
        assertTrue( serverEngine.isOutboundDone() );
        b2a.flip();
      
        aIn.clear();
        clientResult = clientEngine.unwrap(b2a, aIn);
        // when the client receives the close_notify, it closes
        assertEquals(SSLEngineResult.Status.CLOSED, 
                                clientResult.getStatus());
        clientEngine.closeInbound();

        assertTrue( clientEngine.isInboundDone() );
    }
    public void testAPair()                     throws Exception {
        setEmUp();
        doHandshake();
        doEchoTest();
        shutEmDown();        
    }
}
