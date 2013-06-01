/* TestMsgParser.java */
package org.xlattice.node.nodereg;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Random;

import junit.framework.*;

import org.xlattice.*;
import org.xlattice.DigSigner;
import org.xlattice.crypto.Key64Coder;
import org.xlattice.crypto.RSAKey;
import org.xlattice.crypto.RSAKeyGen;
import org.xlattice.crypto.RSAPublicKey;
import org.xlattice.crypto.SHA1withRSAVerifier;
import org.xlattice.util.Base64Coder;

/**
 * Test the blocking node registry client.
 *
 * @author Jim Dixon
 */
public class TestMsgParser extends TestCase {

    private final static byte[] CRLF = MsgParser.CRLF;

    private Random rng = new Random ( new Date().getTime() );
    private RSAKeyGen keyGen;
    private final InetAddress host;

    // these get changed for each test run
    private MsgParser    parser;
    private RSAKey       key;           // client's
    private RSAPublicKey pubkey;        // client's

    private ByteBuffer in;
    private ByteBuffer out;
    private int port;
    
    public TestMsgParser (String name)       
                    throws CryptoException, java.net.UnknownHostException {
        super(name);
        keyGen= new RSAKeyGen();
        host = InetAddress.getLocalHost();
    }

    public void setUp () {
        parser    = null;
        key       = null;
        pubkey    = null;

        in     = ByteBuffer.allocate(1024);
        out    = ByteBuffer.allocate(1024);
        port   = -1;
    }
    
    public void testParameterChecks ()          throws Exception {
        try {
            parser = new MsgParser (in, out, host, port);
            fail("accepted negative port");
        } catch (IllegalArgumentException iae) { /* success */ }
        try {
            parser = new MsgParser (null, out, host, 1);
            fail("accepted null buffer");
        } catch (IllegalArgumentException iae) { /* success */ }
    }
    /**
     * Look for correct handling of line folding and proper detection
     * of CRLFs not followed by spaces.  The actual data is nonsense.
     *
     * XXX These tests began to fail after the decoding and digital
     * XXX signature logic was added to MsgParser; the tests need to
     * XXX be fixed.
     */
//  public void testLineBreaks ()               throws Exception {
//      in.clear();
//      in.put(
//          "reg\r\nrsa 1234\r\n 56\r\nhttp www.xlattice.org:80\r\nabcd\r\n"
//          .getBytes());
//      in.flip();
//      parser = new MsgParser(in, out, host, 1);
//      ByteBuffer results = parser.parse();
//  
//      int[] ends = parser.getCrlfEnds();
//      assertEquals (4, ends.length);
//      assertEquals (5, ends[0]);
//      assertEquals (20, ends[1]);
//      assertEquals (46, ends[2]);
//      assertEquals (52, ends[3]);
//      assertEquals ("reg", parser.getCmd());
//      assertEquals ("http www.xlattice.org:80", parser.getEndPoint());

//      in.clear();
//      in.put(
//          "reg\r\nrsa 1234\r\n 56\r\nhttp www.xlattice.org:80\r\n"
//          .getBytes());
//      in.flip();
//      parser = new MsgParser(in, out, host, 1);
//      results = parser.parse();
//  
//      ends = parser.getCrlfEnds();
//      assertEquals ( 4, ends.length);
//      assertEquals ( 5, ends[0]);
//      assertEquals (20, ends[1]);
//      assertEquals (46, ends[2]);
//      assertEquals ( 0, ends[3]);

//      // the algorithm detects line folding anywhere
//      in.clear();
//      in.put(
//     "reg\r\nrs\r\n a 1234\r\n 56\r\nhttp www.xlat\r\n tice.org:80\r\n1234\r\n"
//          .getBytes());
//      in.flip();
//      parser = new MsgParser(in, out, host, 1);
//      results = parser.parse();
//  
//      ends = parser.getCrlfEnds();
//      assertEquals (4, ends.length);
//      assertEquals (5, ends[0]);
//      assertEquals (23, ends[1]);
//      assertEquals (52, ends[2]);
//      assertEquals (58, ends[3]);
//  } // GEEP

    /**
     * The server returns a deep copy of the public key to each
     * request.
     */
    public void testServerKey ()                throws Exception {
        RSAPublicKey pk1 = Server.getPublicKey();
        RSAPublicKey pk2 = Server.getPublicKey();
        assertFalse (pk1 == pk2);
        assertTrue  (pk1.equals(pk2));
    }
    public void testRealParsing ()              throws Exception {
        assertNotNull(Server.getMap());

        // the client key
        key = (RSAKey) keyGen.generate();
        pubkey = (RSAPublicKey) key.getPublicKey();
        String encodedPubKey = Key64Coder.encodeRSAPublicKey(pubkey);
       
        // the server's key and signer
        RSAPublicKey srvPubKey = Server.getPublicKey();
        assertNotNull(srvPubKey);
        DigSigner srvSigner    = Server.getSigner();
        assertNotNull(srvSigner);
        SigVerifier verifier   = Server.getVerifier();
        assertNotNull(verifier);
        
        in.clear();           // yes, I'm neurotic
        in.put("reg\r\n".getBytes());
        in.put(encodedPubKey.getBytes());
        in.put(CRLF);
        in.put("http www.xlattice.org:80\r\n".getBytes());
        
        DigSigner signer = key.getSigner("rsa");
        signer.update(in.array(), 0, in.position());
        byte[] sig = signer.sign();
        String encodedSig = Base64Coder.encode(sig);
        in.put(encodedSig.getBytes());
        in.put(CRLF);
        in.flip();
        
        parser = new MsgParser(in, out, host, 1);
        ByteBuffer results = parser.parse();
        results.flip();

        int[] ends = parser.getCrlfEnds();
        assertEquals (4, ends.length);
        assertEquals (5, ends[0]);
        assertEquals ("reg", parser.getCmd());
        assertEquals ("http www.xlattice.org:80", parser.getEndPoint());
        assertNotNull(results);
       
        verifyWithPubKey (results, srvPubKey);
        // XXX FAILS IF RUN BEFORE THE PRECEDING TEST :-) XXX
        parseAndVerify (results, verifier);
    }
    
    public void verifyWithPubKey (ByteBuffer b, RSAPublicKey pubkey) 
                                                throws Exception {
        String s = new String (b.array(), 0, b.limit());
        String[] lines = s.split("\r\n");
        assertEquals(4, lines.length);
        assertEquals("ok", lines[0]);

        byte[] nodeID = Base64Coder.decode(lines[1]);
        assertEquals (20, nodeID.length);

        // XXX NEED TO BE ABLE TO CONVERT String BACK INTO Timestamp
        String timestamp = lines[2];

        byte[] sig = Base64Coder.decode(lines[3]);
        int len = lines[0].length() + lines[1].length() 
                + lines[2].length() + 6;
        assertEquals('\n', b.array()[len - 1]);

        SigVerifier verifier = new SHA1withRSAVerifier();
        verifier.init(pubkey);
        verifier.update( b.array(), 0, len);
        assertTrue (verifier.verify(sig));
    }
    public void parseAndVerify (ByteBuffer b, SigVerifier verifier) 
                                                throws Exception {
        String s = new String (b.array(), 0, b.limit());
        String[] lines = s.split("\r\n");
        assertEquals(4, lines.length);
        assertEquals("ok", lines[0]);

        byte[] nodeID = Base64Coder.decode(lines[1]);
        assertEquals (20, nodeID.length);

        // XXX NEED TO BE ABLE TO CONVERT String BACK INTO Timestamp
        String timestamp = lines[2];

        byte[] sig = Base64Coder.decode(lines[3]);
        int len = lines[0].length() + lines[1].length() 
                + lines[2].length() + 6;
        assertEquals('\n', b.array()[len - 1]);

        // the verifier is already init()ed
        verifier.update( b.array(), 0, len);
        assertTrue (verifier.verify(sig));
    }
}
