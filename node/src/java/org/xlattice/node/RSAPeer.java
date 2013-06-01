/* RSAPeer.java */
package org.xlattice.node;

import org.xlattice.*;
import org.xlattice.crypto.RSAPublicKey;

public class RSAPeer                         extends Peer {

    // CONSTRUCTORS /////////////////////////////////////////////////
    public RSAPeer (NodeID myNodeID, RSAPublicKey myPubkey,
                        Overlay[] myOverlays, Connector[] myConnectors) {
        super (myNodeID, 
                new RSAPublicKey(myPubkey),             // deep copy
                myOverlays, myConnectors);
    }
    // IDENTITY ///////////////////////////////////////////
    public PublicKey getPublicKey() {
        return new RSAPublicKey((RSAPublicKey)pubkey);  // deep copy
    }
    // EQUALS/HASH ////////////////////////////////////////
    public boolean equals (Object o) {
        if (super.equals(o)) {
            RSAPeer other = (RSAPeer) o;
            return ((RSAPublicKey)pubkey)
                .equals ((RSAPublicKey)other.pubkey);
        } else {
            return false;
        }
    }
    public int hashCode() {
        return nodeID.hashCode() ^ ((RSAPublicKey)pubkey).hashCode();
    }
    // SERIALIZATION ////////////////////////////////////////////////
}
    
