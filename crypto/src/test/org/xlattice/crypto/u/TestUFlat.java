/* TestUFlat.java */
package org.xlattice.crypto.u;

import junit.framework.*;

import java.io.File;
import java.util.Random;

import static org.xlattice.crypto.u.UConst.*;

/**
 * @author Jim Dixon
 */

public class TestUFlat extends AbstractTestU {

    public TestUFlat (String name)              throws Exception {
        super(name);
    }
    // UNIT TESTS ///////////////////////////////////////////////////
    public void testEmptyU()                throws Exception {
        doTestEmpty(FLAT_DIR);
    } 
    public void testLazy()                  throws Exception {
        doTestLazy(FLAT_DIR, 16);
    }
}
