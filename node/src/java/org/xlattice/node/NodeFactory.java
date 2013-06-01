/* NodeFactory.java */
package org.xlattice.node;

import java.io.File;
import java.io.IOException;

import org.xlattice.crypto.RSAPublicKey;
import org.xlattice.transport.IPAddress;

public class NodeFactory {


    protected NodeFactory() {
        /* STUB */
    }

    /** 
     * Create a TNode with the specified node configuration and using 
     * directory passed for any local storage.  If the configuration
     * is null, the directory must contain a valid configuration file
     * named xlattice.xml.  If the directory is null, the configuration
     * may not be, and the node will have no local storage.  The TNode
     * runs as a separate thread in the same JVM (Java virtual machine)
     * as the caller.  If successful, this method returns a reference to
     * the TNode created.
     *
     * @paran nc      node configuration, an object tree
     * @param dir     directory to be used for local store
     * @return a reference to the TNode created or null
     */
    TNode createTNode (NodeConfig nc, File dir) throws IOException {
        /* STUB */
        return null;
    }
  
    /** 
     * Create a JNode with the specified node configuration and using 
     * directory passed for any local storage.  If the configuration
     * is null, the directory must contain a valid configuration file
     * named xlattice.xml.  If the directory is null, the configuration
     * may not be, and the node will have no local storage.  The JNode
     * runs in a different JVM (Java virtual machine) from that of the
     * caller.
     * 
     * If the RSA public key and IPv4 address and port number of the
     * master are specified, then the JNode will ping the master once
     * it begins running and will accept commands from the master.
     *
     * If the JNode is created successfully, this method returns a 
     * reference to a JNode control object.  Otherwise it returns null.
     *
     * @paran nc      node configuration, an object tree
     * @param dir     directory to be used for local store
     * @param pubkey  base64-encoded RSA public key 
     * @param master  IPv4 address and port number of master
     * @return a reference to the control object for the JNode created
     */
    JNodeCtl createJNode (NodeConfig nc, File dir, 
                          RSAPublicKey pubkey, IPAddress master)
                                                throws IOException {
        /* STUB */
        return null;
    }
}
