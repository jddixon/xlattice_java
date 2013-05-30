/* DataKeyedWriter.java */
package org.xlattice.overlay;

import java.io.IOException;
import java.io.File;
import java.nio.ByteBuffer;
import org.xlattice.CryptoException;
import org.xlattice.NodeID;
import org.xlattice.crypto.SignedList;

/**
 *
 * @author Jim Dixon
 */
public interface DataKeyedWriter {
    /**
     * Delete the data item whose hash or title key is the value of 
     * the nodeID.
     *
     * XXX This operation may be ambiguous.  It is intended for use
     * XXX in, for example, LRU caches.
     *
     * @param nodeID whose value of the SHA1 content hash
     * @return whether the operation is successful
     */
    public void delete (final NodeID nodeID, DelCallBack listener);
   
    /**
     * Store a data item whose content hash is the value of nodeID.
     * If an item with this key is already present, return false and
     * do not write to disk.  If there is no item with this key and
     * storage is successful, return true.
     * 
     * @param nodeID whose value is the SHA1 content hash of the buffer
     * @param b      data being stored
     */
    public void put (final NodeID nodeID, byte[] b, PutCallBack listener);
    /**
     * Store a SignedList.  The value of the NodeID is the key
     * calculated from the RSA public key and title of the list.
     * If a data item with this title key is already present, retrieve
     * it.  Replace it if the stored item verifies but has a more 
     * recent timestamp.
     * 
     * @param nodeID whose value is the key of the SignedList
     * @param list   the SignedList being stored
     * @return       whether the list was written to store
     */
    public void putSigned (final NodeID nodeID, SignedList list, 
                                                PutCallBack listener);
}
