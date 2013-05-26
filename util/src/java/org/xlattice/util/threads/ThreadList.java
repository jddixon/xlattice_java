/* ThreadList.java */
package org.xlattice.util.threads;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Iterator;

/**
 * A fixed-size list of Killables, Threads which can run jobs for us.
 *
 * This class is not thread-safe.  All external calls should be
 * synchronized on the class instance.
 *
 * XXX BAD IDEA: this class should be made thread-safe.
 *
 * XXX ERROR: removing a Thread doesn't affect the busy count!
 *
 * @author Jim Dixon
 */
public class ThreadList {

    public static final int MIN_CAPACITY = 8;

    // INSTANCE VARIABLES ///////////////////////////////////////////
    /** number of busy Threads */
    private       int busyCount;
    /** maximum number of Threads */
    private final int capacity;
    /** the thread list itself */
    private final ArrayList list;

    // CONSTRUCTORS /////////////////////////////////////////////////
    /**
     * @param n list capacity; at least MIN_CAPACITY
     */
    public ThreadList (int n) {
        capacity  = Math.max ( n, MIN_CAPACITY);
        list    = new ArrayList (capacity);
        busyCount = 0;
    }
    /**
     * Increase busy count.  If this is more than the _current_ list
     * size, we have an internal error.
     */
    public void startJob() {
        if (busyCount >= list.size())
            throw new IllegalStateException("too many jobs running");
        // don't optimize or count will be off after throw ;-)
        busyCount++;
    }
    /** Decrease busy count. */
    public void endJob() {
        if (busyCount <= 0)
            throw new IllegalStateException("negative number of jobs running");
        // likewise: decrement _after_ test
        busyCount--;
    }
    /** Is everyone busy? */
    public boolean allBusy() {
        return busyCount == list.size();  // was == capacity
    }
    /** Is everyone idle? */
    public boolean allIdle() {
        return busyCount == 0;
    }

    /** Add a thread to the lit. */
    public void add (Killable o) {
        list.add(o);
    }
    /** @return the fixed capacity of the list */
    public int capacity() {
        return capacity;
    }
    /** Mark all list members as dying. */
    public void die () {
        Iterator it = list.iterator();
        while (it.hasNext()) {
            Killable k = (Killable) it.next();
            k.die();
        }
    }
    /** @return the number of Killables in the list */
    public int size () {
        return list.size();
    }
    // XXX OBSOLETE OR BADLY NAMED //////////////////////////////////
    /**
     * Remove last object from list.
     *
     * XXX Utility doubtful, but it's in the tests.
     */
    public Killable pop () {
        return (Killable) list.remove( list.size() - 1);
    }
    /**
     * Remove a specific item from the list.  This does not kill the
     * thread.
     *
     * XXX SHOULD NOT BE USED
     *
     * @param  o    the object to be removed
     * @return true if the object was found in the list, false otherwise.
     */
    public boolean remove (Killable o) {
        return list.remove(o);
    }
}
