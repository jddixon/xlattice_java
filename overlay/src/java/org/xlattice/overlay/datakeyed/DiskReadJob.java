/* DiskReadJob.java */
package org.xlattice.overlay.datakeyed;

import org.xlattice.NodeID;
import org.xlattice.overlay.DataKeyed;
import org.xlattice.overlay.GetCallBack;

/**
 * @author Jim Dixon
 **/

public class DiskReadJob extends DiskIOJob {

    // CONSTRUCTORS /////////////////////////////////////////////////
    public DiskReadJob (NodeID nodeID, GetCallBack callBack) {
        super(nodeID, callBack);
    }
    // INTERFACE Runnable ///////////////////////////////////////////
    /**
     * 
     * @throws NullPointerException if disk has not been set
     */
    public void run () {
        getDisk().get(id, (GetCallBack)cb);
    }
}
