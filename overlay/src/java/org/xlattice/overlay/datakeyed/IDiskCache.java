/* IDiskCache.java */
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

public interface IDiskCache {

    /**
     * Returns a reference to the single DiskCache instance 
     * corresponding to the relative path given.
     *
     * XXX The first call could fix the maximum size of the
     * XXX logical disk.
     *
     * XXX These static final methods cannot appear in the interface.
     *
     */
    // public static final DiskCache getInstance (String s);
    // public static final DiskCache getInstance();

    /** initializes (clears) the underlying Bloom filter */
    public void init ();

    /** badly named */
    public String getPathToXLattice();
    
    /** For testing, should be deprecated ASAP. */
    public DiskIOThreadPool getThreadPool();
    
    // BLOOM FILTER INTERFACE ///////////////////////////////////////
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
    public double falsePositives(int n);
    
    /**
     * Add a key to the filter.
     */
    public void insert (NodeID id);
    
    /**
     * This method has a non-zero false positive rate.  The filter 
     * can normally be set up to make this a very small number.
     *
     * @return whether a key is in the filter
     * @see #falsePositives
     */
    public boolean member (NodeID id);
    
    /**
     * Remove a key from the filter.  
     */
    public void remove (NodeID id); 
    
    /**
     * A counter is incremented and decremented whenever a key is
     * inserted or removed.  The value of this counter is returned
     * as a crude estimate of the number of keys in the filter.
     *
     * @return the approximate number of keys in the filter
     */
    public int size ();
    
    // DISK I/O QUEUES //////////////////////////////////////////////
    public void acceptReadJob(NodeID id, GetCallBack cb);
    
    public void acceptWriteJob(NodeID id, byte[] data, PutCallBack cb);
}
