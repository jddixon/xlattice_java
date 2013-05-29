/* Testjava */
package org.xlattice.protocol.stun;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.Socket;
import java.util.Random;

import junit.framework.*;

import org.xlattice.util.StringLib;

/**
 * This currently (as of 2006-02-09) tests only the BindingRequest/
 * BindingResponse messages.  The BindingErrorResponse tests must 
 * wait until we have our own external server running.
 *
 * @author Jim Dixon
 */

public class TestStunMsg extends TestCase implements StunConst {

    private Random rng = new Random();

    private StunMsg msg;
    private byte[]  serialized;
    private final Inet4Address serverAddr;
    private final byte[] numericServerAddress; 
    private final int serverPort = STUN_SERVER_PORT;
    
    // fake alternative address and port for server
    private final Inet4Address altAddr;
    private final byte[] numericAltAddress;
    private final int altPort = serverPort + 1;
    
    // fake CLIENT public address
    private final byte[] numericDummyAddr = {
        (byte)1, (byte)2, (byte)3, (byte)4 };
    private final Inet4Address dummyPublicAddress;
    private final int dummyPort = 7890; 
    /**
     * Most servers return three attrs, {Mapped,Source,Changed}Address,
     * in that order, in response to a BindingRequest.
     * 
     * xten.net and ams-ix.net return five attrs, the extra two being
     * XorMappedAddress and ServerAddress in that order.
     */
    public TestStunMsg (String name)            throws Exception {
        super(name);

        // CONFIGURE A STUN SERVER FOR LIVE FIRE EXERCISES //////////
        // server names from http://wwww.voip-info.org/wiki-STUN.  Softjoys
        // is in Russia.
        serverAddr = (Inet4Address) Inet4Address
                    //.getByName("stun.fwdnet.net");    // WORKS, tcp in DNS
                    //.getByName("stun01.sipphone.com");// WORKS, DNS has both
                    .getByName("stun.softjoys.com");  // WORKS, DNS has both
                    //.getByName("stun.voipbuster.com");// WORKS, NO DNS
                    //.getByName("stun.voxgratia.org"); // WORKS, BAD DNS
                    //.getByName("stun.xten.net");      // WORKS, no tcp in DNS
                    //.getByName("stun1.noc.ams-ix.net"); // WORKS, no tcp
                    
                    // currently this code
                    //.getByName("72.44.80.208"); // stun.xlattice.org, udp
        
        numericServerAddress = serverAddr.getAddress();
        
        // this is a bit dodgey!  FAKE alternative server address/port.
        // Convention seems to be that the alternate port and address
        // are 1 down from real.
        numericAltAddress = new byte[4];
        System.arraycopy (numericServerAddress, 0, numericAltAddress, 0, 4);
        numericAltAddress[3] = (byte) (numericAltAddress[3] - 1);
        altAddr = (Inet4Address)Inet4Address
            .getByAddress(numericAltAddress);
        
        dummyPublicAddress   = (Inet4Address)Inet4Address
                                .getByAddress(numericDummyAddr);
    }

    public void setUp () {
        msg        = null;
        serialized = null;
    }

    /**
     * Bare BindingRequest.
     */
    public void testBindingRequest()            throws Exception {
        msg = new BindingRequest();
        assertEquals (BINDING_REQUEST,  msg.type);
        assertEquals (0,                        msg.length());
        assertEquals (0,                        msg.size());
        assertEquals (HEADER_LENGTH,    msg.wireLength());

        serialized = new byte[msg.wireLength()];
        msg.encode(serialized);

        BindingRequest decodedMsg = (BindingRequest) StunMsg.decode(serialized);
        assertEquals (BINDING_REQUEST,  decodedMsg.type);
        assertEquals (0,                        decodedMsg.length());
        assertEquals (0,                        decodedMsg.size());
        assertEquals (HEADER_LENGTH,    decodedMsg.wireLength());

    }
    /**
     * Bare BindingResponse, which is actually illegal.
     */
    public void testBindingResponse()           throws Exception {
        msg = new BindingResponse();
        assertEquals (BINDING_RESPONSE,  msg.type);
        assertEquals (0,                        msg.length());
        assertEquals (0,                        msg.size());
        assertEquals (HEADER_LENGTH,    msg.wireLength());

        serialized = new byte[msg.wireLength()];
        msg.encode(serialized);

        BindingResponse decodedMsg = (BindingResponse) StunMsg.decode(serialized);
        assertEquals (BINDING_RESPONSE,  decodedMsg.type);
        assertEquals (0,                        decodedMsg.length());
        assertEquals (0,                        decodedMsg.size());
        assertEquals (HEADER_LENGTH,    decodedMsg.wireLength());

        // MINIMAL BINDING RESPONSE ///////////////////////
        // MappedAddress, SourceAddress, and ChangedAddress are mandatory
        MappedAddress  m = new MappedAddress  (dummyPublicAddress, dummyPort);
        SourceAddress  s = new SourceAddress  (serverAddr, serverPort);
        ChangedAddress c = new ChangedAddress (altAddr,    altPort);

        msg.add(m);
        assertEquals (1,        msg.size());
        msg.add(s);
        assertEquals (2,        msg.size());
        msg.add(c);
        assertEquals (3,        msg.size());

        int serLength = msg.wireLength();
        assertEquals ( 20 + 3 * 12, serLength );
        serialized = new byte[serLength];
        msg.encode(serialized);
        
        decodedMsg = (BindingResponse) StunMsg.decode(serialized);
        assertEquals (BINDING_RESPONSE,  decodedMsg.type);
        assertEquals (36,                       decodedMsg.length());
        assertEquals (3,                        decodedMsg.size());
    }
    /**
     * Run minimal BindingRequest by real server.
     */
    public void testLiveFireBindingRequest()    throws Exception {
        DatagramSocket socket = new DatagramSocket();
        socket.setReuseAddress(true);
        socket.connect (serverAddr, serverPort);
        socket.setSoTimeout(500);       // ms

        msg = new BindingRequest();
        serialized = new byte[msg.wireLength()];
        msg.encode(serialized);
        
        DatagramPacket outPkt = new DatagramPacket(serialized, 
                                    serialized.length, serverAddr, serverPort);
        socket.send(outPkt);
        
        byte[] buffer = new byte[200];
        DatagramPacket inPkt = new DatagramPacket(buffer, 200);
        socket.receive(inPkt);

        BindingResponse decodedMsg = (BindingResponse) StunMsg.decode(buffer);
        assertEquals (BINDING_RESPONSE,  decodedMsg.type);
        byte[] msgID = msg.getMsgID();
        
        // HACK -- shouldn't assume that order is fixed -- 
        // although it seems to be.

        // stun.fwdnet.net returns 3
        // assertEquals (5,    decodedMsg.size());
        StunAttr 
        attr = decodedMsg.get(0);
        assertEquals(MAPPED_ADDRESS,        attr.type);
        attr = decodedMsg.get(1);
        assertEquals(SOURCE_ADDRESS,        attr.type);
        
        attr = decodedMsg.get(2);
        assertEquals(CHANGED_ADDRESS,       attr.type);
      
        if (decodedMsg.size() > 3) {
            // XXX we should be passing IP addresses as simple byte arrays
            attr = decodedMsg.get(3);
            assertEquals(XOR_MAPPED_ADDRESS,    attr.type);
        
            attr = decodedMsg.get(4);
            assertEquals(SERVER_NAME,           attr.type);
            System.out.println("ServerName is " + new String(attr.value));
        }
        socket.close();
    } 

    /**
     * This amounts to a server test.  It should return a
     * BindingErrorResponse with UnknownAttributes.
     *
     * XXX The Vovida server times out instead of returning an
     * XXX   error response, so comment out for now.
     * XXX Softjoys behavior is the same
     */
//  public void testBadAttrs()                throws Exception {
//      DatagramSocket socket = new DatagramSocket();
//      socket.setReuseAddress(true);
//      socket.connect (serverAddr, serverPort);
//      socket.setSoTimeout(500);       // ms

//      msg = new BindingRequest();
//      // generate some trash attributes
//      int badAttrCount = 1 + rng.nextInt(3);
//      for (int i = 0; i < badAttrCount; i++) {
//          int attrLen = 5 + rng.nextInt(30);
//          attrLen = 4 * ((attrLen + 3)/ 4);
//          byte[] attrVal = new byte[attrLen];
//          rng.nextBytes(attrVal);
//          StunAttr badAttr = new BadAttr (0x7000 + i, attrVal);
//          msg.add( badAttr );
//          // DEBUG
//          System.out.println("  attr " + i + ": type = " + (0x7000 + i)
//                  + ", length = " + attrLen);
//          // END
//      }
//      assertEquals(badAttrCount,  msg.size());

//      serialized = new byte[msg.wireLength()];
//      msg.encode(serialized);
//      // DEBUG
//      System.out.println("wire length is " + msg.wireLength());
//      System.out.println("message with bad (unknown) attributes:\n  " +
//              "---- ---- ---+---+---+---+---+---+---+---+\n  " +
//          StringLib.byteArrayToHex(serialized, 0, serialized.length));
//      // END
//      DatagramPacket outPkt = new DatagramPacket(serialized, 
//                                  serialized.length, serverAddr, serverPort);
//      socket.send(outPkt);
//      
//      byte[] buffer = new byte[200];
//      DatagramPacket inPkt = new DatagramPacket(buffer, 200);

//      socket.receive(inPkt);

//      BindingErrorResponse decodedMsg 
//                  = (BindingErrorResponse) StunMsg.decode(buffer);
//      assertEquals (BINDING_ERROR_RESPONSE,  decodedMsg.type);
//      byte[] msgID = msg.getMsgID();
//      
//      socket.close();
//  } // GEEP
    /**
     * This amounts to a server test.  It should silently ignore
     * these attributes.
     */
    public void testIgnoredAttrs()                throws Exception {
        DatagramSocket socket = new DatagramSocket();
        socket.setReuseAddress(true);
        socket.connect (serverAddr, serverPort);
        socket.setSoTimeout(500);       // ms

        msg = new BindingRequest();
        // generate some trash attributes
        int badAttrCount = 1 + rng.nextInt(3);
        for (int i = 0; i < badAttrCount; i++) {
            int attrLen = 5 + rng.nextInt(30);
            attrLen = 4 * ((attrLen + 3)/ 4);
            byte[] attrVal = new byte[attrLen];
            rng.nextBytes(attrVal);
            StunAttr badAttr = new IgnoredAttr (0x8000 + i, attrVal);
            msg.add( badAttr );
        }
        assertEquals(badAttrCount,  msg.size());

        serialized = new byte[msg.wireLength()];
        msg.encode(serialized);
        DatagramPacket outPkt = new DatagramPacket(serialized, 
                                    serialized.length, serverAddr, serverPort);
        socket.send(outPkt);
        
        byte[] buffer = new byte[200];
        DatagramPacket inPkt = new DatagramPacket(buffer, 200);

        // ERROR XXX 222222222222222222222222222222222222222222222222
        socket.receive(inPkt);

        BindingResponse decodedMsg = (BindingResponse) StunMsg.decode(buffer);
        assertEquals (BINDING_RESPONSE,  decodedMsg.type);
        byte[] msgID = msg.getMsgID();
        
        socket.close();
    } 
    
//  // XXX Need a TLS connection for this ...
//  public void testLiveSharedSecret()          throws Exception {
//      int timeout = 500;

//      // XXX USE XLATTICE transport INSTEAD 
//      Socket socket = new Socket (serverAddr, serverPort);
//      socket.setSoTimeout(timeout);
//      InputStream  in  = socket.getInputStream();
//      OutputStream out = socket.getOutputStream();
//      
//      msg = new SharedSecretRequest();
//      serialized = new byte[msg.wireLength()];
//      msg.encode(serialized);      // XXX operation name is misleading???
//      out.write(serialized);
//     
//      // XXX THIS NEEDS TO BE DONE MORE CAREFULLY
//      byte[] buffer = new byte[200];
//      int count = in.read(buffer);
//      // HACK
//      System.out.println("data from server:\n" +
//              StringLib.byteArrayToHex(buffer, 0, count));
//      // END
//      
//      SharedSecretResponse decodedSSR = (SharedSecretResponse) 
//                                              StunMsg.decode(buffer);
//      assertEquals (BINDING_RESPONSE,  decodedSSR.type);
//      
//      byte[] msgID = msg.getMsgID(); 
//     
//  } // GEEP
}
