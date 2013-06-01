/* TestCompost.java */
package org.xlattice.node;

import java.io.File;
import java.io.IOException;

import junit.framework.*;

/**
 * @author Jim Dixon
 */

public class TestCompost extends TestCase {

    public final static String HEAP_NAME = "junk.compost";
    
    // CONSTRUCTOR //////////////////////////////////////////////////
    public TestCompost (String name) {
        super(name);
    }
    // SETUP/TEARDOWN ///////////////////////////////////////////////
    public void setUp () {
    }
    // UNIT TESTS ///////////////////////////////////////////////////
    public void testFreshHeap ()                throws Exception {
        File file = new File(HEAP_NAME);
        if (file.exists())
            assertTrue(file.delete());
        Compost heap = Compost.getInstance(HEAP_NAME);
        assertTrue(file.exists());
        assertEquals(Compost.LENGTH, file.length());

    }
}
