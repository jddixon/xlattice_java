/* TestServer.java */
package org.xlattice.protocol.stun;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.security.KeyStore;
import java.util.Random;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import junit.framework.*;

import org.xlattice.Acceptor;
import org.xlattice.Connection;
import org.xlattice.Connector;
import org.xlattice.EndPoint;
import org.xlattice.Protocol;
import org.xlattice.Transport;
import static org.xlattice.crypto.tls.TlsConst.*;
import org.xlattice.transport.IPAddress;
import org.xlattice.transport.tls.Tls;
import org.xlattice.transport.tls.TlsAddress;

import org.xlattice.util.StringLib;

/**
 * Emulates a simple client to test the STUN server.
 *
 * @author Jim Dixon
 */

public class TestServer extends TestCase implements StunConst {

    private static Random rng = new Random();

    private final Tls myTls;
    private final String   serverKeyStoreName;
    private final String   serverPasswd;
    private final char[]   passphrase;
    private final KeyStore serverKeys;
    private final String   clientKeyStoreName;
    private final String   clientPasswd;
    
    /** the client's authentication key */
    private SecretKey clientKey;
   
    // XXX confusing mixture of XLattice abstractions and conventional
    // XXX InetAddress, Socket, ServerSocket
    private final Inet4Address thisHost;
    private       Inet4Address priHost;
    private       Inet4Address secHost;
    private       IPAddress clientAddr;
    private       int clientPort;
    
    private       IPAddress priAddr;
    private final int priPort;
    private final int secPort;
    
    private       Server server;
    private       TlsAddress serverTlsAddr;
    private       EndPoint serverTlsEnd;
    
    private       Connector connector;
    private       EndPoint clientTlsEnd;

    // from the SecretServer; should change whenever requested
    private byte[] userName;
    private byte[] password;
 
    // CONSTRUCTOR, JuNIT SETUP AND TEARDOWN ////////////////////////
    public TestServer (String name)       throws Exception {
        super(name);
        
        priPort = Integer.parseInt(System.getProperty("STUN_PRIMARY_PORT"));
        secPort = Integer.parseInt(System.getProperty("STUN_SECONDARY_PORT"));

        thisHost = (Inet4Address)InetAddress.getLocalHost();

        myTls               = new Tls();
        serverKeyStoreName  = System.getProperty("STUN_SERVER_KEYSTORE");
        serverPasswd        = System.getProperty("STUN_SERVER_PASSWORD");
        passphrase          = serverPasswd.toCharArray();
        clientKeyStoreName  = null;
        clientPasswd        = null;
        
        serverKeys = KeyStore.getInstance("JKS");
        serverKeys.load( new FileInputStream(serverKeyStoreName),
                         passphrase );


    }

    public void setUp ()                        throws Exception {
        clientKey  = null;
        clientPort = 0;       // EPHEMERAL PORT 
    }
    public void tearDown()                      throws Exception {
        if (server != null) {
            try {
                server.close();         // does a join
            } catch (Throwable t) { /* ignore */ }
        }
        server = null;
    }
    // UTILITIES ////////////////////////////////////////////////////
    /**
     * Get the username and password from the SecretServer using
     * a bare TCP/IP connection, ie, without TLS, and creates the
     * client's HMAC authentication key.
     */
    private void getUserNameAndPassword()       throws Exception {
        assertNotNull(clientTlsEnd);
        Connection knx = connector.connect(clientTlsEnd, true);
        OutputStream outs = knx.getOutputStream();
        InputStream  ins  = knx.getInputStream();

        SharedSecretRequest req = new SharedSecretRequest();
        int reqLen = req.wireLength();
        byte[] outBuffer = new byte[reqLen];
        req.encode(outBuffer);
        outs.write(outBuffer, 0, reqLen);
        
        byte[] inBuf = new byte[256];
        ins.read(inBuf);
        StunMsg resp = StunMsg.decode(inBuf);
        userName  = resp.get(0).value;
        password  = resp.get(1).value;
        clientKey = new SecretKeySpec(password, "HmacSHA1");
        knx.close();
    } 

    /**
     */
    public Server setUpServer(boolean authenticating, int authLevel)    
                                                throws Exception {

        String pri = System.getProperty("STUN_PRIMARY_HOST");
        String sec = System.getProperty("STUN_SECONDARY_HOST");
        
        // NO LONGER USED
        priHost = (Inet4Address) InetAddress.getByName(pri);
        priAddr = new IPAddress (priHost, priPort);
        secHost = (Inet4Address) InetAddress.getByName(sec);
        // END NOT USED
       
        serverTlsAddr = new TlsAddress(priAddr,
                authLevel, serverKeys, passphrase, true);
        serverTlsEnd  = new EndPoint (myTls, serverTlsAddr);
        connector  = myTls.getConnector(serverTlsAddr, true);
        
        clientAddr = new IPAddress (thisHost, clientPort);
        TlsAddress clientTlsAddr = new TlsAddress(clientAddr,
                authLevel, null, null, true);
        clientTlsEnd  = new EndPoint (myTls, clientTlsAddr);

        // XXX It is impossible to specify OPTIONAL authentication
        // XXX in the current implementation.
       
        server = new Server(pri, sec, priPort, secPort, 
                            serverKeyStoreName, serverPasswd, 
                            authenticating, false);
        assertNotNull(server);

        // make sure the server is running 
        boolean serverRunning = false;
        for (int i = 0; i < 1024; i++) {
            if (server.isRunning()) {
                serverRunning = true;
                break;
            } else {
                Thread.sleep(1);
            }
        }
        if (!serverRunning) 
            fail("LOCAL SERVER IS NOT RUNNING");
        return server;
    } 

    public void testNoAuth()                    throws Exception {

        boolean authenticating = false;
        int     authLevel      = 0;             // don't care
      
        server = setUpServer(authenticating, authLevel);
        assertTrue("local server is not running",  server.isRunning() );
        
        // send a BindingRequest //////////////////////////
        // Must specify the local address or it may use priHost or secHost
        DatagramSocket socket = new DatagramSocket(0, thisHost);
        socket.setReuseAddress(true);
        socket.connect (priHost, priPort);
        socket.setSoTimeout(250);
        clientPort = socket.getLocalPort();
        assertTrue(socket.isConnected());

        StunMsg msg = new BindingRequest();
        byte[] serialized = new byte[msg.wireLength()];
        msg.encode(serialized);
        
        DatagramPacket outPkt = new DatagramPacket(serialized, 
                                    serialized.length, priHost, priPort);
        socket.send(outPkt);
        
        // receive BindingResponse and validate ///////////
        byte[] buffer = new byte[256];
        DatagramPacket inPkt = new DatagramPacket(buffer, 256);
        assertNotNull(inPkt);
        socket.receive(inPkt);

        BindingResponse decodedMsg = (BindingResponse) StunMsg.decode(buffer);
        assertEquals (BINDING_RESPONSE,  decodedMsg.type);
        byte[] msgID = msg.getMsgID();
       
        assertEquals (3,    decodedMsg.size());
        for (int i = 0; i < decodedMsg.size(); i++) {
            StunAttr attr = decodedMsg.get(i);
            if (attr.type == MAPPED_ADDRESS) {
                assertTrue( thisHost.equals (((AddrAttr)attr).getAddress()) );
                assertEquals( clientPort,   ((AddrAttr)attr).getPort());
            } else if (attr.type == SOURCE_ADDRESS) {
                assertTrue( priHost.equals  (((AddrAttr)attr).getAddress()) );
                assertEquals( priPort,      ((AddrAttr)attr).getPort());
            } else { 
                assertEquals(CHANGED_ADDRESS,attr.type);
                assertTrue( secHost.equals (((AddrAttr)attr).getAddress()) );
                assertEquals( secPort,     ((AddrAttr)attr).getPort());
            }
        } 
        server.close();  
    }
    /** 
     * Client gets a secret (a username/password pair) and uses it.
     * Confirm that the message integrity attribute is correct.
     */
    public void testAnonymousAuth()             throws Exception {

        server = setUpServer(true, ANONYMOUS_TLS);
        assertNotNull(server);
        getUserNameAndPassword(); 

        // send a BindingRequest //////////////////////////
        DatagramSocket socket = new DatagramSocket(0, thisHost);
        socket.setReuseAddress(true);
        socket.connect (priHost, priPort);
        socket.setSoTimeout(500);
        clientPort = socket.getLocalPort();

        StunMsg msg = new BindingRequest();
        msg.add(new UserNameAttr(userName));
        msg.add(new MessageIntegrity());
        byte[] serialized = new byte[msg.wireLength()];
        msg.encode(serialized);
        MessageIntegrity.setHMAC(serialized, clientKey);
        
        DatagramPacket outPkt = new DatagramPacket(serialized, 
                                    serialized.length, priHost, priPort);
        socket.send(outPkt);
        
        // receive BindingResponse and validate ///////////
        byte[] buffer = new byte[256];
        DatagramPacket inPkt = new DatagramPacket(buffer, 256);
        socket.receive(inPkt);

        BindingResponse decodedMsg = (BindingResponse) StunMsg.decode(buffer);
        assertEquals (BINDING_RESPONSE,  decodedMsg.type);
        byte[] msgID = msg.getMsgID();
       
        // XXX assume that attributes are in usual order
        assertEquals (4,    decodedMsg.size());
        StunAttr attr = decodedMsg.get(0);
        assertEquals(MAPPED_ADDRESS,   attr.type);
        assertTrue( thisHost.equals (((AddrAttr)attr).getAddress()) );
        assertEquals( clientPort,           ((AddrAttr)attr).getPort());
            
        attr = decodedMsg.get(1);
        assertEquals(SOURCE_ADDRESS,        attr.type);
        assertTrue( priHost.equals (((AddrAttr)attr).getAddress()) );
        assertEquals( priPort,              ((AddrAttr)attr).getPort());
        
        attr = decodedMsg.get(2);
        assertEquals(CHANGED_ADDRESS,       attr.type);
        assertTrue( secHost.equals (((AddrAttr)attr).getAddress()) );
        assertEquals( secPort,              ((AddrAttr)attr).getPort());
       
        attr = decodedMsg.get(3);
        assertEquals(MESSAGE_INTEGRITY,    attr.type);
        assertTrue ( ((MessageIntegrity)attr)
                .verify(buffer,
                    HEADER_LENGTH + decodedMsg.length(), 
                    clientKey));
        server.close(); 
    } 

}
