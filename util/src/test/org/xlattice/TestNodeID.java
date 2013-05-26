/* TestNodeID.java */
package org.xlattice;

import java.util.Date;
import java.util.Random;

import junit.framework.*;

import org.xlattice.util.StringLib;

/**
 * @author Jim Dixon
 */

public class TestNodeID extends TestCase {

    public static final int LENGTH = NodeID.LENGTH;

    Random rng = new Random ( new Date().getTime() );
    public TestNodeID (String name) {
        super(name);
    }

    public void setUp () {
    }

    public void testBadNodeIDs()            throws Exception {
        assertFalse(NodeID.isValid(null));
        assertFalse(NodeID.isValid(new byte[LENGTH - 1]));
        assertTrue (NodeID.isValid(new byte[LENGTH]));
        assertFalse(NodeID.isValid(new byte[LENGTH + 1]));
    }
    public void testThisNThat()             throws Exception {
        byte[] val1 = new byte[LENGTH];
        byte[] val2 = new byte[LENGTH];
        rng.nextBytes(val1);            // 20 random bytes
        rng.nextBytes(val2);
        NodeID id1 = new NodeID(val1);
        NodeID id2 = new NodeID(val2);
        assertFalse(id1.equals(id2));   // seems a safe assumption ;-)
        byte[] val1a = id1.value();
        byte[] val2a = id2.value();
        for (int i = 0; i < LENGTH ; i++) {
            assertEquals (val1[i], val1a[i]);
            assertEquals (val2[i], val2a[i]);
        }
        assertTrue (id1.equals(id1.clone()));
        assertTrue (id1.equals(id1));

        assertEquals (id1.hashCode(), ((NodeID)id1.clone()).hashCode());
        // will fail every 4 billion times or so
        assertFalse (id1.hashCode() == id2.hashCode());
    }
    public void doComparison(final int DEPTH, int expected, 
                                              NodeID x, NodeID y) {
        byte [] valueX = x.value();
        byte [] valueY = y.value();
        // DEBUG
        int result = x.compareTo(y);
        if (result != expected) {
            System.out.println("depth " + DEPTH 
                + ": expected " + expected 
                + " but comparison returns " + result 
                + "\n  " + StringLib.byteArrayToHex(valueX, 0, DEPTH + 1)
                + "\n  " + StringLib.byteArrayToHex(valueY, 0, DEPTH + 1)
            );
        }
        // END
        assertEquals(expected,    x.compareTo(y));

    }
    public void testComparator()                throws Exception {
        byte[] valueA = new byte[LENGTH];
        byte[] valueX = new byte[LENGTH];
        byte[] valueY = new byte[LENGTH];
        byte[] valueZ = new byte[LENGTH];
        rng.nextBytes(valueX);
        NodeID nodeIDx;
        NodeID nodeIDy;
        NodeID nodeIDz;
        
        final int COUNT = 1 + rng.nextInt(5);
        final int DEPTH = 3 + rng.nextInt(LENGTH - 3);
        for (int i = 0; i < COUNT; i++) {
            rng.nextBytes(valueA);
            nodeIDx = new NodeID(valueX);       // c'tor copies value 
            assertEquals(0,     nodeIDx.compareTo(nodeIDx) );
            
            for (int j = 0; j < DEPTH; j++) {
                System.arraycopy(valueA, 0, valueX, 0, LENGTH);
                System.arraycopy(valueA, 0, valueY, 0, LENGTH);
                System.arraycopy(valueA, 0, valueZ, 0, LENGTH);
                int midPoint = 1 + rng.nextInt(254);    // so 1 .. 254
                int oneBelow = midPoint - 1;
                int oneAbove = midPoint + 1;
                valueX[DEPTH] = (byte)oneBelow;
                valueY[DEPTH] = (byte)midPoint;
                valueZ[DEPTH] = (byte)oneAbove;
                nodeIDx = new NodeID(valueX);    
                nodeIDy = new NodeID(valueY);    
                nodeIDz = new NodeID(valueZ);    
                doComparison(DEPTH, -1, nodeIDx, nodeIDy);
                doComparison(DEPTH, -1, nodeIDy, nodeIDz);
                doComparison(DEPTH,  1, nodeIDy, nodeIDx);
                doComparison(DEPTH,  1, nodeIDz, nodeIDy);
            }
        }
    }
}
