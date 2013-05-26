/* Queue.java */
package org.xlattice.util;

/**
 * A simple, unsynchronized queue.
 *
 * This class is not thread safe.  If used in a multi-threaded 
 * environment, operations must be synchronized externally.
 *
 * @author Jim Dixon
 */
public final class Queue {

    private   int count;
    protected SinglyLinkedNode head;
    protected SinglyLinkedNode tail;
    
    public Queue () {
        count = 0;
    }
    public Queue (Object o) {
        head = tail = new SinglyLinkedNode (o);
        count = 1;
    }
    public Object dequeue () {
        Object o = null;
        
        if (head != null) {
            if (head == tail)
                tail = null;
            o = head.value;
            head.value = null;  // do our bit for garbage collection
            head = head.next;
            count--;
        }
        return o;
    }
    /**
     * @param o Object being added to the queue (as value of node)
     */
    public void enqueue(Object o) {
        if (tail == null) {
            head = tail = new SinglyLinkedNode (o);
            count = 1;
        } else {
            tail = tail.next = new SinglyLinkedNode (o);
            count++;
        }
    }
    public boolean isEmpty() {
        return head == null;
    }
    public int size() {
        return count;
    }
}
