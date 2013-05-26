/* TestTermParser.java */
package org.xlattice.util.context;

import junit.framework.*;

/**
 * @author Jim Dixon
 **/

import org.xlattice.Context;

public class TestTermParser extends TestCase {

    TermParser xp;
    
    public TestTermParser (String name) {
        super(name);
    }

    private static final String THIS_IS = "this is ";
    public void setUp () {
        xp = null;
    }

    public void tearDown() {
    }
 
    public void testEmpty() {
        try {
            xp = new TermParser(null);
            fail("expected NullPointerException!");
        } catch (NullPointerException npe) { /* success */ }
    }
    public void testSimplest() throws Exception {
        xp = new TermParser(THIS_IS);
        Term t = xp.next();
        assertTrue (t instanceof Literal);
        assertEquals(THIS_IS, t.resolve(new Context()));
        
        assertNull("successfully got second term from a one-term expression!",
                xp.next());
    }
}
