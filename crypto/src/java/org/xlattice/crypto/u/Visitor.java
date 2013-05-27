package org.xlattice.crypto.u;

/**
 * @author Jim Dixon
 */
public interface Visitor {

    /** typically used to set up visitor's state */
    void enterU (final String pathToU);
    /** invoked at each leaf file */
    void visitFile (final String relPath);
    /** do something with any accumulated state */
    void exitU();
}


