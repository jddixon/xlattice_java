/* Source.java */
package org.xlattice.protocol.xl;

import org.xlattice.NodeID;

public class Source extends NodeIDAttr {

    public Source (byte[] id) {
        super ( SOURCE, id );
    }
    public Source (NodeID nodeID) {
        this(nodeID.value());
    }
}
