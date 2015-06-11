/* TestDecimalVersion.java */
package org.xlattice.util;

import java.util.Date;
import java.util.Random;
import junit.framework.*;

public class TestDecimalVersion extends TestCase {

    Date   now = new Date();
    Random rng = new Random ( now.getTime() );  // time is a long (ms) 

    public TestDecimalVersion(String name) {
        super(name);
    }
   
    public void setUp() {
    }

    public void testversionFromBytes()                  throws Exception {
    	DecimalVersion dv1 = new DecimalVersion(1, 2, 3, 4);
        DecimalVersion dv2 = null;
    	byte b[] = {1, 2, 3, 4};
        try {   
    	    dv2 = DecimalVersion.versionFromBytes(b);
        } catch (Exception e) {
            fail("unexpected exception converting byte array");
        }
    	assertEquals(dv1, dv2);
    
    	//dv3, err = versionFromBytes(b[1:]); // so only 3 bytes long
    	//assert(err, Equals, WrongLengthForVersion);
    	//assert(dv3, Equals, DecimalVersion(0));
    }
    public void testDecimalVersion()                    throws Exception {
    
    	// always print at least two decimals
    	DecimalVersion dv = new DecimalVersion(1, 0, 0, 0);
    	String v = dv.toString();
    	assertEquals(v, "1.0");
    	DecimalVersion dv2 = DecimalVersion.parseDecimalVersion(v);
    	assertEquals(dv2, dv);
    
    	// don't print more if the values are zero
    	dv = new DecimalVersion(1, 2, 0, 0);
    	v  = dv.toString();
    	assertEquals(v, "1.2");
    	dv2 = DecimalVersion.parseDecimalVersion(v);
    	assertEquals(dv2, dv);
    
    	// if the third byte is zero but the fourth isn't, print
    	// both
    	dv = new DecimalVersion(1, 2, 0, 4);
    	v = dv.toString();
    	assertEquals(v, "1.2.0.4");
    	dv2 = DecimalVersion.parseDecimalVersion(v);
    	assertEquals(dv2, dv);
    
    	// other cases
    	dv = new DecimalVersion(1, 2, 3, 0);
    	v = dv.toString();
    	assertEquals(v, "1.2.3");
    	dv2 = DecimalVersion.parseDecimalVersion(v);
    	assertEquals(dv2, dv);
    
    	dv = new DecimalVersion(1, 2, 3, 4);
    	v = dv.toString();
    	assertEquals(v, "1.2.3.4");
    	dv2 = DecimalVersion.parseDecimalVersion(v);
    	assertEquals(dv2, dv);
    
    	for (int i = 0; i < 8; i++) {
    		int n = rng.nextInt();
    		dv = new DecimalVersion(n);
    		v = dv.toString();
    		dv2 = DecimalVersion.parseDecimalVersion(v);
    		assertEquals(dv2, dv);
    	}
    }
}
