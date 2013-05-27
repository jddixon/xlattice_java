/* Counter.java */
package org.xlattice.crypto.u;

import java.io.File;

/**
 * @author Jim Dixon
 */

public class Counter implements Visitor {
    private int    _count;

    public Counter () {}

    // INTERFACE VISITOR ///////////////////////////////////////////
    /** set up visitor's state */
    public void enterU (final String pathToU) {
        _count = 0;                 // yes, unnecessary
    }
    /** invoked at each leaf file */
    public void visitFile (final String relPath) {
        _count++;
    }
    /** do something with any accumulated state */
    public void exitU() {}

    // RESULTS /////////////////////////////////////////////////////
    /** return a count of leaf/data files in U */
    public int getCount() {
        return _count;
    }
}
