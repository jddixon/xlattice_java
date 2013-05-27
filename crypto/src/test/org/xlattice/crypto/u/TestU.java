/* TestU.java */
package org.xlattice.crypto.u;

import java.io.File;
import java.util.Random;

import junit.framework.*;

import static org.xlattice.crypto.u.UConst.*;

/**
 * @author Jim Dixon
 */

public class TestU extends TestCase {

    public final static String TEST_DIR = "tmp-u";

    private U store;
    private Counter johnny;
    private Walker  walker;

    private Random rng = new Random();

    public TestU (String name)              throws Exception {
        super(name);
    }
    static public final String someHex  = "0123456789abcdef";
    static public final String FOO       = "foo";

    public void testChkDirFixPath()         throws Exception {
        StringBuffer sb;

        UFlat uFlat = (UFlat)U.createU(FOO, FLAT_DIR);     // lazy
        sb = new StringBuffer(FOO);
        sb = uFlat.chkDirFixPath(sb, someHex);
        assertEquals (FOO, sb.toString());
        uFlat.close();
        assertTrue ( U.recursiveRemove(FOO) );

        U16 u16 = (U16)U.createU(FOO, DIR16);
        sb = new StringBuffer(FOO);
        sb = u16.chkDirFixPath(sb, someHex);
        assertEquals (FOO + "/0", sb.toString());
        u16.close();
        assertTrue ( U.recursiveRemove(FOO) );

        U256 u256 = (U256)U.createU(FOO, DIR256);
        sb = new StringBuffer(FOO);
        sb = u256.chkDirFixPath(sb, someHex);
        assertEquals (FOO + "/01", sb.toString());
        u256.close();
        assertTrue ( U.recursiveRemove(FOO) );

        U256x16 u256x16 = (U256x16)U.createU(FOO, DIR256x16);
        sb = new StringBuffer(FOO);
        sb = u256x16.chkDirFixPath(sb, someHex);
        assertEquals (FOO + "/01/2", sb.toString());
        u256x16.close();
        assertTrue ( U.recursiveRemove(FOO) );

        U256x256 u256x256 = (U256x256)U.createU(FOO, DIR256x256);
        sb = new StringBuffer(FOO);
        sb = u256x256.chkDirFixPath(sb, someHex);
        assertEquals (FOO + "/01/23", sb.toString());
        u256x256.close();
        assertTrue ( U.recursiveRemove(FOO) );
    }
    public void testSigFile()         throws Exception {
        StringBuffer sb;
        UFlat uFlat = (UFlat)U.createU(FOO, FLAT_DIR);
        U.writeSig(uFlat);
        // XXX read and compare
        uFlat.close();

        // XXX REOPEN, DISCOVERING
        // XXX read and compare

        assertTrue ( U.recursiveRemove(FOO) );
    }
    public void testDiscovery()         throws Exception {
        // NO, ADD creation of .u to U creation - except where
        // discovery involved.
    }
}
