/* KadNode.java */
package org.xlattice.protocol.xlkad;

import org.xlattice.NodeID;

/**
 * An XLKad node.
 * 
 * @author Jim Dixon
 */
public class KadNode {

    /** number of bits in a key */
    private final int B = NodeID.LENGTH * 8;
   
    // INSTANCE VARIABLES ///////////////////////////////////////////
    private final NodeID myID;
    
    private final KBucket[] buckets = new KBucket[B];
    
    // CONSTRUCTORS /////////////////////////////////////////////////
    public KadNode(NodeID id) {
        // XXX SHOULD GENERATE ONE IF NOT SUPPLIED
        if (id == null) 
            throw new IllegalArgumentException("null NodeID");
        myID = (NodeID) id.clone();
        
        // This is for testing.  In a production system buckets likely
        // to be used should be created here.
        for (int i = B -2; i < B; i++) {
            buckets[i] = new KBucket();
        }
        
    }
    public KadNode () {
        this(null);
    }
    
    /** 
     * @return the index of the bucket a NodeID belongs in
     */
    public int index (NodeID id) {
        // STUB
        return 0;
    }
    /**
     * @return the 'distance' between this node and another key
     */
    public NodeID distance (NodeID other) {
        if (other == null)
            throw new IllegalArgumentException("null other NodeID");
        byte[] xor = new byte[NodeID.LENGTH];
        for (int i = 0; i < NodeID.LENGTH; i++)
            xor[i] = (byte)(myID.value()[i] ^ other.value()[i]);
        return new NodeID(xor);
    }
    /**
     * Base-2 logarithm of the distance between this node and 
     * another, which is the same as the index of the KBucket
     * that the other node would fall into.
     */
    public int logDistance (NodeID other) {
        if (other == null)
            throw new IllegalArgumentException("null other NodeID");
        // XXX should merge xor code in 
        byte[] d  = distance(other).value();
        byte[] me = myID.value();
        for (int i = 0; i < NodeID.LENGTH; i++) {
            byte xor = (byte)(d[i] ^ me[i]);
            if (xor != 0) { 
                int j;
                byte mask = (byte)(0x80);
                // we are done somewhere in this byte
                for (j = 7; j > 0; j--) {
                    if ((mask & xor) == 0)
                        break;
                    mask >>= 1;
                }
                // DEBUG
                System.out.println("logDistance: i = " + 8 + ", j = " + j);
                // END
                return (B - 1) - (i * 8 + j);
            } 
        }
        return B - 1;
    }
}
