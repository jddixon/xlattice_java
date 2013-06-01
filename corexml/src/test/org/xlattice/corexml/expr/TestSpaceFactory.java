/* TestSpaceFactory.java */
package org.xlattice.corexml.expr;

import junit.framework.*;

/**
 * @author Jim Dixon
 **/

public class TestSpaceFactory extends TestCase {

    private SpaceFactory factory = SpaceFactory.getInstance();
    private String spaces;
    
    public TestSpaceFactory (String name) {
        super(name);
    }
    public void setUp () { }
    public void tearDown() { }
    public void testMe() {
        String spaces = factory.makeSpaces(-4);
        assertEquals("", spaces);
        spaces = factory.makeSpaces(14);
        assertEquals(14, spaces.length());
        String spaces2 = factory.makeSpaces(14);
        assertTrue (spaces == spaces2);
    }
}
