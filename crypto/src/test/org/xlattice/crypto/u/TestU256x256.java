/* TestU256x256.java */
package org.xlattice.crypto.u;

import java.io.File;
import junit.framework.*;
import static org.xlattice.crypto.u.UConst.*;

/**
 * @author Jim Dixon
 */

public class TestU256x256 extends AbstractTestU {

    public TestU256x256 (String name)   throws Exception {
        super(name);
    }
    public void testEmptyU256x256()     throws Exception {
        doTestEmpty(DIR256x256);
    } 
    public void testLazy()              throws Exception {
        doTestLazy(DIR256x256, 256);
    }
    public void testNotLazy()           throws Exception {
        doTestNotLazy(DIR256x256, 256);
    }
}
