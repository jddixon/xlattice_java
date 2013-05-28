/* TestAsyncTlsEchoers.java */
package org.xlattice.transport.tls;

import java.net.InetAddress;
import java.security.SecureRandom;

import junit.framework.*;

import org.xlattice.EndPoint;
import org.xlattice.transport.*;
import org.xlattice.transport.tcp.*;

//import org.xlattice.util.StringLib;

/**
 * The test fixture is one TLS Echo server and K TLS echo clients.
 * The server consists of a standard TCP Acceptor which attaches
 * a ConnectionListener to each generated connection.  The listener
 * is itself an AsyncTlsServerConnection and its AsyncPacketHandler,
 * AsyncTlsEchoS.
 *
 * Each client is started by a standard TCP Connector which attaches
 * another ConnectionListener to its connection.  In this case the
 * listener is a AsyncTlsClientConnection.  This has a different 
 * AsyncPacketHandler, AsyncTlsEchoC.
 *
 * Each of the AsyncTlsConnections contains a TLSEngine which does 
 * the encryption/decryption.  What passes over the wire is ciphertext.
 *
 * @author Jim Dixon
 */

public class TestAsyncTlsEchoers extends TestCase {

    private         IOScheduler   scheduler;
    protected final SecureRandom  rng;
    protected final Tcp           tcp;
    protected final InetAddress   localHost;
    protected final int           serverPort;
    protected final IPAddress     serverAddr;
    // hmmmm...
    protected       EndPoint      serverEnd;
   
    private int k;
    private int n;
    
    protected SchedulableConnector[]  ctr;
    protected SchedulableConnection[] cnx;
    protected SchedulableAcceptor   acc;

    protected EndPoint[]    clientEnd;
    protected IPAddress[]   clientAddr;


    public TestAsyncTlsEchoers (String name)    throws Exception {
        super(name);
        rng = new SecureRandom();
        int junk = rng.nextInt();
        tcp = new Tcp();
        localHost = InetAddress.getLocalHost();
        serverPort = Integer.parseInt(System.getProperty("STUN_PRIMARY_PORT"));
        serverAddr = new IPAddress (localHost, serverPort);
    }

    public void setUp ()                        throws Exception {
        scheduler = new IOScheduler();
        // set up the acceptor ////////////////////////////
        acc = (SchedulableAcceptor)tcp
                                    .getAcceptor(serverAddr, false);
        // This is the listener added by the Acceptor.  
//      ConnectionListener listener = new ... WORKING HERE
//      // XXX VERY SERIOUS PROBLEM: as currently conceived, the 
//      // XXX server-side ConnectionListener must have a no-arg constructor
//      acc.setConnListener(new EchoSListener());
//      acc.setReceiver(scheduler);
//      scheduler.add(acc);
         
        // set up the connectors //////////////////////////
    }
    public void tearDown()                      throws Exception {
        if (scheduler != null) {
            scheduler.close(); 
            scheduler = null;
        }
        if (acc != null)
            try { acc.close(); } catch (Exception e) {}
    }
    
    public void testFixture()                   throws Exception {
        /* STUB */
    }
}
