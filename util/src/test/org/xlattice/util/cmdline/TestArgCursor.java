/* TestArgCursor.java */
package org.xlattice.util.cmdline;

import junit.framework.*;

/**
 *
 *
 * @author Jim Dixon
 */
public class TestArgCursor extends TestCase {

    ArgCursor cursor;
    
    public TestArgCursor (String name) {
        super(name);
    }

    public void setUp () {
        cursor = null;
    }

    public void tearDown() {
    }
    
    /** an argument list with no members is acceptable */
    public void testEmpty() {
        cursor = new ArgCursor ( new String[]{} );
        assertNotNull (cursor);
        assertFalse   (cursor.hasNext());
        assertEquals  (0, cursor.length());
    }

    public final static String a = " aa ";
    public final static String b = "bbb\r\n";
    public final static String c = "\tcccc\t";
    public final static String d = "ddddd";
    public final static String e = "  \n\n";
    public final static String f = "\t \t \r";
    
    public void testTrimToEmpty() {
        try {
            new ArgCursor( new String[] {e, f} );
            fail();
        } catch (IllegalArgumentException iae) { }
    }

    public void testTrimming() {
        cursor = new ArgCursor ( new String[] {a, b, c, d} );
        assertNotNull (cursor);
        assertTrue    (cursor.hasNext());
        assertEquals  (4, cursor.length());
        assertEquals  (0, cursor.index());
        assertEquals  ("aa", cursor.next());
        assertEquals  ("bbb", cursor.peek());
        assertTrue    (cursor.hasNext());
        assertEquals  ("bbb", cursor.next());
        assertTrue    (cursor.hasNext());
        assertEquals  ("cccc", cursor.next());
        assertEquals  ("ddddd", cursor.peek());
        assertEquals  ("ddddd", cursor.next());
        assertEquals  (4, cursor.index());
        assertFalse    (cursor.hasNext());
        try {
            cursor.next();
            fail();
        } catch ( ArrayIndexOutOfBoundsException obe ) { /* ignore it */ }
    }
}
