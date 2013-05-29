/* TestKadNode.java */
package org.xlattice.protocol.xlkad;

import java.util.Random;

import junit.framework.*;

/**
 * @author Jim Dixon
 */

public class TestKadNode extends TestCase {

    private Random rng = new Random();
    private KadNode myNode;
    private KadID  myID;
    private KadID  otherID;
    
    public TestKadNode (String name) {
        super(name);
    }

    public void setUp () {
        myNode  = null;
        myID    = null;
        otherID = null;
    }

    public void testConstructors()              throws Exception {
    }

}
