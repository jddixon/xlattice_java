/* NodeIDAttr.java */
package org.xlattice.protocol.xl;

import org.xlattice.NodeID;
import org.xlattice.util.StringLib;

public abstract class NodeIDAttr extends ValueAttr {

    public NodeIDAttr (int type, byte[] id) {
        super ( type, id );
        if (id.length != NodeID.LENGTH)
            throw new IllegalArgumentException(
                    "NodeID must have a length of " + NodeID.LENGTH);
    }
    public String toString() {
        StringBuffer sb = new StringBuffer();
        switch (type) {
            case SOURCE:        sb.append("source: ");      break;
            case DESTINATION:   sb.append("destination: "); break;
            default:
                throw new IllegalStateException(
                        "unknown NodeIDAttr type " + type);
        }
        sb.append( StringLib.byteArrayToHex(value, 0, value.length) );
        return sb.toString();
    }
}   
