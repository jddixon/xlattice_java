/* TestTlsAddress.java */
package org.xlattice.transport.tls;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.security.KeyStore;
import java.security.SecureRandom;
import javax.net.ssl.SSLContext;

import junit.framework.*;

import org.xlattice.util.StringLib;

/**
 *
 * @author Jim Dixon
 */

public class TestTlsAddress extends TestCase {

    private Inet4Address addr;
    
    public TestTlsAddress (String name) {
        super(name);
    }

    public void setUp () {
    }

    public void testConstructors()              throws Exception {
        
    }

}
