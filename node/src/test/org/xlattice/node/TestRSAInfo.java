/* TestRSAInfo.java */
package org.xlattice.node;

import java.math.BigInteger;
import java.util.Random;
import junit.framework.*;
import org.xlattice.crypto.RSAKey;
import org.xlattice.util.Base64Coder;
import org.xlattice.util.StringLib;

/**
 * Verify functioning of RSAInfor class.
 *
 * XXX THERE IS A PROBLEM WITH THIS CODE, an occasional failure
 * XXX as yet unexplained.
 *
 * @author Jim Dixon
 */
public class TestRSAInfo extends TestCase {

    private Random rng = new Random (new java.util.Date().getTime());
    private RSAInfo info;
    private Base64Coder coder = new Base64Coder();
    
    public TestRSAInfo (String name) {
        super(name);
    }

    public void setUp () {
        info = new RSAInfo();
    }
   
    public void testSmallValues ()              throws Exception {
        String base64Value;     // value in base64-encoded form
        BigInteger bigValue;    // same (we hope) value as a BigInteger

        base64Value = coder.encode ( new byte[] { 3 } ) ;
        bigValue = new BigInteger("3");
        info.setP(base64Value);
        assertTrue (bigValue.equals( info.getBigP() ));
        assertTrue (base64Value.equals( info.getP() ));
        
        base64Value = coder.encode ( new byte[] { 17 } ) ;
        bigValue = new BigInteger("17");
        info.setQ(base64Value);
        assertTrue (bigValue.equals( info.getBigQ() ));
        assertTrue (base64Value.equals( info.getQ() ));
        
        base64Value = coder.encode ( new byte[] { 127 } ) ;
        bigValue = new BigInteger("127");
        info.setD(base64Value);
        assertTrue (bigValue.equals( info.getBigD() ));
        assertTrue (base64Value.equals( info.getD() ));
        
        base64Value = coder.encode ( new byte[] { -19 } ) ;
        bigValue = new BigInteger("-19");
        info.setE(base64Value);
        assertTrue (bigValue.equals( info.getBigE() ));
        assertTrue (base64Value.equals( info.getE() ));
    }
    /**
     * These tests used to throw up spurious failures because of the way 
     * that leading zeroes are handled and because of the treatment
     * of 0xff when it is the value of byte zero.  
     *
     * One problem was that XLattice byte array serialization does not 
     * suppress leading zeroes; the BigInteger String serialization 
     * methods do.  Therefore the base64 encodings of the same value 
     * will differ.
     * 
     * There are other problems.  For example, if the byte array
     *   ....x....1....x....2....x....3....x....4
     *   00099594d4ae220e484dfe9552d86327bed7e32c
     * is used to construct a BigInteger which is then serialized
     * using StringLib on BigInteger.getByteArray().  
     *     099594d4ae220e484dfe9552d86327bed7e32c
     * However we get 
     *      99594d4ae220e484dfe9552d86327bed7e32c
     * from BigInteger.toString (16).  This means that BigInteger 
     * discards the high order 0x00 byte, returning a byte[19] 
     * when the constructor used a byte[20].  On the other hand,
     * if the high-order byte (byte zero) is 0xff, the constructor 
     * will insert a preceding 0x00 byte as a new byte zero.
     *
     * As far as we are concerned here, the problem is that 
     * BigInteger's adding and dropping of high-order 0x00 bytes 
     * changes the base64-encoding and so causes unit tests to fail.
     * The solution is to do comparisons using BigInteger.equals().
     */
    public void testRandomValues ()             throws Exception {
        byte [] data = new byte[20];
        for (int i = 0; i < 32; i++) {  // have done multiple runs @4096
            rng.nextBytes(data);        // 20 random bytes
            String base64Value = coder.encode (data ) ;
            BigInteger bigValue = new BigInteger(data);

            info.setP(base64Value);     // set with String value
            assertTrue( bigValue.equals( info.getBigP() ));
            info.setBigP(bigValue);     // set with BigInteger
            assertTrue (bigValue.equals( 
                        new BigInteger (coder.decode(info.getP())) )); 
            
            info.setQ(base64Value);
            assertTrue( bigValue.equals( info.getBigQ() ));
            info.setBigQ(bigValue);
            assertTrue (bigValue.equals( 
                        new BigInteger (coder.decode(info.getQ())) )); 
            
            info.setD(base64Value);
            assertTrue( bigValue.equals( info.getBigD() ));
            info.setBigD(bigValue);
            assertTrue (bigValue.equals( 
                        new BigInteger (coder.decode(info.getD())) )); 
            
            info.setE(base64Value);
            assertTrue( bigValue.equals( info.getBigE() ));
            info.setBigE(bigValue);
            assertTrue (bigValue.equals( 
                        new BigInteger (coder.decode(info.getE())) )); 
        }
    }
}
