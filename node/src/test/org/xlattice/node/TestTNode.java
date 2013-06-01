/* TestTNode.java */
package org.xlattice.node;

import java.util.Date;
import java.util.Random;
import junit.framework.*;
import org.xlattice.*;
import org.xlattice.crypto.RSAKey;
import org.xlattice.crypto.RSAKeyGen;
import org.xlattice.crypto.RSAPublicKey;
import org.xlattice.util.StringLib;

/**
 * 
 * @author Jim Dixon
 */
public class TestTNode extends TestCase {

    private Random rng = new Random ();
    private RSAKeyGen keyGen;

    // these get changed for each test run
    private RSAKey    key;
    private Node      node;
    private NodeID    nodeID;
    private PublicKey pubkey;
    
    public TestTNode (String name)              throws Exception {
        super(name);
        keyGen= new RSAKeyGen();
    }

    public void setUp () {
        key    = null;
        node   = null;
        nodeID = null;
        pubkey = null;
    }
    
    public void testNakedNode ()                throws Exception {
        try {
            node = new TNode (null, null, null, null, null);
            fail ("TNode accepted null arguments");
        } catch (IllegalArgumentException iae) { /* expected */ }
    }
    public void testWithoutOverlays()           throws Exception {
        key = (RSAKey) keyGen.generate();
        pubkey = key.getPublicKey();
        
        byte[] b = new byte[20];
        rng.nextBytes(b);
        NodeID nodeID = new NodeID (b);
       
        try {
            node = new TNode (key, nodeID, null, null, null);
        } catch (IllegalArgumentException iae) {
            fail("unexpected illegal arg exception if overlays array null");
        }
        assertNotNull(node);
        assertNotNull(node.getNodeID());
        assertNotNull(node.getNodeID().value());
        assertTrue (nodeID.equals(node.getNodeID()));
        assertTrue (pubkey.equals(node.getPublicKey()));
        assertEquals(0, node.sizeConnections());
        assertEquals(0, node.sizeOverlays());
        assertEquals(0, node.sizePeers());
    }
    
}
