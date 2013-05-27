/* TestU16.java */
package org.xlattice.crypto.u;

import java.io.File;
import junit.framework.*;
import static org.xlattice.crypto.u.UConst.*;

/**
 * @author Jim Dixon
 */

public class TestU16 extends AbstractTestU {

    public TestU16 (String name)       throws Exception {
        super(name);
    }

    public void testEmptyU16()                throws Exception {
        doTestEmpty(DIR16);
    } 
    public void testLazy()                  throws Exception {
        doTestLazy(DIR16, 16);
    }
    public void testNotLazy()               throws Exception {
        doTestNotLazy(DIR16, 16);
    }
}
