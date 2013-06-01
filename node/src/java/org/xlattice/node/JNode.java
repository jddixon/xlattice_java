/* JNode.java */
package org.xlattice.node;

import java.io.File;
import java.io.IOException;

import org.xlattice.NodeID;
import org.xlattice.Overlay;
import org.xlattice.Peer;
import org.xlattice.crypto.RSAKey;
import org.xlattice.crypto.RSAPublicKey;
import org.xlattice.transport.IPAddress;

/**
 * Represents a Node which will be run in a separate JVM (Java 
 * virtual machine) by the runner creating an instance of this
 * class.
 *
 * A JNode differs from a TNode in that (a) it executes in a 
 * separate JVM, (b) it has a master, and (c) it reserves a 
 * control channel for communicating with the master.  The control
 * channel is overlay zero, with transport UDP, protocol XL, and 
 * a local IP4 address.
 */
public class JNode                              extends TNode {

    protected final RSAPublicKey masterKey;
    protected final IPAddress    masterAddr;

    public JNode(RSAKey myKey, NodeID myID, File myDir,
                 Overlay [] myOverlays, Peer[] myPeers,
                 RSAPublicKey mKey, IPAddress mAddr)
                                                throws IOException {
        super(myKey, myID, myDir, myOverlays, myPeers);
        if (mKey != null && mAddr != null) {
            masterKey  = mKey;
            masterAddr = mAddr;
        } else {
            masterKey  = null;
            masterAddr = null;
        }
    }


    // MAIN /////////////////////////////////////////////////////////
    public static void usage(String msg) {
        StringBuffer sb = new StringBuffer(); 
        if (msg != null)
            sb.append(msg).append("\n");
        sb.append("usage: jnode.sh [options]+\n")
          .append("where the options are:\n")
          .append("  -c serialized-XML-configuration\n")
          .append("  -d directory-name\n")
          .append("  -k master RSA public key\n")
          .append("  -m master's IPv4 address (host and port number)\n")
          .append("The configuration must not contain embedded newlines.\n")
          .append("If the configuration is omitted, the directory must\n")
          .append("contain a valid configuration in xlattice.xml.\n")
          .append("If the directory name is omitted, the node has no local file system.\n");
        System.out.println(sb.toString());
    }
    public static void usage() {
        usage (null);
    }
          
    /**
     * Invoked with either a serialized XML configuration file or with
     * the name of a directory or both.  
     */
    public static void main (String[] args) {

        usage();

    }
}
