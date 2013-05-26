/* TestNonBlockingLog.java */
package org.xlattice.util;

import java.util.Date;

/**
 * @author Jim Dixon
 **/

import junit.framework.*;

public class TestNonBlockingLog extends TestCase {

    public TestNonBlockingLog (String name) {
        super(name);
    }

    public void setUp () {
    }
    public void testSingleLog()                 throws Exception {
        NonBlockingLog log = NonBlockingLog.getInstance("junk.log");
        Thread.currentThread().sleep(2);    // give logger time to start
        assertTrue(log.isOpen());
        log.message("test run at " + new Date().toString());
        log.close();
        Thread.currentThread().sleep(2);    // time for the logger to shut down
        assertFalse(log.isOpen());
    }
}
