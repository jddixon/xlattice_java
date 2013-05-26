/* Stack.java */
package org.xlattice.util;

/**
 * A simple, unsynchronized stack.  Elements are stored in
 * a singly-linked list, with each node having a pointer to the 
 * node below it, if any.
 *
 * Operations on the stack do not throw exceptions.  In particular,
 * a peek() or pop() operation returns null if EITHER the stack is
 * empty OR a null value was pushed onto the stack.
 *
 * This class is not thread safe.  If used in a multi-threaded 
 * environment, operations must be synchronized externally.
 *
 * XXX It might make sense to maintain a pool of SinglyLinkedNodes, 
 * reusing them as needed.  There could be a ceiling on the pool
 * size, and the pool could be static.  Access would have to be
 * synchronized.  If this is done here, it should be done in Queue
 * as well.
 *
 * @author Jim Dixon
 */
public final class Stack {

    private   int count;
    protected SinglyLinkedNode top;
    protected SinglyLinkedNode base;
    
    public Stack () {
        count = 0;
    }
    public Stack (Object o) {
        top = base = new SinglyLinkedNode (o);
        count = 1;
    }
    public boolean isEmpty() {
        return base == null;
    }
    /**
     * Peek at the Object at the top of the stack, if any.  If a null has
     * been pushed on the stack, the peek will return null, which is
     * indistinguishable from the stack having been empty.  If the
     * distinction is important, you should call size() before this 
     * operation.
     *
     * @return the value at the top of the stack, or null if the
     *         stack is empty
     */
    public Object peek() {
        Object o = null;
        if (top != null) 
            o = top.value;
        return o;
    }
    /**
     * Pop the Object at the top of the stack, if any.  If a null has
     * been pushed on the stack, the pop will return null, which is
     * indistinguishable from the stack having been empty.  If the
     * distinction is important, you should call size() before this 
     * operation.
     *
     * @return the value at the top of the stack, or null if the
     *         stack is empty
     */
    public Object pop () {
        Object o = null;
        
        if (top != null) {
            if (top == base)
                base = null;
            o = top.value;
            top.value = null;   // do our bit for garbage collection
            top = top.next;     // element below in the stack; may be null
            count--;
        }
        return o;
    }
    /**
     * Push an Object on the stack; the Object may be null.
     * 
     * @param o Object being added to the stack (as value of node)
     */
    public void push(Object o) {
        if (base == null) {
            top = base = new SinglyLinkedNode (o);
            count = 1;
        } else {
            SinglyLinkedNode oldTop = top;
            top = new SinglyLinkedNode (o);
            top.next = oldTop;
            count++;
        }
    }
    public int size() {
        return count;
    }
}
