/* Peer.java */
package org.xlattice;

import java.util.ArrayList;

/**
 * A Peer is another Node, a neighbor.  
 * 
 * XXX As this has evolved, it begins to look like Node should 
 * XXX simply extend Peer.
 * 
 * @author Jim Dixon
 */
public abstract class Peer {

    protected final NodeID nodeID;
    /** Peer's PublicKey, possibly learned via nodeID */
    protected       PublicKey pubkey;
    protected final ArrayList overlays;
    protected final ArrayList connectors;

    // CONSTRUCTORS /////////////////////////////////////////////////
    public Peer(NodeID id) {
        this (id, null, null, null);
    }
    public Peer(NodeID id, PublicKey p, Overlay[] o, Connector[] c) {
    
        if (id == null) 
            throw new IllegalArgumentException("null NodeID");
        nodeID = (NodeID)id.clone();
        pubkey = p;
        overlays = new ArrayList();
        if (o != null)
            for (int i = 0; i < o.length; i++)
                overlays.add(o[i]);
        
        connectors = new ArrayList();
        if (c != null)
            for (int i = 0; i < c.length; i++)
                overlays.add(c[i]);
    }
    // IDENTITY /////////////////////////////////////////////////////
    /** @return the 160-bit value identifying the Peer */
    public NodeID getNodeID() {
        return nodeID;
    }
    /** @return the Peer's public key */
    public PublicKey getPublicKey() {
        return pubkey;
    }
    public void setPublicKey(PublicKey p) {
        if (p == null)
            throw new IllegalArgumentException("null PublicKey");
        pubkey = p;
    }
    // OVERLAYS /////////////////////////////////////////////////////
    public void addOverlay (Overlay o) {
        if (o == null) 
            throw new IllegalArgumentException("null Overlay");
        overlays.add(o);
    }
    /** 
     * @return a count of the number of overlays the peer can be
     *         accessed through
     */
    public int sizeOverlays () {
        return overlays.size();
    }
    /** @return how to access the peer (transport, protocol, address) */
    public Overlay getOverlay(int n) {
        return (Overlay) overlays.get(n);
    }
    
    // CONNECTORS ///////////////////////////////////////////////////
    public void addConnector (Connector c) {
        if (c == null)
            throw new IllegalArgumentException("null Connector");
        connectors.add(c);
    }
    /** 
     * @return a count of known Connectors for this Peer 
     * @deprecated
     */
    public int connectorCount() {
        return sizeConnectors();
    }
    /** @return a count of known Connectors for this Peer */
    public int sizeConnectors() {
        return connectors.size();
    }
    /** 
     * Return a Connector, an Address-Protocol pair identifying
     * an Acceptor for the Peer.  Connectors are arranged in order
     * of preference, with the zero-th Connector being the most
     * preferred.
     *
     * XXX Could as easily return an EndPoint.
     * 
     * @return the Nth Connector 
     */
    public Connector getConnector(int n) {
        return (Connector)connectors.get(n);
    }

    // EQUALS/HASHCODE //////////////////////////////////////////////
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Peer)) 
            return false;
        Peer other = (Peer)o;
        return nodeID.equals(other.nodeID);
            // THINK ABOUT publicKey.equals(other.publicKey)
    }
    public int hashCode() {
        return nodeID.hashCode();
            // ^ publicKey.hashCode();
    }
}
