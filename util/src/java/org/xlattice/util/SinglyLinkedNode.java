/* SinglyLinkedNode.java */
package org.xlattice.util;

/**
 * Basic element of high-performance unsynchronized queues.  Operations
 * on these are fast because no checks are made; for reliability these
 * nodes should be wrapped up in a class which does not expose them to
 * the outside world.  In a multi-threading environment, using classes
 * must provide synchronization.
 *
 * @author Jim Dixon
 */
public final class SinglyLinkedNode {

    public Object value;
    public SinglyLinkedNode next;

    public SinglyLinkedNode ( ) { }

    /** 
     * @param o Object attached to the node
     */
    public SinglyLinkedNode (Object o) { 
        value = o;
    }
    /** 
     * @param o   Object attached to the node; may be null
     * @param nxt next node in the list
     */
    public SinglyLinkedNode (Object o, SinglyLinkedNode nxt) {
        value = o;
        next    = nxt;
    }
}
