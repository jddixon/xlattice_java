/* TestNodeInfoMgr.java */
package org.xlattice.node;

import java.io.File;
import java.util.Random;

import junit.framework.*;

import org.xlattice.NodeID;
import org.xlattice.crypto.RSAKey;
import org.xlattice.util.FileLib;

/**
 * @author Jim Dixon
 */

public class TestNodeInfoMgr extends TestCase {

    public static final String INFO_DIR_NAME = "test.node.info/";
    private Random rng = new Random();

    private NodeInfoMgr mgr;
    
    // CONSTRUCTOR //////////////////////////////////////////////////
    public TestNodeInfoMgr (String name) {
        super(name);
    }
    // SETUP/TEARDOWN ///////////////////////////////////////////////
    public void setUp () {
        mgr = null;
    }
    // UNIT TESTS ///////////////////////////////////////////////////
    public void testEmptyConfig ()              throws Exception {
        try {
            mgr = new NodeInfoMgr(null);
            fail("accepted null argument");
        } catch (IllegalArgumentException iae) { /* expected */ }
        try {
            mgr = new NodeInfoMgr("");
            fail("accepted empty argument");
        } catch (IllegalArgumentException iae) { /* expected */ }
    }
    public void testUtilityMethods()            throws Exception {
        String s;
        try {
            s = NodeInfoMgr.checkConfigName(null);
            fail("accepted null config name");
        } catch (IllegalArgumentException iae) { /* good */ }
        try {
            s = NodeInfoMgr.checkConfigName("");
            fail("accepted empty config name");
        } catch (IllegalArgumentException iae) { /* good */ }
        try {
            s = NodeInfoMgr.checkConfigName(".xml");
            fail("accepted '.xml' as config name");
        } catch (IllegalArgumentException iae) { /* good */ }
        try {
            s = NodeInfoMgr.checkConfigName(File.separator + "abc");
            fail("accepted absolute path as config name");
        } catch (IllegalArgumentException iae) { /* good */ }
        try {
            s = NodeInfoMgr.checkConfigName("abc/../def");
            fail("accepted '..' in config name");
        } catch (IllegalArgumentException iae) { /* good */ }
        assertEquals ("abc", NodeInfoMgr.checkConfigName("abc.xml"));

        mgr = new NodeInfoMgr(INFO_DIR_NAME);
        assertEquals ( INFO_DIR_NAME + "abc.xml", mgr.getFileName("abc"));
               
        NodeConfig nc = NodeInfoMgr.buildRandomNC();
        assertEquals(0, nc.sizeOverlay());
        NodeID id = nc.getNodeID();
        assertNotNull(id);
        RSAInfo rsa = nc.getKey();      // XXX NAMING ERROR
        assertNotNull(rsa);
    }
    public void testStartingEmpty ()            throws Exception {
        FileLib.recursingDelete(INFO_DIR_NAME);
        
        File infoDir = new File(INFO_DIR_NAME);
        assertFalse(infoDir.exists());
        mgr = new NodeInfoMgr (INFO_DIR_NAME);
        assertTrue(infoDir.exists());
        assertTrue(infoDir.isDirectory());
        assertEquals(0, mgr.size());
      
        // 2011-08-23 The next line fails because a minOccur of 1 is violated
        NodeConfig ncA = mgr.get("aaa");
        assertEquals(1, mgr.size());

        NodeConfig ncB = mgr.get("bbb");
        assertEquals(2, mgr.size());
       
        // start with new NodeInfoMgr /////////////////////
        mgr = new NodeInfoMgr (INFO_DIR_NAME);
        assertEquals(2, mgr.size());

        NodeConfig ncC = mgr.get("ccc");
        assertEquals(3, mgr.size());
        NodeConfig ncD = mgr.get("ddd");
        assertEquals(4, mgr.size());
    } 
}
