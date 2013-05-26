/* JobQueue.java */
package org.xlattice.util.threads;

import org.xlattice.util.Queue;     // not thread safe

/** 
 * A queue of fixed or variable-length capacity.  
 *
 * This is a thin wrapper around Queue, adding 
 * <ul>
 *   <li> the notion of capacity</li>
 *   <li> a type for the objects stored in the queue, Runnable</li> 
 *   <li> the constraint that these objects must not be null</li>
 * </ul>
 *
 * This class is not thread safe.  Using methods must provide external 
 * synchronization.  
 *
 * @author Jim Dixon
 */
public class JobQueue {

    private final int maxQueueLen;
    public static final int MIN_CAPACITY = 8;
    private final Queue myQ;
    
    /** 
     * Create a JobQueue of fixed capacity if n is positive and 
     * of indefinite capacity if n is -1;
     */
    public JobQueue (int n) {
        if (n <= 0 && n != -1)
            throw new IllegalArgumentException(
                                    "non-positive queue length");
        if (n == -1)
            maxQueueLen = Integer.MAX_VALUE;
        else
            maxQueueLen = Math.max ( n, MIN_CAPACITY);
        myQ = new Queue();
    }

    /** 
     * If the list is of fixed capacity, returns that fixed
     * capacity; otherwise returns -1, denoting a variable-length list.
     *
     * @return the current capacity of a fixed-length list or -1
     */
    public int capacity() {
        if (maxQueueLen == Integer.MAX_VALUE)
            return -1;
        else
            return maxQueueLen;
    }
    /** 
     * Remove first object from the queue, if there is such a first
     * object.  If there is no such first Runnable, returns null.
     *
     * @return that first Runnable, which is also removed, or null
     */
    public Runnable dequeue () {
        return (Runnable) myQ.dequeue();
    }
    /** 
     * Add object at the end of the queue.  The object may not be
     * null.
     *
     * @param o Runnable being added to the queue
     * @return  whether the operation succeeded
     */
    public boolean enqueue (Runnable o) {
        if (o == null)
            throw new IllegalArgumentException ("null Runnable");
        if (myQ.size() + 1 > maxQueueLen) 
            return false;
        myQ.enqueue(o);
        return true;
    }
    public boolean isEmpty() {
        return myQ.isEmpty();
    }
    public int size() {
        return myQ.size();
    }
}
