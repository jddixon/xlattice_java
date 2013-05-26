/* TestStack.java */
package org.xlattice.util;

import java.util.Stack;
import java.util.EmptyStackException;

/**
 * This is TestArrayStack.java with ArrayStack replaced by Stack
 * throughout.  The objective to to confirm that Stack is a drop-in
 * replacement for ArrayStack (which was deprecated in 2006)
 *
 * DIFFERENCES FOUND IN THIS CODE:
 *   ArrayStack peek(n) as replaced by Stack get(n)
 *   Stack replaces what ArrayStack calls get() with remove
 *   Stack is indexed base 1 from the top of the stack, whereas 
 *     ArrayStack indexes base 0 from the bottom
 *
 * @author Jim Dixon
 **/

import junit.framework.*;

public class TestStack extends TestCase {

    private Stack  stack;
    private String []   x;

    public TestStack (String name) {
        super(name);
    }

    public void setUp () {
        stack = new Stack();
        x     = new String [] {
                 "pickles",  "frog",    "dogs body", "harlequin",
                 "penguins", "wombats", "puddles",   "ooff!"    };
    }

    public void testEmpty() {
        assertNotNull(stack);
        assertTrue(stack.isEmpty());
    }

    public void testSimpleOps() {
        assertEquals (0, stack.size());
        String s = (String)stack.push(x[0]);
        assertEquals (x[0], s);
        assertEquals (x[0], stack.peek());

        assertEquals(1, stack.size());
        s = (String)stack.push(x[1]);
        assertEquals (x[1], s);
        assertEquals (x[1], stack.peek());

        assertEquals(2, stack.size());
        s = (String)stack.push(x[2]);
        assertEquals (x[2], s);
        assertEquals (x[2], stack.peek());

        assertEquals(3, stack.size());
        s = (String)stack.push(x[3]);
        assertEquals (x[3], s);
        assertEquals (x[3], stack.peek());

        assertEquals(4, stack.size());
        s = (String)stack.pop();
        assertEquals (x[3], s);
        assertEquals (x[2], stack.peek());
        assertEquals(3, stack.size());
        
        s = (String)stack.pop();
        assertEquals (x[2], s);
        assertEquals (x[1], stack.peek());
        assertEquals(2, stack.size());
        
        s = (String)stack.pop();
        assertEquals (x[1], s);
        assertEquals (x[0], stack.peek());
        assertEquals(1, stack.size());
        
        s = (String)stack.pop();
        assertEquals (x[0], s);
        assertEquals(0, stack.size());
        try {
            stack.peek();
            fail("empty stack didn't throw exception on peek");
        } catch (EmptyStackException e) { /* success */ }
        try {
            stack.pop();
            fail("empty stack didn't throw exception on pop");
        } catch (EmptyStackException e) { /* success */ }
    }
    public void testGets() {
        assertTrue (stack.isEmpty());
        String s = "junk";
        assertEquals(s, stack.push(s));
        assertEquals(1, stack.size());      // after the push
        assertFalse (stack.isEmpty());
        // XXX get => remove on the next line
        String s1 = (String) stack.remove(0);
        assertEquals(0, stack.size());
        assertTrue (stack.isEmpty());
        try {
            // XXX get => remove on the next line
            s1 = (String) stack.remove(0);
            fail("got an element from an empty stack");
        } catch (Exception e) { /* success */ }
        
    }
    public void testDepthOps() {
        for (int i = 0; i < x.length; i++)
            stack.push(x[i]);

        // test search function, which returns depth
        // XXX Stack returns a 1-based index, so s/i/i+1/ in assertion
        for (int i = 0; i < x.length; i++) {
            System.out.printf("element %d is %s\n", i, x[i]);   // XXX
            // XXX assertEquals ( i, stack.search(x[i]));
            assertEquals ( x.length - i, stack.search(x[i]));
        }
        assertEquals(-1, stack.search("zombie hero"));
             
        // XXX BEGIN peek(n) replaced by get(n)
        String s0 = (String) stack.get(0);   // the bottom
        assertEquals (x[0], s0);
        String s1 = (String) stack.get(1);   // one up
        assertEquals (x[1], s1);
        String s6 = (String) stack.get(6);  // just below the top
        assertEquals (x[6], s6);
        String s7 = (String) stack.get(7);  // the top
        assertEquals (x[7], s7);
        try {
            stack.get(-1);
            fail("get at depth of -1 didn't throw exception");
        } catch (IndexOutOfBoundsException e) { /* success */ }
        try {
            stack.get(stack.size());
            fail("get at depth of stacksize didn't throw exception");
        } catch (IndexOutOfBoundsException e) { /* success */ }
        // END get at depth

        // get from depth
        // BEGIN get() replaced by remove()
        int sizeNow = stack.size();
        assertEquals (s0, stack.remove(0));
        assertEquals (--sizeNow, stack.size());
        assertEquals (s6, stack.remove(5));
        assertEquals (--sizeNow, stack.size());
        // END get() replaced by remove()
    }
}
