/* KBucket.java */
package org.xlattice.protocol.xlkad;

import org.xlattice.NodeID;
import org.xlattice.util.Queue;

/**
 * A KBucket is a queue of up to K contacts.  When a communication
 * is received from a XLKad node, the contact is moved to the end of
 * the queue.  Under certain circumstances contacts are removed from
 * the front of the queue.  At any time a contact may be added at the
 * end of the list.  It must not be possible to add a contact more
 * than once.
 * 
 * @author Jim Dixon
 */
public class KBucket {

    public static final int K = 20;

    /** not thread-safe, must synchronize access */
    private final Queue queue;

    public KBucket() {
        queue = new Queue();
    }

    /**
     * Returns the number of contacts in the bucket, but is not
     * synchronized.
     */
    public int size() {
        return queue.size();
    }

}
