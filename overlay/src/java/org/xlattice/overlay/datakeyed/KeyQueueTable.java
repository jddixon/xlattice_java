/* KeyQueueTable.java */
package org.xlattice.overlay.datakeyed;

import java.util.HashMap;
import java.util.Map;
import org.xlattice.NodeID;
import org.xlattice.overlay.GetCallBack;
import org.xlattice.util.NonBlockingLog;
import org.xlattice.util.StringLib;

/**
 *
 * XXX NodeIDs must be deep-copied.
 *
 * @author Jim Dixon
 */
public class KeyQueueTable {
    
    private final Map map;
    private final IMemCache memCache;

    protected final static NonBlockingLog debugLog
                        = NonBlockingLog.getInstance("debug.log");

    // CONSTRUCTORS /////////////////////////////////////////////////
    public KeyQueueTable (IMemCache mCache) {
        if (mCache == null)
            throw new IllegalArgumentException (
                    "null MemCache instance");
        memCache = mCache;
        map      = new HashMap();

    }
    // LOGGING //////////////////////////////////////////////////////
    protected void DEBUG_MSG(String msg) {
        if (debugLog != null)
            debugLog.message("KeyQueueTable" + msg);
    }
    // PROPERTIES ///////////////////////////////////////////////////


    public int size () {
        return map.size();
    }
    // TABLE OPERATIONS /////////////////////////////////////////////
    /**
     * Adds a callback to the queue on a NodeID.  If the queue already
     * exists, returns null.  Otherwise returns a reference to the 
     * newly created queue.
     *
     * @return reference to the KeyQueue, if created, otherwise null
     */
    public KeyQueue enqueue (NodeID id, GetCallBack cb) {
        boolean newQ = false;
        if (id == null || cb == null)
            throw new IllegalArgumentException (
                    "null NodeID or GetCallBack");
        KeyQueue keyQ = null;
        synchronized (map) {
            keyQ = (KeyQueue) map.get(id);
            if (keyQ == null) {
                newQ = true;
                keyQ = new KeyQueue (memCache, id, cb);
                map.put( id, keyQ);
            } else {
                keyQ.add(cb);
            }
        }
        DEBUG_MSG(".enqueue, id = " + StringLib.byteArrayToHex(id.value())
                + "\n    " 
                + (newQ ? "added new queue" : "used existing queue") );
        return newQ ? keyQ : null;        
    }
    /**
     * If there is a KeyQueue corresponding to the NodeID in the
     * table, this method removes it and returns a reference to the
     * KeyQueue to the caller.  If there is no such mapping, returns
     * null.
     *
     * @param id NodeID that is expected to be associated with a key queue
     * @return   the key queue or null
     */
    public KeyQueue remove (NodeID id) {
        if (id == null)
            throw new IllegalArgumentException ("null NodeID");
        synchronized (map) {
            return (KeyQueue) map.remove(id);
        }
    }
}
