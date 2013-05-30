/* MemCache.java */
package org.xlattice.overlay.datakeyed;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.xlattice.CryptoException;
import org.xlattice.NodeID;
import org.xlattice.crypto.SHA1Digest;
import org.xlattice.crypto.SignedList;
import org.xlattice.overlay.CallBack;
import org.xlattice.overlay.DataKeyed;
import org.xlattice.overlay.GetCallBack;
import org.xlattice.overlay.DelCallBack;
import org.xlattice.overlay.PutCallBack;
import org.xlattice.util.NonBlockingLog;
import org.xlattice.util.StringLib;         // DEBUG

/**
 *
 * This class is intended to be thread safe.
 * @author Jim Dixon
 */
public class MemCache implements IMemCache {
    /** we are a singleton */
    private static MemCache _instance;
    private static String   _pathToXLattice = "";
    
    /** items in cache */
    public final static int  DEFAULT_MAX_COUNT = 1024;
    /** bytes in cache */
    public final static long DEFAULT_MAX_BYTES = 128*1024;

    private        int  maxCount   = DEFAULT_MAX_COUNT;
    private        long maxBytes   = DEFAULT_MAX_BYTES;

    private        long cacheBytes = 0L;
    private        Map keysInMemory;

    private final KeyQueueTable keyQTable;
    private final DiskCache diskCache;
    
    protected final static NonBlockingLog debugLog 
        = NonBlockingLog.getInstance("debug.log");
    protected final static NonBlockingLog errorLog 
        = NonBlockingLog.getInstance("error.log");
    
    // CONSTRUCTORS AND SUCH ////////////////////////////////////////
    /**
     *
     * @param countHint rough estimate of expected item count
     * @param maxByteCount maximum size of cache in bytes
     */
    private MemCache () {
        diskCache    = DiskCache.getInstance(_pathToXLattice);
        keysInMemory = new HashMap (maxCount);
        keyQTable    = new KeyQueueTable(this);
    }
    public final static MemCache getInstance() {
        return getInstance("");
    }
    public final static MemCache getInstance(String s) {
        if (_instance == null) {
            if (s == null)
                s = "";
            _pathToXLattice = s;
            _instance = new MemCache();
        }
        return _instance;
    }
    // LOGGING //////////////////////////////////////////////////////
    /**
     * Subclasses should override.
     */
    public void DEBUG_MSG(String msg) {
        if (debugLog != null)
            debugLog.message("MemCache" + msg);
    }
    public void ERROR_MSG(String msg) {
        if (errorLog != null)
            errorLog.message("MemCache" + msg);
    }
    // PROPERTIES ///////////////////////////////////////////////////
    public void add (NodeID id, byte[] b) {
        if (id == null || b == null)
            throw new IllegalArgumentException(
                    "null NodeID or byte array");
        DEBUG_MSG(".add: nodeID = " 
                + StringLib.byteArrayToHex(id.value()));
        synchronized(keysInMemory) {
            keysInMemory.put(id, b.clone());
        }
    }
    public long byteCount () {
        synchronized(keysInMemory) {
            return cacheBytes;
        }
    }
    public void clear() {
        synchronized(keysInMemory) {
            keysInMemory.clear();
            cacheBytes = 0L;
        }
    }
    public int itemCount () {
        synchronized(keysInMemory) {
            return keysInMemory.size();
        }
    }
    public String getPathToXLattice () {
        return _pathToXLattice;
    }
    // INTERFACE DataKeyedReader ////////////////////////////////////
    /**
     * Retrieve data by content key (content hash).
     *
     * Any integrity checks should be performed elsewhere.
     *
     * Intended to be thread safe.
     */
    public void get (NodeID id, GetCallBack cb) {
        if (id == null) {
            cb.finishedGet(CallBack.BAD_ARGS, null);
            return;
        }
        DEBUG_MSG(".get for " 
                    + StringLib.byteArrayToHex(id.value()));
        byte[] data = null;
        int status;
        synchronized (keysInMemory) {
            data = (byte[]) keysInMemory.get(id);
        }
        if (data != null) {
            DEBUG_MSG(".get: data is in cache for " 
                    + StringLib.byteArrayToHex(id.value()));
            cb.finishedGet (CallBack.OK, data);
        } else {
            DEBUG_MSG(".get: data is NOT in cache for " 
                    + StringLib.byteArrayToHex(id.value()));
            KeyQueue keyQ = (keyQTable.enqueue (id, cb));
            if (keyQ != null) {                     // if new key queue
                DEBUG_MSG(".get: starting disk read for " 
                    + StringLib.byteArrayToHex(id.value()));
                diskCache.acceptReadJob (id, keyQ); //    start a disk read
            }
        }
    } 
    /**
     * Retrieve a serialized SignedList, given its key, calculated
     * from the RSA public key and title of the list.
     *
     * @param nodeID whose value is the key
     */
    public void getSigned (NodeID nodeID, GetCallBack listener) {   
        byte[] data = null;
        int status;
        // STUB
        status = CallBack.NOT_IMPLEMENTED;
        listener.finishedGet (status, data);
    }

    // INTERFACE DataKeyedWriter ////////////////////////////////////
    public void delete (NodeID nodeID, DelCallBack listener) {
        int status;
        // STUB
        status = CallBack.NOT_IMPLEMENTED;
        listener.finishedDel (status);
    }
    /**
     * Store an item by content hash.
     * 
     * @param id  NodeID whose value is the SHA1 content hash of the buffer
     * @param b   data being stored
     */
    public void  put (NodeID id, byte[] b, PutCallBack cb) {
        if (cb == null)
            throw new IllegalArgumentException("null callback");
        int status = CallBack.OK;
        if (id == null || b == null || b.length == 0) 
            status = CallBack.BAD_ARGS;
        else {
            // verify the hash
            NodeID checkID = null;
            try {
                SHA1Digest sha1 = new SHA1Digest();
                checkID = new NodeID(sha1.digest(b));
                if (checkID == null || !checkID.equals(id))
                    throw new CryptoException();
            } catch (CryptoException ce) {
                status = CallBack.VERIFY_FAILS;
            }
        }
        if (status != CallBack.OK)
            cb.finishedPut(status);
        else 
            _put(id, b, cb);
    }
    public void put (byte[] b, PutCallBack cb) {
        if (cb == null)
            throw new IllegalArgumentException("null callback");
        int status = CallBack.OK;
        if (b == null || b.length == 0)
            status = CallBack.BAD_ARGS;

        // calculate the hash
        NodeID id = null;
        try {
            SHA1Digest sha1 = new SHA1Digest();
            id = new NodeID(sha1.digest(b));
        } catch (CryptoException ce) {
            status = CallBack.VERIFY_FAILS;
        }
        if (status != CallBack.OK)
            cb.finishedPut(status);
        else 
            _put(id, b, cb);
    } 
    public void putSigned (NodeID nodeID,  SignedList list,
                                           PutCallBack listener) {
        int status;
        // STUB
        status = CallBack.NOT_IMPLEMENTED;
        listener.finishedPut (status);;
    }
    /** 
     * Put with hash (NodeID value) guaranteed to be correct.
     */
    private void _put (NodeID id, byte[] b, PutCallBack cb) {
        Object cachedData;
        synchronized (keysInMemory) {
            cachedData = keysInMemory.get(id);
            if (cachedData == null) {
                // XXX SHOULD CHECK keysInMemory.size() against maxCount
                // XXX SHOULD CHECK cacheBytes against maxBytes
                keysInMemory.put(id, b.clone());
                cacheBytes += b.length;
            }
        } 
        if (cachedData != null) 
            cb.finishedPut(CallBack.EXISTS);
        else 
            diskCache.acceptWriteJob (id, b, cb);
    }
}
