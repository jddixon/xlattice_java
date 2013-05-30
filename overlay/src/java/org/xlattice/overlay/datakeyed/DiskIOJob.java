/* DiskIOJob.java */
package org.xlattice.overlay.datakeyed;

import org.xlattice.NodeID;
import org.xlattice.overlay.CallBack;
import org.xlattice.overlay.DataKeyed;

/**
 * @author Jim Dixon
 **/

public abstract class DiskIOJob implements Runnable {

    protected final NodeID    id;
    protected final CallBack  cb;
    private         DataKeyed myDisk;

    // CONSTRUCTORS /////////////////////////////////////////////////
    protected DiskIOJob (NodeID nodeID, CallBack callBack) {
        if (nodeID == null || callBack == null)
            throw new IllegalArgumentException ("null node ID or callback");
        id = nodeID;
        cb = callBack;
    }
    // PROPERTIES ///////////////////////////////////////////////////
    public CallBack getCallBack() {
        return cb;
    }
    public DataKeyed getDisk() {
        return myDisk;
    }
    public void setDisk (DataKeyed disk) {
        if (disk == null)
            throw new IllegalArgumentException("null disk");
        if (myDisk != null)
            throw new IllegalStateException("can't change disk once set");
        myDisk = disk;
    }
    public NodeID getNodeID() {
        return id;
    }
}
