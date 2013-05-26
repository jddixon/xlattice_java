/* Node.java */
package org.xlattice;

/**
 * A Node is uniquely identified by a NodeID and can satisfy an 
 * identity test constructed using its public key.  That is, it
 * can prove that it holds the private key materials corresponding
 * to the public key.
 *
 * XXX A Node also participates in some number of Overlays and will
 * XXX usually have a number of Peers.  This interface needs to be
 * XXX elaborated.
 *
 * @author Jim Dixon
 */
public interface Node                           extends Runnable {

    // IDENTITY /////////////////////////////////////////////////////
    /** @return the 20-byte/160-bit node ID */
    public NodeID getNodeID ();
    
    /** @return the public key associated with the Node */
    public PublicKey getPublicKey();

    /** @return a DigSigner used to make digital signatures */
    public DigSigner getSigner()            throws CryptoException;
   
    // OVERLAYS /////////////////////////////////////////////////////
    public Node addOverlay (Overlay v);
    public Overlay getOverlay (int n);
    public Overlay removeOverlay (Overlay v);
    public int sizeOverlays ();

    // PEERS ////////////////////////////////////////////////////////
    public Node addPeer (Peer v);
    public Peer getPeer (int n);
    public Peer removePeer (Peer v);
    public int sizePeers ();

    // CONNECTIONS //////////////////////////////////////////////////
    public Node addConnection (Connection v);
    public Connection getConnection (int n);
    public Connection removeConnection (Connection v);
    public int sizeConnections ();

    // INTERFACE Runnable ///////////////////////////////////////////
    public void run();

}
