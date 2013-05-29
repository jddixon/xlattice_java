/* TestStunAttr.java */
package org.xlattice.protocol.stun;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Random;

import junit.framework.*;

import org.xlattice.protocol.TLV;
import org.xlattice.protocol.TLV16;
import org.xlattice.util.StringLib;

/**
 * @author Jim Dixon
 */

public class TestStunAttr extends TestCase implements StunConst {

    private Random rng = new Random();

    private Inet4Address addr;
    private StunAttr attr;
    
    public TestStunAttr (String name) {
        super(name);
    }

    public void setUp () {
        addr = null;
        attr = null;
    }

    public void testConstructors()              throws Exception {
    }

    /**
     * This actually tests all subclasses of StunAddr.
     */
    public void testMappedAddress()             throws Exception {
        // constructor ////////////////////////////////////
        Inet4Address loopback = (Inet4Address)InetAddress
                                            .getByName("127.0.0.1");
        attr = new MappedAddress(loopback, 0xfedc);
        byte[] expected = new byte[] {
            //                  family          ------- port ------
            (byte) 0x00,  (byte) 0x01,   (byte) 0xfe,   (byte) 0xdc,
            (byte)  127,  (byte)    0,   (byte)    0,   (byte)    1};

        assertEquals( StunAttr.MAPPED_ADDRESS, attr.type );
        assertEquals( 8, attr.length() );
        for (int i = 0; i < expected.length; i++)
            assertEquals( "value[" + i + "] expected vs actual: ",
                    expected[i], attr.value[i] );
        // writing/serializing to message /////////////////
        byte[] message = new byte[HEADER_LENGTH + 12];
        attr.encode(message, HEADER_LENGTH);

        // reading/deserializing message //////////////////
        TLV tlv = TLV16.decode(message, HEADER_LENGTH);
        assertEquals (2,                tlv.fieldSize);
        assertEquals (StunAttr.MAPPED_ADDRESS,   
                                        tlv.type);
        assertEquals (8,                tlv.length());
        for (int i = 0; i < expected.length; i++)
            assertEquals( "TLV value[" + i + "] expected vs actual: ",
                    expected[i], tlv.value[i] );
    }
   
    /**
     * Tests ValueAttr.
     */
    public void testIgnoredAttr()               throws Exception {
        // generate some trash attributes
        int badAttrCount = 1 + rng.nextInt(3);
        for (int i = 0; i < badAttrCount; i++) {
            byte[] buffer = new byte[1024];
            int attrLen = 5 + rng.nextInt(30);
            attrLen = 4 * ((attrLen + 3)/ 4);
            byte[] attrVal = new byte[attrLen];
            rng.nextBytes(attrVal);
            StunAttr badAttr = new IgnoredAttr (0x8000 + i, attrVal);
            assertEquals( 0x8000 + i,   badAttr.type);
            assertEquals( attrLen,      badAttr.length());
            for (int j = 0; j < attrLen; j++)
                assertEquals ( attrVal[j],  badAttr.value[j] );
            badAttr.encode(buffer, HEADER_LENGTH);
            StunAttr decoded = (StunAttr)StunAttr
                                .decode (buffer, HEADER_LENGTH);
            assertEquals( 0x8000 + i,   decoded.type );
            assertEquals( attrLen,      decoded.length() );
            for (int j = 0; j < attrLen; j++)
                assertEquals ( attrVal[j],  decoded.value[j] );

        }
    } 
    public void testErrorCodes()                throws Exception {

        ErrorCode error;
        int code;
        String reason;
        
        int [] codes = ErrorCode.codes;
        byte[] buffer;
        ErrorCode decoded;

        for (int n = 0; n < codes.length; n++) {
            buffer = new byte[256];
            code   = codes[n];
            error  = new ErrorCode(code);
            assertEquals (code,         error.code);
            // serialize the attribute 
            error.encode(buffer, HEADER_LENGTH);
            // now deserialize it
            decoded = (ErrorCode)StunAttr
                                .decode(buffer, HEADER_LENGTH);
            assertEquals (code,         decoded.code);
            // HACK - depends upon implementation detail
            StunAttr attr = decoded;
            byte[] x      = attr.value;
            int nullAt;
            for (nullAt = 4; nullAt < x.length; nullAt++)
                if (x[nullAt] == 0) 
                    break;
            String s = new String(x, 4, nullAt - 4);
            assertEquals(ErrorCode.reasonPhrases[n], s);
            // END
        }
    }
    public void testUnknownAttrs()              throws Exception {

        UnknownAttributes unk;
        int [] attrs;
        byte[] buffer;
        UnknownAttributes decoded;

        for (int n = 0; n < 8; n++) {
            buffer = new byte[256];
            int count = 1 + rng.nextInt(8);
            boolean odd = (count % 2) == 0 ? false : true;
            attrs = new int [count];
            for (int i = 0 ; i < count; i++ )
                attrs[i] = rng.nextInt(65536);
                
            unk  = new UnknownAttributes(attrs);
            if (odd)
                assertEquals ( count + 1,   unk.attrs.length );
            else
                assertEquals ( count,       unk.attrs.length );
            for (int i = 0; i < count; i++)
                assertEquals (unk.attrs[i],         attrs[i]);
            if (odd)
                assertEquals (unk.attrs[count],     attrs[count - 1]);
            // serialize the attribute 
            unk.encode(buffer, HEADER_LENGTH);
            // now deserialize it
            decoded = (UnknownAttributes)StunAttr
                                .decode(buffer, HEADER_LENGTH);
            // XXX NOT COMPLETE
        //    assertEquals (code,         decoded.code);
        }
    }

}
