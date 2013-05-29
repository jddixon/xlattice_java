/* Destination.java */
package org.xlattice.protocol.xl;

import org.xlattice.NodeID;

public class Destination extends NodeIDAttr {

    public Destination (byte[] id) {
        super ( DESTINATION, id );
    }
    public Destination (NodeID nodeID) {
        this( nodeID.value() );
    }
}
