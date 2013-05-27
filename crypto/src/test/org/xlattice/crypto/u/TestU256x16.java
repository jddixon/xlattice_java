/* TestU256x16.java */
package org.xlattice.crypto.u;

import java.io.File;
import junit.framework.*;
import static org.xlattice.crypto.u.UConst.*;

/**
 * @author Jim Dixon
 */

public class TestU256x16 extends AbstractTestU {

    public TestU256x16 (String name)    throws Exception {
        super(name);
    }
    public void testEmptyU256x16()      throws Exception {
        doTestEmpty(DIR256x16);
    } 
    public void testLazy()              throws Exception {
        doTestLazy(DIR256x16, 256);
    }
    public void testNotLazy()           throws Exception {
        doTestNotLazy(DIR256x16, 256);
    }
}
