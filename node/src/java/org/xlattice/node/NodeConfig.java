/* NodeConfig.java */
package org.xlattice.node;

import java.util.ArrayList;

import org.xlattice.CryptoException;
import org.xlattice.NodeID;
import org.xlattice.crypto.RSAKey;
import org.xlattice.util.Base64Coder;

/**
 * Holder for Node configuration information, for use with
 * org.xlattice.corexml.bind.  Bind only recognizes a limited
 * number of types: primitives and String.
 * 
 *
 * @author Jim Dixon
 */
public class NodeConfig {

    private Base64Coder coder = new Base64Coder();

    /** 160-bit quasi-unique identifier */
    private NodeID  nodeID_;
    /** RSA Key; the RSA public key is used to prove the Node's identity */
    private RSAInfo _rsa;
    /** the Overlays the Node interfaces with */
    private ArrayList overlays = new ArrayList();

    public NodeConfig () {}

    // NODE ID //////////////////////////////////////////////////////
    // String interface for corexml.bind //////////////////
    /** @return the NodeID as a base-64 encoded String */
    // FIX 2011-08-23 a bug somewhere ...
    public String getID() {
        return getId();
    }
    // END FIX
    public String getId() {
        return coder.encode(nodeID_.value());
    }
    /** 
     * Sets the NodeID from a base-64 encoded value.  The NodeID is
     * 20 bytes long, so should be 28 bytes long when base-64 encoded.
     */
    // FIX 2011-08-23 a bug somewhere ...
    public void setID(String id) {
        setId(id);
    }
    // END FIX
    public void setId (String id) {
        if (id == null)
            throw new IllegalArgumentException ("null NodeID");
        if (id.length() != 28)
            throw new IllegalArgumentException(
                    "not a valid 20-byte NodeID");
        nodeID_ = new NodeID( coder.decode(id) );
    }
    // NodeID interface for internal use //////////////////
    /** @return the NodeID in object form */
    public NodeID getNodeID () {
        return nodeID_;
    }
    /**
     * Sets the NodeID.
     * @param nodeID the NodeID in object form
     */
    public void setNodeID (NodeID nodeID) {
        nodeID_ = nodeID;
    }

    // RSA KEY //////////////////////////////////////////////////////
    /**
     * XXX This and the setKey() method should be given better
     * XXX names, possibly get/setKeyInfo().
     * @return a set of numbers specifying the RSA key
     */
    public RSAInfo getKey () {
        return _rsa;
    }
    /**
     * Set the RSA key specifier.
     * @param rsa data structure whose numbers specify the key
     */
    public void setKey (RSAInfo rsa) {
        _rsa = rsa;
    }
    /**
     * Convenience method.
     */
    public RSAKey getRSAKey()                   throws CryptoException {
        return new RSAKey ( _rsa.getBigP(), _rsa.getBigQ(), 
                            _rsa.getBigE(), _rsa.getBigD() );
    }
    // OVERLAYS /////////////////////////////////////////////////////
    /**
     * Add another to the set of Overlays this node interfaces with.
     *
     * @param overlay a data structure describing the Overlay
     */
    public void addOverlay (OverlayConfig overlay) {
        overlays.add (overlay);
    }
    /**
     * @return the descriptor for the Nth Overlay
     */
    public OverlayConfig getOverlay (int n) {
        return (OverlayConfig) overlays.get(n);
    }
    /** @return a count of the number of Overlays associated with this Node */
    // FIX 2011-08-23
    public int sizeOverlaies() {
        return sizeOverlay() ;
    }
    // END FIX
    public int sizeOverlay () {
        return overlays.size();
    }
    // PEERS ////////////////////////////////////////////////////////
}
