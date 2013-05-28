/* TestTls.java */
package org.xlattice.transport.tls;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.security.KeyStore;
import java.security.SecureRandom;
import javax.net.ssl.SSLContext;

import junit.framework.*;

import org.xlattice.Transport;
import org.xlattice.transport.tls.Tls;
import org.xlattice.util.StringLib;

/**
 *
 * @author Jim Dixon
 */

public class TestTls extends TestCase {

    private Transport tls;
    
    private SimpleTlsServer server;
    private final int serverPort = 9999;
    private Thread serverThread;

    private SimpleTlsClient client;
    private final int clientPort = 9998;
    private Thread clientThread;
    
    private Inet4Address addr;
    
    public TestTls (String name) {
        super(name);
    }

    public void setUp () {
        tls = null;
        server = null;
        client = null; 
    }
    public void tearDown() {
        if (server != null) 
            try { 
                server.close();
            } catch (Throwable t) { /* ignore */ }
        if (client != null) 
            try { 
                client.close(); 
            } catch (Throwable t) { /* ignore */ }
    }

    public void testConstructors()              throws Exception {
        tls = new Tls();
        assertEquals("tls",     tls.name());

     
    }

}
