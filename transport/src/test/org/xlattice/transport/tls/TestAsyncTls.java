/* TestAsyncTls.java */
package org.xlattice.transport.tls;

import java.security.SecureRandom;

import junit.framework.*;

import org.xlattice.transport.IOScheduler;

/**
 *
 * @author Jim Dixon
 */

public class TestAsyncTls extends TestCase {

    private IOScheduler scheduler;
    private final SecureRandom rng;
    
    public TestAsyncTls (String name)           throws Exception {
        super(name);
        rng = new SecureRandom();
        int junk = rng.nextInt();
    }

    public void setUp ()                        throws Exception {
    }
    public void tearDown()                      throws Exception {
        if (scheduler != null) {
            scheduler.close(); 
            scheduler = null;
        }
    }
    public void testFactories()                 throws Exception {
        /* STUB */
    }
}
