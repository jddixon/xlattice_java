/* CountingBloom.java */
package org.xlattice.crypto.filters;

/**
 * Counting version of the Bloom filter.  Adds a 4-bit counter to each
 * bit in the Bloom filter, enabling members to be removed from the set
 * without having to recreate the filter from scratch.
 *
 * This class should be thread-safe.
 *
 * @author Jim Dixon
 */

public class CountingBloom extends BloomSHA1 {
       
    private NibbleCounters nibCounter;

    public CountingBloom (int m, int k) {
        super(m, k);
        nibCounter = new NibbleCounters(filterWords);
    }
    public CountingBloom (int m) {
        this (m, 8);
    }
    public CountingBloom () {
        this (20, 8);
    }

    /**
     * Clear both the underlying filter in the superclass and the
     * bit counters maintained here.
     */
    public final void clear() {
        synchronized (this) {
            super.doClear();
            nibCounter.clear();
        }
    }
    /**
     * Add a key to the set represented by the filter, updating counters
     * as it does so.  Overflows are silently ignored.
     *
     * @param b byte array representing a key (SHA1 digest)
     */
    public final void insert (byte[]b) {
        synchronized (this) {
            ks.getOffsets(b);
            for (int i = 0; i < k; i++) {
                filter[wordOffset[i]] |=  1 << bitOffset[i];
                nibCounter.inc(wordOffset[i], bitOffset[i]);
            }
            count++;
        }
    }
    /** 
     * Remove a key from the set, updating counters while doing so.
     * If the key is not a member of the set, no action is taken.
     * However, if it is a member (a) the count is decremented, 
     * (b) all bit counters are decremented, and (c) where the bit
     * counter goes to zero the corresponding bit in the filter is
     * zeroed. [No change in code, but jdd clarified these comments
     * 2005-03-29.]
     *
     * @param b byte array representing the key to be removed.
     */
    public final void remove (byte[]b) {
        synchronized (this) {
            if (isMember(b)) {     // calls ks.getOffsets
                for (int i = 0; i < k; i++) {
                    int newCount = nibCounter.dec(wordOffset[i], bitOffset[i]);
                    if (newCount == 0) {
                        filter[wordOffset[i]] &=  ~(1 << bitOffset[i]);
                    }
                }
                count--;
            }
        }
    }
}
