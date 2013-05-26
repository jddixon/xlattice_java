/* TestContext.java */
package org.xlattice.util.context;

import junit.framework.*;

/**
 * @author Jim Dixon
 */

import org.xlattice.Context;

public class TestContext extends TestCase {

    Context ctx;
    
    public TestContext (String name) {
        super(name);
    }

    public void setUp () {
        ctx = new Context();
    }

    public void tearDown() {
    }
 
    public void testEmpty() {
        assertNotNull(ctx);
        assertEquals(0, ctx.size());
        assertEquals(null, ctx.getParent());
        assertEquals(null, ctx.lookup("foo"));
    }
    public void testAddingNulls() {
        try {
            ctx.bind(null, "bar");
            fail("bind with null name succeeded!");
        } catch (Exception e) { /* success */ }
        try {
            ctx.bind("foo", null);
            fail("bind with null object succeeded!");
        } catch (Exception e) { /* success */ }
    }
    public void testSimpleBindings() {
        ctx.bind("foo", "that was foo");
        ctx.bind("bar", "that was bar");
        assertEquals(2, ctx.size());
        assertEquals("that was foo", ctx.lookup("foo"));
        assertEquals("that was bar", ctx.lookup("bar"));
    }
    public void testNestedContexts() {
        Context ctx1 = new Context(ctx);
        Context ctx2 = new Context(ctx1);
        assertTrue (ctx  == ctx1.getParent());
        assertTrue (ctx1 == ctx2.getParent());
        ctx.bind ("foo", "bar0");
        ctx1.bind ("foo", "bar1");
        ctx2.bind ("foo", "bar2");
        assertEquals ("bar2", ctx2.lookup("foo"));
        ctx2.unbind("foo");
        assertEquals ("bar1", ctx2.lookup("foo"));
        ctx1.unbind("foo");
        assertEquals ("bar0", ctx2.lookup("foo"));
        ctx.unbind("foo");
        assertNull(ctx2.lookup("foo"));

        ctx.bind("wombat", "Freddy Boy");
        assertEquals("Freddy Boy", ctx2.lookup("wombat"));
        Context ctx99 = ctx2.setParent(null);
        assertTrue (ctx99 == ctx2);
        assertNull(ctx2.getParent());
        assertNull(ctx2.lookup("wombat"));  // broke chain of contexts
    }
}
