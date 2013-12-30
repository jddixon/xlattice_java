/* TestProjMgr.java */
package org.xlattice.projmgr;

import junit.framework.*;

/**
 * @author Jim Dixon
 */

import org.xlattice.corexml.om.*;

public class TestProjMgr extends TestCase {

    public TestProjMgr (String name) {
        super(name);
    }
    public void setUp() {
    }
    public void tearDown() {
    }
    public void testStaticFunctions() {
        // JNLP version numbers are fairly free form but do not
        // permit embedded spaces
        assertTrue  (ProjMgr.isWellFormedVersion("1.1"));
        assertTrue  (ProjMgr.isWellFormedVersion("4.52a2"));
        assertTrue  (ProjMgr.isWellFormedVersion("4.52b99"));
        assertFalse (ProjMgr.isWellFormedVersion(".52b99"));
        assertFalse (ProjMgr.isWellFormedVersion("52 99"));
        assertFalse (ProjMgr.isWellFormedVersion(""));
        assertFalse (ProjMgr.isWellFormedVersion(null));

        assertTrue  (ProjMgr.isWellFormedId("a59"));
        assertTrue  (ProjMgr.isWellFormedId("_a59"));
        assertTrue  (ProjMgr.isWellFormedId("a_59abc"));
        assertTrue  (ProjMgr.isWellFormedId("a59Z_"));
        assertTrue  (ProjMgr.isWellFormedId("A59_"));
        assertTrue  (ProjMgr.isWellFormedId("_59_"));
        assertFalse (ProjMgr.isWellFormedId(".a59"));
        assertFalse (ProjMgr.isWellFormedId("a.59"));
        assertFalse (ProjMgr.isWellFormedId("$a59"));
        assertFalse (ProjMgr.isWellFormedId("a$59"));
        assertFalse (ProjMgr.isWellFormedId("59"));
        assertFalse (ProjMgr.isWellFormedId(""));
    }
}
