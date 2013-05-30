/* DataKeyedReader.java */
package org.xlattice.overlay;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.xlattice.CryptoException;
import org.xlattice.NodeID;

/**
 * @author Jim Dixon
 **/

public interface DataKeyedReader {
    /** 
     * Retrieve data by content key (content hash).
     */
    public void get (final NodeID nodeID, GetCallBack listener);
    /**
     * Retrieve a serialized SignedList, given its key, calculated
     * from the RSA public key and title of the list.
     *
     * @param nodeID whose value is the key
     */
    public void getSigned (final NodeID nodeID, GetCallBack listener);
}
