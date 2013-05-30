/* KeyQueue.java */
package org.xlattice.overlay.datakeyed;

import org.xlattice.NodeID;
import org.xlattice.overlay.CallBack;
import org.xlattice.overlay.GetCallBack;
import org.xlattice.util.ArrayStack;
import org.xlattice.util.NonBlockingLog;
import org.xlattice.util.StringLib;

/**
 * When a data item is not found in the in-memory cache (MemCache),
 * an instance of this class is queued on the key, a NodeID.  If
 * the data item is found on disk or otherwise fetched, each queue
 * member is called back with a success status code.  If the item
 * cannot be found, each is called with an appropriate failure code.
 *
 * @author Jim Dixon
 */
public class KeyQueue implements GetCallBack {

    protected final static NonBlockingLog debugLog
                        = NonBlockingLog.getInstance("debug.log");

    private final ArrayStack keyQ;
    /** necessary for updating MemCache */
    private final IMemCache memCache;
    private final NodeID   myID;     
   
    // CONSTRUCTORS /////////////////////////////////////////////////
    /** Unreachable no-arg constructor. */
    private KeyQueue() {
        memCache = null;
        keyQ     = null;
        myID     = null;
    }

    /**
     * The queue is always constructed when we have an item but no
     * queue to put it in.  So we make the queue and add that item
     * to it.
     * 
     * @param id  NodeID that these CallBacks are waiting for
     * @param cb  CallBack that is being queued up
     * @throws IllegalArgumentException if an argument is null
     */
    public KeyQueue (IMemCache mCache, NodeID id, GetCallBack cb) {
        if (mCache == null || id == null || cb == null)
            throw new IllegalArgumentException(
                    "null MemCache or NodeID or GetCallBack");
        DEBUG_MSG(" constructor, keyQ on " 
                + StringLib.byteArrayToHex(id.value()));
        memCache = mCache;
        myID     = id;
        keyQ     = new ArrayStack();
        keyQ.push (cb);
    }
    // LOGGING //////////////////////////////////////////////////////
    protected void DEBUG_MSG(String msg) {
        if (debugLog != null)
        debugLog.message("KeyQueue" + msg);
    }
    // PROPERTIES ///////////////////////////////////////////////////
    /**
     * This method must be externally synchronized.
     *
     * @param  cb  CallBack that is being queued up
     * @throws IllegalArgumentException if argument is null
     */
    public void add (GetCallBack cb) {
        if (cb == null)
            throw new IllegalArgumentException ("null GetCallBack");
        DEBUG_MSG(".add(callback), keyQ on " 
                + StringLib.byteArrayToHex(myID.value()));
        keyQ.push(cb);
    }
    public NodeID getNodeID() {
        return myID;
    }
    public int size () {
        synchronized (keyQ) {
            return keyQ.size();
        }
    }
    // INTERFACE GetCallBack ////////////////////////////////////////
    private int status = -1;
    /**
     * If whatever was requested was found, it is returned as the
     * value of the byte array and the status code is zero; otherwise 
     * the byte array is null and the status code is non-zero.
     *
     * @param status application-specific status code
     * @param data   requested value as byte array or null if failure
     */
    public void finishedGet (int status, byte[] data) {
        DEBUG_MSG(".finishedGet, "
                + StringLib.byteArrayToHex(myID.value())
                + ":\n    status = " + status
                + ", " + keyQ.size() + " callbacks pending");
        this.status = status;
        byte[] myData = null;
        if (status == CallBack.OK) {
            myData = data;
            if (myData != null)           // let's be paranoid
                memCache.add (myID, myData);
        }
        synchronized (keyQ) {
            int count = keyQ.size();
            for (int i = 0; i < count; i++) {
                GetCallBack cb = (GetCallBack) keyQ.peek(i);
                cb.finishedGet(status, myData);
            }
            keyQ.clear();
        }
    }
    /**
     * Useful only for debugging.
     */
    public int getStatus () {
        return status;
    }
}
