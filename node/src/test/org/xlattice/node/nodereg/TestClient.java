/* TestClient.java */
package org.xlattice.node.nodereg;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Random;

import junit.framework.*;
import org.xlattice.*;
import org.xlattice.EndPoint;
import org.xlattice.crypto.RSAKey;
import org.xlattice.crypto.RSAKeyGen;
import org.xlattice.crypto.RSAPublicKey;
import org.xlattice.transport.IOScheduler;
import org.xlattice.transport.IPAddress;
import org.xlattice.transport.tcp.SchedulableTcpAcceptor;
import org.xlattice.transport.tcp.Tcp;
import org.xlattice.util.StringLib;

/**
 * Test the blocking node registry client.
 *
 * @author Jim Dixon
 */
public class TestClient extends TestCase {

    private Random rng = new Random ( new Date().getTime() );
    private RSAKeyGen keyGen;

    private       InetAddress thisHost;
    private final int serverPort = NodeRegSListener.NODE_REG_SERVER_PORT;
    private final IPAddress srvAddr;
    private final IPAddress clientAddr;
    private final Tcp tcp;
    private final EndPoint thisEnd;

    // these get changed for each test run
    private RSAKey      key;
    private PublicKey   pubkey;
    private Client      client;
    private NodeID      nodeID;
    private IOScheduler scheduler;
    private SchedulableTcpAcceptor akc;

    public TestClient (String name)         throws Exception {
        super(name);
        keyGen   = new RSAKeyGen();
        thisHost = InetAddress.getLocalHost();

        srvAddr = new IPAddress(thisHost, serverPort);
        clientAddr = new IPAddress(thisHost, 0);
        tcp = new Tcp();
        thisEnd = new EndPoint(tcp, clientAddr);
        
    }

    public void setUp () {
        key       = null;
        pubkey    = null;
        client    = null;
        nodeID    = null;
        scheduler = null;
        akc       = null;
    }

    public void testKeylessClient ()            throws Exception {
        try {
            client = new Client (null);
            fail("accepted null RSA key!");
        } catch (IllegalArgumentException iae) { /* success */ }
    }
    public void testWithKey()                   throws Exception {
        key    = (RSAKey) keyGen.generate();

        client = new Client (key);
        assertNotNull(client);
        NodeID nodeID = client.register(null);
        assertNull(nodeID);
    }
    private void shutDownScheduler ()           throws Exception {
        try { scheduler.close(); } catch (Exception e) { /* ignore */ }
        Thread schedThread = scheduler.getThread();
        if (schedThread.isAlive())
            schedThread.join();
        assertFalse(schedThread.isAlive());
        assertFalse(scheduler.isRunning());
    }
    /**
     * XXX Need to verify server's digital signature.
     */
    public void testWithServer()                throws Exception {
        scheduler = new IOScheduler();          // in separate thread
        EndPoint serverEnd = new EndPoint(tcp, srvAddr);
        akc = new SchedulableTcpAcceptor (serverEnd, scheduler,
                                         new NodeRegSListenerFactory());
        scheduler.add(akc);
        Thread.currentThread().sleep(2);        // XXX a hack
        // blocking registrations
        for (int k = 0; k < 16; k++) {
            key = (RSAKey) keyGen.generate();
            Client client = new Client(key);
            nodeID = client.register(thisEnd);  // ephemeral port
            assertNotNull(nodeID);
        }
        akc.close();
        shutDownScheduler();
    }
}
