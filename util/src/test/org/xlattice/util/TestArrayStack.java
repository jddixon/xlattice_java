/* TestArrayStack.java */
package org.xlattice.util;

import java.util.EmptyStackException;

/**
 * ArrayStack is * DEPRECATED * so this test should vanish.
 *
 * TestStack is this code edited to replace ArrayStack with Stack,
 * ignoring generics.
 *
 * @author Jim Dixon
 **/

import junit.framework.*;

public class TestArrayStack extends TestCase {

    private ArrayStack  stack;
    private String []   x;

    public TestArrayStack (String name) {
        super(name);
    }

    public void setUp () {
        stack = new ArrayStack();
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
    /** Bug found 2004-11-28. */
    public void testGets() {
        assertTrue (stack.isEmpty());
        String s = "junk";
        assertEquals(s, stack.push(s));
        assertEquals(1, stack.size());      // after the push
        assertFalse (stack.isEmpty());
        String s1 = (String) stack.get(0);
        assertEquals(0, stack.size());
        assertTrue (stack.isEmpty());
        try {
            s1 = (String) stack.get(0);
            fail("got an element from an empty stack");
        } catch (Exception e) { /* success */ }
        
    }
    public void testDepthOps() {
        for (int i = 0; i < x.length; i++)
            stack.push(x[i]);

        // test search function, which returns depth
        for (int i = 0; i < x.length; i++) 
            assertEquals ( i, stack.search(x[i]));
        assertEquals(-1, stack.search("zombie hero"));
             
        // peek at depth
        String s0 = (String) stack.peek(0);   // the bottom
        assertEquals (x[0], s0);
        String s1 = (String) stack.peek(1);   // one up
        assertEquals (x[1], s1);
        String s6 = (String) stack.peek(6);  // just below the top
        assertEquals (x[6], s6);
        String s7 = (String) stack.peek(7);  // the top
        assertEquals (x[7], s7);
        try {
            stack.peek(-1);
            fail("peek at depth of -1 didn't throw exception");
        } catch (IndexOutOfBoundsException e) { /* success */ }
        try {
            stack.peek(stack.size());
            fail("peek at depth of stacksize didn't throw exception");
        } catch (IndexOutOfBoundsException e) { /* success */ }

        // get from depth
        int sizeNow = stack.size();
        assertEquals (s0, stack.get(0));
        assertEquals (--sizeNow, stack.size());
        assertEquals (s6, stack.get(5));
        assertEquals (--sizeNow, stack.size());
    }
}
