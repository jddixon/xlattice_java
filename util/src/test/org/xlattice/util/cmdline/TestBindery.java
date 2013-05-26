/* TestBindery.java */
package org.xlattice.util.cmdline;

import junit.framework.*;

/**
 * @author Jim Dixon
 **/

public class TestBindery extends TestCase {

    /** Command line spectification. */
	static CmdLineOpt[] options = {
                 // option field
                 //  name  name     min      max
		new BooleanOpt ("b", "bFld"                ),
		new DoubleOpt  ("d", "dFld",  1.0,    100.0),
		new IntOpt     ("i", "iFld",  10,     10000),
        new LongOpt    ("l", "lFld",  -1L,   20000L),
		new StringOpt  ("s", "sFld"                )
	};

    class ZootSuit {
        boolean bFld;                  // defaults to false
        double  dFld = 3.1415926536;   
        int     iFld = 42;        
        long    lFld = 217L;
        String  sFld = "ZZ Topd";
    
        /** this is *not* static */
    	public void main(String[] args) {
        
			//ZootSuit inst = new ZootSuit();
            ZootSuit inst = this;
			int next = Bindery.bind (args, options, inst);
		    // deal with any unprocessed arguments
            // program logic goes here
    	}
    }
    public TestBindery (String name) {
        super(name);
    }

    ZootSuit zs;

    public void setUp () {
        zs = null;
    }

    public void tearDown() {
    }
    
    public void testConstructor() {
        zs = new ZootSuit();
        assertNotNull("constructor returned null", zs);
    }
    public void testMissingInt() {
        try {
            zs = new ZootSuit();
            zs.main( new String[]{
                    "-b", "-i",       "-d", "1.05", "-s", "bubble gum"});
            fail("did not detect missing int option value");
        } catch (CmdLineException cle) { }
    }
    public void testMissingDouble() {
        try {
            zs = new ZootSuit();
            zs.main( new String[]{
                    "-b", "-i", "25", "-d",         "-s", "bubble gum"});
            fail("did not detect missing double option value");
        } catch (CmdLineException cle) { }
    }
    public void testMissingString() {
        try {
            zs = new ZootSuit();
            zs.main( new String[]{
                    "-b", "-i", "25", "-d", "1.05", "-s"              });
            fail("did not detect missing String option value");
        } catch (CmdLineException cle) { }
        
    }
    // WRONG TYPE CHECKS
    
    public void testDoubleType()  {
        zs = new ZootSuit();
        zs.main(
            new String[]{"-d", "1.05"});
        assertEquals(1.05, zs.dFld, 0.00001);
        // RANGE CHECKS
        //
        // SCIENTIFIC NOTATION CHECKS
    }
    public void testIntType()  {
        zs = new ZootSuit();
        zs.main(
            new String[]{"-i", "25"});
        assertEquals(25, zs.iFld);
        // RANGE CHECKS
    }
    public void testLongType() {
        zs = new ZootSuit();
        zs.main(
            new String[]{"-l", "257"});
        assertEquals(257L, zs.lFld);
        // RANGE CHECKS
    }
    public void testBadString() {
        zs = new ZootSuit();
        try {
            zs.main(
                new String[]{"-s", "-begins with -"});
            fail("string beginning with - passed!");
        } catch ( CmdLineException cle ) {}
    }

    public void testAllTypes () {
        zs = new ZootSuit();
        zs.main(
            new String[]{
                "-b",                   // boolean
                "-d", "1.05",           // double
                "-i", "25",             // int
                "-s", "bubble gum"});   // String
        assertTrue(zs.bFld);
        assertEquals(25, zs.iFld);
        assertEquals(1.05, zs.dFld, 0.0001);
        assertEquals("bubble gum", zs.sFld);
    }
}
