/* TestBindingServer.java */
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
 * Tests the simpler features of a BindingServer.
 * 
 * @author Jim Dixon
 */

public class TestBindingServer extends TestCase implements StunConst {

    private KeyGenerator keyGen;
    private Random rng = new Random();

    /** keys private to the Server */
    private SecretKey userNameKey;
    private SecretKey passwordKey;
    /** the client's authentication key */
    private SecretKey clientKey;
    
    // XXX confusing mixture of XLattice abstractions and conventional
    // XXX InetAddress, Socket, ServerSocket
    private Inet4Address thisHost;
    private IPAddress clientAddr;
    private int clientPort;
    
    private IPAddress priAddr;
    private final int priPort;
    private final int secPort;
    
    private final Tls myTls;
    private       int authLevel;
    private final String serverKeyStoreName;
    private final String serverPasswd;
    private final char[] passphrase;
    private final String clientKeyStoreName;
    private final String clientPasswd;
    
    private EndPoint clientTlsEnd;
   
    private final TlsAddress serverTlsAddr;
    private EndPoint serverTlsEnd;
    private final Connector tlsCtr;
    private SecretServer secretServer;

    private BindingServer server;

    // from the SecretServer; should change whenever requested
    private byte[] userName;
    private byte[] password;
  
    // XXX only 0 and 1 should be used (priAddr + priPort and secPort),
    // XXX because we don't have a distinct secondary address
    //
    // XXX THIS IS NO LONGER TRUE - PLEASE FIX XXX
    private BindingSender[] senders;
    
    // CONSTRUCTOR, JuNIT SETUP AND TEARDOWN ////////////////////////
    public TestBindingServer (String name)       throws Exception {
        super(name);
        keyGen = KeyGenerator.getInstance("HmacSHA1");
        userNameKey = keyGen.generateKey();
        passwordKey = keyGen.generateKey();
   
        // TEMPORARY HACK junk.tls.init
        myTls               = new Tls();
        authLevel           = ANONYMOUS_TLS;
        serverKeyStoreName  = "test.server.private";
        serverPasswd        = "87654321";
        passphrase          = serverPasswd.toCharArray();
        clientKeyStoreName  = null;
        clientPasswd        = null;
        // END HACK
        
        thisHost = (Inet4Address)InetAddress.getLocalHost();
        priPort = 45678;
        secPort = priPort + 1;
        priAddr = new IPAddress (thisHost, priPort);

        KeyStore serverKeys = KeyStore.getInstance("JKS");
        serverKeys.load( new FileInputStream(serverKeyStoreName),
                         passphrase );
        serverTlsAddr = new TlsAddress(priAddr,
                authLevel, serverKeys, passphrase, true);
        serverTlsEnd  = new EndPoint (myTls, serverTlsAddr);
        tlsCtr  = myTls.getConnector(serverTlsAddr, true);

        clientPort = 0;       // EPHEMERAL PORT 
        clientAddr = new IPAddress (thisHost, clientPort);
        TlsAddress clientTlsAddr = new TlsAddress(clientAddr,
                authLevel, null, null, true);
        clientTlsEnd  = new EndPoint (myTls, clientTlsAddr);
    }

    public void setUp ()                        throws Exception {
        // server secret keys
        userName   = null;
        password   = null;
        // client's HMAC key
        clientKey  = null;

        // start the secret server
        // WORKING HERE
        secretServer   = new SecretServer(thisHost, priPort,
                        serverKeyStoreName, serverPasswd,
                        userNameKey, passwordKey, false);
        while (!secretServer.isRunning())
            Thread.sleep(1);

        // start four BindingSenders
        senders = new BindingSender[4];
        senders[0] = new BindingSender(thisHost, priPort);
        senders[1] = new BindingSender(thisHost, secPort);
        // the other two are null
        while ( (!senders[0].isRunning()) || (!senders[1].isRunning()) )
            Thread.sleep(1);

    }
    public void tearDown()                      throws Exception {
        if (secretServer != null && secretServer.isRunning()) {
            try {
                secretServer.close();         // does a join
            } catch (Throwable t) { /* ignore */ }
        }
        secretServer = null;
        // BindingServer
        if (server != null && server.isRunning()) {
            try {
                server.close();        
            } catch (Throwable t) {}
        }
        server = null;
        for (int i = 0; i < senders.length; i++) 
            if (senders[i] != null && senders[i].isRunning())
                try {
                    senders[i].close();
                } catch (Throwable t) { }
    }
    // UTILITIES ////////////////////////////////////////////////////
    /**
     * Get the username and password from the SecretServer using
     * a bare TCP/IP connection, ie, without TLS, and creates the
     * client's HMAC authentication key.
     */
    private void getUserNameAndPassword()       throws Exception {

        Connection knx    = tlsCtr.connect(clientTlsEnd, true);
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
    // TestBindingServer METHODS ////////////////////////////////////
    /**
     * Confirm that the MappedAddress, ResponseAddress, and
     * ChangedAddress attributes are correct, given a client
     * BindingRequest without authentication.
     *
     * XXX Inet4Address has equals() XXX
     */
    public boolean matchesMyAddress (Inet4Address a)
                                                throws Exception {
        byte[] myIPv4 = thisHost.getAddress();
        byte[] other  = a.getAddress();
        assertEquals (myIPv4.length, other.length);
        for (int i = 0; i < myIPv4.length; i++)
            if (myIPv4[i] != other[i])
                return false;
        return true;
    } 
    
    public BindingServer setUpServer(boolean authenticating)
                                                throws Exception {
        DatagramSocket bsSocket = senders[0].getSocket();
        assertNotNull(bsSocket);

        BindingServer server;
        if (authenticating)
            server = new BindingServer( bsSocket, 
                        thisHost, priPort, thisHost, secPort, 
                        userNameKey, passwordKey, false, senders, false);
        else
            server = new BindingServer( bsSocket, 
                        thisHost, priPort, thisHost, secPort, 
                        null, null, true, senders, false);
        return server;
    }

    public void testNoAuth()                    throws Exception {
        BindingServer server = setUpServer(false);

        // send a BindingRequest //////////////////////////
        DatagramSocket socket = new DatagramSocket();
        socket.setReuseAddress(true);
        socket.connect (thisHost, priPort);
        socket.setSoTimeout(50);    // on the same machine, so 50ms ample
        clientPort = socket.getLocalPort();

        StunMsg msg = new BindingRequest();
        byte[] serialized = new byte[msg.wireLength()];
        msg.encode(serialized);
        
        DatagramPacket outPkt = new DatagramPacket(serialized, 
                                    serialized.length, thisHost, priPort);
        socket.send(outPkt);
        
        // receive BindingResponse and validate ///////////
        byte[] buffer = new byte[256];
        DatagramPacket inPkt = new DatagramPacket(buffer, 256);
        socket.receive(inPkt);

        StunMsg  decodedMsg = StunMsg.decode(buffer);
        int msgType = decodedMsg.type;
        if (msgType == BINDING_ERROR_RESPONSE) {
            ErrorCode errCode = (ErrorCode) decodedMsg.get(0);
            fail( errCode.toString() );
        }
        assertEquals (BINDING_RESPONSE,  msgType);

        BindingResponse response = (BindingResponse) decodedMsg;
        byte[] msgID = msg.getMsgID();
       
        assertEquals (3,    response.size());
        for (int i = 0; i < response.size(); i++) {
            StunAttr attr = response.get(i);
            if (attr.type == MAPPED_ADDRESS) {
                assertTrue( matchesMyAddress (((AddrAttr)attr).getAddress()) );
                assertEquals( clientPort,    ((AddrAttr)attr).getPort());
            } else if (attr.type == SOURCE_ADDRESS) {
                assertTrue( matchesMyAddress (((AddrAttr)attr).getAddress()) );
                assertEquals( priPort,       ((AddrAttr)attr).getPort());
            } else {
                assertEquals(CHANGED_ADDRESS,attr.type);
                assertTrue( matchesMyAddress (((AddrAttr)attr).getAddress()) );
                assertEquals( secPort,       ((AddrAttr)attr).getPort());
            }
        }
        server.close(); 
    } 
    /** 
     * Client gets a secret (a username/password pair) and uses it.
     * Confirm that the message integrity attribute is correct.
     */
    public void testMessageIntegrity()          throws Exception {
        getUserNameAndPassword(); 
        assertNotNull("userName is null", userName);
        assertNotNull("password is null", password);
        
        BindingServer server = setUpServer(true);
        for (int n = 0; n < 16; n++) 
            if (server.isRunning()) 
               break;
            else 
                Thread.currentThread().sleep(20);
        assertTrue("couldn't start BindingServer", server.isRunning());
        
        // send a BindingRequest //////////////////////////
        DatagramSocket socket = new DatagramSocket();
        socket.setReuseAddress(true);
        socket.connect (thisHost, priPort);
        socket.setSoTimeout(500);    // on the same machine, so 50ms ample
        clientPort = socket.getLocalPort();

        StunMsg msg = new BindingRequest();
        msg.add(new UserNameAttr(userName));
        msg.add(new MessageIntegrity());
        byte[] serialized = new byte[msg.wireLength()];
        msg.encode(serialized);
        MessageIntegrity.setHMAC(serialized, clientKey);
        
        DatagramPacket outPkt = new DatagramPacket(serialized, 
                                    serialized.length, thisHost, priPort);
        socket.send(outPkt);
        
        // receive BindingResponse and validate ///////////
        byte[] buffer = new byte[256];
        DatagramPacket inPkt = new DatagramPacket(buffer, 256);
        // TIMES OUT XXX
        socket.receive(inPkt);

        BindingResponse decodedMsg = (BindingResponse) StunMsg.decode(buffer);
        assertEquals (BINDING_RESPONSE,  decodedMsg.type);
        byte[] msgID = msg.getMsgID();
       
        assertEquals (4,    decodedMsg.size());
        for (int i = 0; i < decodedMsg.size(); i++) {
            StunAttr attr = decodedMsg.get(i);
            if (attr.type == MAPPED_ADDRESS) {
                assertTrue( matchesMyAddress (((AddrAttr)attr).getAddress()) );
                assertEquals( clientPort,    ((AddrAttr)attr).getPort());
            } else if (attr.type == SOURCE_ADDRESS) {
                assertTrue( matchesMyAddress (((AddrAttr)attr).getAddress()) );
                assertEquals( priPort,       ((AddrAttr)attr).getPort());
            } else if (attr.type == CHANGED_ADDRESS) {
                assertTrue( matchesMyAddress (((AddrAttr)attr).getAddress()) );
                assertEquals( secPort,       ((AddrAttr)attr).getPort());
            } else {
                assertEquals(MESSAGE_INTEGRITY,    attr.type);
                assertTrue ( ((MessageIntegrity)attr)
                    .verify(buffer,
                        HEADER_LENGTH + decodedMsg.length(), 
                        clientKey));
            }
        }
        server.close(); 
    }

//  /** get a secret but don't necessarily use it */
//  public void testOptionalAuth()              throws Exception {
//      getUserNameAndPassword(); 
//  }
//  /** expect an error if client omits message integrity */
//  public void testMandatoryAuth()             throws Exception {
//      getUserNameAndPassword(); 
//  }

    private void doTestChangeRequest(int which) throws Exception {
        Inet4Address expectedAddr = thisHost;
        int          expectedPort = priPort;
        if ((which & CHANGE_IP) != 0)
            expectedAddr = thisHost;    // waste of time!
        if ((which & CHANGE_PORT) != 0)
            expectedPort = secPort;

        BindingServer server = setUpServer(false);

        // send a BindingRequest //////////////////////////
        DatagramSocket socket = new DatagramSocket();
        socket.setReuseAddress(true);
        // socket not connected
        socket.setSoTimeout(50);    // on the same machine, so 50ms ample
        clientPort = socket.getLocalPort();

        StunMsg msg = new BindingRequest();
        if (which != 0)
            msg.add( new ChangeRequest(which) );
        byte[] serialized = new byte[msg.wireLength()];
        msg.encode(serialized);
        
        DatagramPacket outPkt = new DatagramPacket(serialized, 
                                    serialized.length, thisHost, priPort);
        socket.send(outPkt);
        
        // receive BindingResponse and validate ///////////
        byte[] buffer = new byte[256];
        DatagramPacket inPkt = new DatagramPacket(buffer, 256);
        socket.receive(inPkt);

        BindingResponse decodedMsg = (BindingResponse) StunMsg.decode(buffer);
        assertEquals (BINDING_RESPONSE,  decodedMsg.type);
        byte[] msgID = msg.getMsgID();
       
        assertEquals (3,    decodedMsg.size());
        for (int i = 0; i < decodedMsg.size(); i++) {
            StunAttr attr = decodedMsg.get(i);
            if (attr.type == MAPPED_ADDRESS) {
                assertTrue( matchesMyAddress (((AddrAttr)attr).getAddress()) );
                assertEquals( clientPort,    ((AddrAttr)attr).getPort());
            } else if (attr.type ==  SOURCE_ADDRESS) {
                assertTrue( expectedAddr
                                .equals    (((AddrAttr)attr).getAddress()) );
                assertEquals( expectedPort, ((AddrAttr)attr).getPort() );
            } else {
                assertEquals(CHANGED_ADDRESS,attr.type);
                assertTrue( matchesMyAddress (((AddrAttr)attr).getAddress()) );
                assertEquals( secPort,       ((AddrAttr)attr).getPort());
            }
        } 
        server.close();
    }
    // WORKING HERE
    public void testChangeRequest()             throws Exception {
        //doTestChangeRequest( 0 );       // no change at all
        doTestChangeRequest( CHANGE_PORT );  // HERE <---
        // XXX Can't do these with current setup - no alternate address
        //doTestChangeRequest( IP_CHANGE );
        //doTestChangeRequest( IP_CHANGE + PORT_CHANGE);
    }
//  public void testErrorCodes()                throws Exception {
//  }
//  public void testUnknownAttributes()         throws Exception {
//  }
//  public void testReflectedFrom()             throws Exception {
//  }

    /////////////////////////////////////////////////////////////////
    // NOTE THAT VOVIDA EXTENSIONS ARE NOT TESTED ///////////////////
    /////////////////////////////////////////////////////////////////
    // These appear to be
    //   0x0021 XOR_ONLY
    //   0x8020 XOR_MAPPED_ADDRESS
    //   0x8022 SERVER_NAME
    //   0x8050 SECONDARY_ADDRESS
    // The RFC specifies that any non-standard attribute with a type
    // of 0x7fff or less should cause an error.  XOR_ONLY falls into
    // this category but we will ignore it instead.  That is, it is
    // treated like types whose value is 0x8000 or above.
}
