/* AbstractNode.java */
package org.xlattice.node;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.xlattice.Connection;
import org.xlattice.CryptoException;
import org.xlattice.DigSigner;
import org.xlattice.Key;
import org.xlattice.Overlay;
import org.xlattice.Node; 
import org.xlattice.NodeID;
import org.xlattice.Peer;
import org.xlattice.PublicKey;

import org.xlattice.crypto.RSAKey;

/**
 * Implementation of a basic XLattice Node.  Such a Node has a
 * unique 160-bit identifier, its NodeID; an RSA key, with which
 * the Node proves its identity; and a set of Overlays through 
 * which it communicates.
 *
 * XXX ITEMS STORED IN ArrayLists ARE FOUND USING indexOf(), WHICH
 * XXX USES equals(), WHICH * MUST * HAVE BEEN IMPLEMENTED FOR THOSE
 * XXX OBJECTS.
 * 
 * @author Jim Dixon
 */
abstract public class AbstractNode implements Node {

    /** cryptographic identity, a PKC key */
    private final Key _key;
    /** node ID, a quasi-unique large number */
    protected final  NodeID nodeID;

    /** local file system */
    protected /* final */ String lfsDirName;
    protected /* final */ File   lfsDir;
    
    /** communications networks that the Node interfaces with */
    protected final ArrayList overlays;
    /** Peers */
    protected final ArrayList peers;
    /** active connections */
    protected final ArrayList connections;
   
    // CONSTRUCTORS /////////////////////////////////////////////////
    /**
     * XXX THIS MAKES SENSE ONLY IF THERE ARE protected SETTERS FOR ALL 
     * XXX PARAMETERS.  
     */
    protected AbstractNode()                    throws IOException {
        this (null, null, null, null, null);
    }
    /**
     * Create a node.  Neither the RSA key nor the NodeID may be null;
     * these values should have been determined by the configurer.  
     * The overlay array may be null.
     *
     * XXX As overlay configurations are determined by the configurer,
     * XXX arguably this parameter should be treated the same as the
     * XXX RSA key and the NodeID.
     *
     * XXX Tracker bug 1483361: we need a way to receive and send 
     * XXX messages, specifically a way to send peers keep-alives 
     * XXX at set intervals.
     *
     * Connections, Peers, and Overlays may be added and dropped 
     * dynamically.
     *
     * 
     * @param key      RSA key for the Node (implicitly, the key pair)
     * @param myID     NodeID, 160-bit identifier
     * @param myDir    this node's work directory; may be null
     * @param overlays communications networks the Node interfaces with
     */
    public AbstractNode (RSAKey key, NodeID myID, File myDir, 
                         Overlay [] myOverlays, Peer [] myPeers) 
                                                    throws IOException {
        if (key == null || myID == null || myID.value() == null) 
            throw new IllegalArgumentException(
                    "null key or nodeID");
        _key = key;
        nodeID   = new NodeID( myID.value() );    // makes deep copy
        lfsDir   = myDir;
        
        overlays = new ArrayList();
        if (myOverlays != null)
            for (int i = 0; i < myOverlays.length; i++) 
                overlays.add( myOverlays[i] );

        peers        = new ArrayList();
        if (myPeers != null)
            for (int i = 0; i < myPeers.length; i++) 
                peers.add( myPeers[i] );

        connections  = new ArrayList();
    }

    // Node INTERFACE ///////////////////////////////////////////////
    // IDENTITY ///////////////////////////////////////////
    /**
     * Returns deep copy of node ID.  This might be null.  
     * 
     * @return quasi-unique 160-bit value
     */
    public NodeID getNodeID () {
        NodeID newID = nodeID;
        if (newID != null) 
            newID = new NodeID( nodeID.value() );    // makes deep copy
        return newID;
    }
    /**
     * @return a copy of the RSA public key
     */
    public PublicKey getPublicKey() {
        return _key.getPublicKey();
    }
    
    public DigSigner getSigner()            throws CryptoException {
        return _key.getSigner("sha1");
    }
    // OVERLAYS ///////////////////////////////////////////
    public Node addOverlay(Overlay val) {
        overlays.add(val);
        return this;
    }
    public Overlay getOverlay(int n) {
        return (Overlay) overlays.get(n);
    }
    public Overlay removeOverlay (Overlay val) {
        if (val != null) {
            // XXX uses equals(), which must have been implemented
            int index = overlays.indexOf(val);
            if (index == -1) 
                val = null;
            else 
                val = (Overlay)overlays.remove(index);
        }
        return (Overlay)val;
    }
    public int sizeOverlays () {
        return overlays.size();
    }
    // PEERS ////////////////////////////////////////////// 
    public Node addPeer(Peer val) {
        peers.add(val);
        return this;
    }
    public Peer getPeer(int n) {
        return (Peer) peers.get(n);
    }
    public Peer removePeer (Peer val) {
        if (val != null) {
            // XXX uses equals(), which must have been implemented
            int index = peers.indexOf(val);
            if (index == -1)
                val = null;
            else 
                val = (Peer)peers.remove(index);
        }
        return (Peer)val;
    }
    public int sizePeers () {
        return peers.size();
    }
    // CONNECTIONS ////////////////////////////////////////
    public Node addConnection(Connection val) {
        connections.add(val);
        return this;
    }
    public Connection getConnection(int n) {
        return (Connection) connections.get(n);
    }
    public Connection removeConnection (Connection val) {
        if (val != null) {
            // XXX uses equals(), which must have been implemented
            int index = connections.indexOf(val);
            if (index == -1)
                val = null;
            else 
                val = (Connection) connections.remove(index);     // removes it
        }
        return (Connection)val;
    }
    public int sizeConnections () {
        return connections.size();
    }
    // EQUALS/HASHCODE ////////////////////////////////////
    public boolean equals(Object o) {
        return o == this;
    }
    public int hashCode() {
        return nodeID.hashCode();
    }
    // OTHER METHODS ////////////////////////////////////////////////
}
