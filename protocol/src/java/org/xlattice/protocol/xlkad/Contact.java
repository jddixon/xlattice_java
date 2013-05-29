/* Contact.java */
package org.xlattice.protocol.xlkad;

import java.net.InetAddress;
import org.xlattice.NodeID;

/**
 *
 * @author Jim Dixon
 */
public class Contact {

    // INSTANCE VARIABLES ///////////////////////////////////////////
    private final NodeID nodeID;
    private final InetAddress addr;
    private final int port;
    
    // CONSTRUCTORS /////////////////////////////////////////////////
    public Contact (byte[] id, InetAddress addr, int port) {
        nodeID = new NodeID(id);
        if (addr == null) 
            throw new IllegalArgumentException("null address");
        this.addr = addr;
        if (0 < port || port > 65535)
            throw new IllegalArgumentException("port out of range: " + port);
        this.port = port;
    }


}
