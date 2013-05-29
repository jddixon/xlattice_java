/* KadID.java */
package org.xlattice.protocol.xlkad;

import org.xlattice.NodeID;
// DEBUG
import org.xlattice.util.StringLib;
// END

/**
 * An XLKad node.
 * 
 * @author Jim Dixon
 */
public class KadID extends NodeID {

    public static final int LENGTH = NodeID.LENGTH;

    /** number of bits in a key */
    private final int B = NodeID.LENGTH * 8;
   
    // CONSTRUCTORS /////////////////////////////////////////////////
    public KadID(byte[] id) {
        super(id);
    }
    /**
     * @return the 'distance' between this id and another key
     */
    public KadID distance (KadID other) {
        if (other == null)
            throw new IllegalArgumentException("null other NodeID");
        byte[] xor = new byte[KadID.LENGTH];
        for (int i = 0; i < KadID.LENGTH; i++)
            xor[i] = (byte)(value()[i] ^ other.value()[i]);
        return new KadID(xor);
    }
    /**
     * Base-2 logarithm of the distance between this id and 
     * another.  If this KadID is the KadID of a KadNode, this is the 
     * same as the index of the KBucket that the other node would fall into.
     */
    public int logDistance (KadID other) {
        if (other == null)
            throw new IllegalArgumentException("null other KadID");
        // XXX should merge xor code in 
        byte[] d  = distance(other).value();
        int i, j = -1;
        for (i = 0; i < KadID.LENGTH; i++) {
            if (d[i] != 0) { 
                byte mask = (byte)(0x80);
                // we are done somewhere in this byte
                for (j = 0; j < 7; j++) {
                    if ((mask & d[i]) != 0) {
                        break;
                    }
                    mask >>>= 1;
                }
                break;
            } 
        }
        return ( i == KadID.LENGTH ) ? 
            -1 : 7 - j + (KadID.LENGTH -1 - i) * 8;
    }
}
