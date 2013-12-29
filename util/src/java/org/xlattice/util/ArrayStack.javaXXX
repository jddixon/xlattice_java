/* ArrayStack.java */
package org.xlattice.util;

import java.util.ArrayList;
import java.util.EmptyStackException;

/**
 * A faster Stack class.  No internal synchronization, so must be
 * externally synchronized if used in a multi-threaded environment.
 *
 * THIS CLASS SHOULD BE REPLACED WITH util.Stack IN ALL USING CODE.

 * @author Jim Dixon
 * @deprecated
 */
public class ArrayStack {

    private ArrayList stack;
    private int top;
 
    /**
     * No-arg constructor.  Defaults to a stack size of 16.
     */
    public ArrayStack() {
        this (16);
    }
    /**
     * Constructor specifying an initial stack size.
     * @param sizeHint initial stack size
     */
    public ArrayStack (int sizeHint) {
        stack = new ArrayList(sizeHint);
        top   = -1;
    }
 
    /**
     * Removes all elements from the stack.
     */
    public void clear () {
        stack.clear();
        top = -1;
    }
    /**
     * Get an object from the stack, removing it.
     * 
     * @param  index from the bottom of the stack, zero-based.
     * @return the element at that index from the bottom of the stack
     * @throws ArrayIndexOutOfBoundsException
     */
    public Object get (int index) {
        if (top > -1) 
            top--;
        return stack.remove(index);
    } 
    /**
     * @return whether the stack is empty
     */
    public boolean isEmpty () {
        return (top < 0);
    }
    /**
     * Get the object at the top of the stack without removing it.
     * @return that object
     * @throws EmptyStackException
     */
    public Object peek() {
        if (top < 0)
            throw new EmptyStackException();
        else 
            return stack.get(top);
    }
    /**
     * Get an object from the stack without removing it.
     * @param  index zero-based index from the bottom of the stack
     * @return that object
     * @throws IndexOutOfBoundsException, EmptyStackException
     */
    public Object peek (int index) {
        return stack.get(index);
    }
    /**
     * Get the object from the top of the stack, removing it.
     * @return that object
     * @throws EmptyStackException
     */
    public Object pop () {
        if (top < 0)
            throw new EmptyStackException();
        return stack.remove(top--);
    }

    /**
     * Push an object on to the stack.  The object may be null.
     */
    public Object push (Object o) {
        stack.add(o);
        top++;
        return o;
    }

    /** 
     * Search the stack for an object.
     * 
     * @return the depth of the object in the stack or -1 if not present 
     */
    public int search (Object o) {
        for (int i = 0; i <= top; i++)
            if (stack.get(i) == o)
                return i;
        return -1;
    }
    /**
     * @return the depth of the stack
     */
    public int size() {
        return top + 1;
    }

    /** @return an array containing stack elements in order, bottom first */
    public Object[] toArray() {
        return stack.toArray();
    }
    /** 
     * Return an array with elements of the same type as the sample.
     * 
     * @param a sample array whose type is to be matched
     * @return an array containing stack elements in order, bottom first 
     */
    public Object[] toArray(Object [] a) {
        return stack.toArray(a);
    }
}
