/* DiskWriteJob.java */
package org.xlattice.overlay.datakeyed;

import org.xlattice.NodeID;
import org.xlattice.overlay.DataKeyed;
import org.xlattice.overlay.PutCallBack;

/**
 * @author Jim Dixon
 **/

public class DiskWriteJob extends DiskIOJob {

    private final byte[] myData;
    
    // CONSTRUCTORS /////////////////////////////////////////////////
    public DiskWriteJob (NodeID nodeID, byte[] data, PutCallBack callBack) {
        super(nodeID, callBack);
        if (data == null || data.length == 0)
            throw new IllegalArgumentException("null or empty byte array");
        myData = data;
    }
    // INTERFACE Runnable ///////////////////////////////////////////
    /**
     * 
     * @throws NullPointerException if disk has not been set
     */
    public void run () {
        getDisk().put(id, myData, (PutCallBack)cb);
    }
}
