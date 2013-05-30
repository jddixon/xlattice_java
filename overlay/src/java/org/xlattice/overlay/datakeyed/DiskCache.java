/* DiskCache.java */
package org.xlattice.overlay.datakeyed;

import org.xlattice.NodeID;
import org.xlattice.crypto.filters.CountingBloom;
import org.xlattice.overlay.CallBack;
import org.xlattice.overlay.GetCallBack;
import org.xlattice.overlay.DelCallBack;
import org.xlattice.overlay.PutCallBack;
import org.xlattice.overlay.DataKeyedReader;
import org.xlattice.overlay.DataKeyedWriter;

/**
 * @author Jim Dixon
 */

public class DiskCache implements IDiskCache {

    // PRIVATE MEMBERS //////////////////////////////////////////////
//  private static final HashMap map = new HashMap();
   
    private static DiskCache _instance;
    /** the path from here to xlattice/ */
    private static String _pathToXLattice;
    
    private final AbstractDisk disk;
    private final CountingBloom bloom;
    private final DiskIOThreadPool threadPool;
    
    // CONSTRUCTORS AND SUCH ////////////////////////////////////////
    
    /**
     * @param _pathToXLattice what precedes "xlattice/..." 
     */
    private DiskCache () {
        disk  = new FlatDisk(_pathToXLattice);
        int m = 20;
        int k =  6;
        bloom = new CountingBloom(m, k);    // 2^m bits, k functions
        threadPool = new DiskIOThreadPool(this);
    }
    /**
     * Returns a reference to the single DiskCache instance 
     * corresponding to the relative path given.
     *
     * XXX The first call could fix the maximum size of the
     * XXX logical disk.
     */
    public static final DiskCache getInstance (String s) {
        if (_instance == null) {
            if (s == null)
                s = "";
            _pathToXLattice = s; 
            _instance = new DiskCache();
        }
        /* BUG? NEEDS A MAP TO SUPPORT MULTIPLE INSTANCES */
        return _instance;
    }
    public static final DiskCache getInstance() {
        return getInstance("");
    }
    /**
     * XXX This should look at the underlying store, verifying its
     * XXX integrity, and then load the filter.
     */
    public void init () {
        clearFilter();                  // the Bloom filter
    }
    // PROPERTIES ///////////////////////////////////////////////////
    public String getPathToXLattice() {
        return _pathToXLattice;
    }
    /**
     * For testing, should be deprecated ASAP.
     */
    public DiskIOThreadPool getThreadPool() {
        return threadPool;
    }
    // BLOOM FILTER INTERFACE ///////////////////////////////////////
    /**
     * Clear the filter, including the nibble counters.  This should 
     * be thread safe.
     */
    private void clearFilter () {
        bloom.clear();
    }
    /**
     * The theoretical false positive rate is 
     *   (1 - e(-kN/M))^k
     * where k is the number of key functions, N is the number 
     * of keys in the filter, and there are 2^M bits in the 
     * filter.  
     * 
     * @param  n number of keys in the filter 
     * @return an approximation to the false positive rate
     */
    public final double falsePositives(int n) {
        return bloom.falsePositives(n);
    }
    /**
     * Add a key to the filter.
     */
    public void insert (NodeID id) {
        bloom.insert(id.value());
    }
    /**
     * This method has a non-zero false positive rate.  The filter 
     * can normally be set up to make this a very small number.
     *
     * @return whether a key is in the filter
     * @see #falsePositives
     */
    public boolean member (NodeID id) {
        return bloom.member(id.value());
    }
    /**
     * Remove a key from the filter.  
     */
    public void remove (NodeID id) {
        bloom.remove(id.value());
    }
    /**
     * A counter is incremented and decremented whenever a key is
     * inserted or removed.  The value of this counter is returned
     * as a crude estimate of the number of keys in the filter.
     *
     * @return the approximate number of keys in the filter
     */
    public int size () {
        return bloom.size();
    }
    // DISK I/O QUEUES //////////////////////////////////////////////
    public void acceptReadJob(NodeID id, GetCallBack cb) {
        threadPool.accept ( new DiskReadJob (id, cb) );
    }
    public void acceptWriteJob(NodeID id, byte[] data, PutCallBack cb) {
        threadPool.accept ( new DiskWriteJob (id, data, cb) );
    }
    // OTHER METHODS ////////////////////////////////////////////////
}
