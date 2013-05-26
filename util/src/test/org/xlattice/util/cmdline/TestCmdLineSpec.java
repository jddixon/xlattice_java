/* TestCmdLineSpec.java */
package org.xlattice.util.cmdline;

import junit.framework.*;

/**
 *
 *
 * @author Jim Dixon
 */
public class TestCmdLineSpec extends TestCase {

    CmdLineSpec spec;
    
    public TestCmdLineSpec (String name) {
        super(name);
    }

    public void setUp () {
        spec = null;
    }

    public void tearDown() {
    }
    
    public void testEmpty() {
        spec = new CmdLineSpec(null, null, null);
        assertEquals ("", spec.getJavaArgs());
        CmdLineOpt[] opts = spec.getOptionDescriptors();
        assertNotNull(opts);
        assertEquals (0, opts.length);
        assertEquals ("", spec.getOtherArgDesc());
    }
}
