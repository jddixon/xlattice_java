/* TestClient.java */
package org.xlattice.protocol.stun;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Comparator;
import java.util.Random;

import javax.naming.NamingEnumeration;
//import javax.naming.directory.Attribute;
//import javax.naming.directory.Attributes;
//import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import junit.framework.*;

import static org.xlattice.crypto.tls.TlsConst.*;
import static org.xlattice.protocol.stun.Client.*;
import org.xlattice.protocol.stun.Client;
import org.xlattice.transport.IPAddress;
import org.xlattice.transport.tls.Tls;
import org.xlattice.util.StringLib;

/**
 * This interim implementation assumes that the shared secret server
 * does NOT use TLS to authenticate (it authenticates using plaintext
 * TCP) or does not authenticate at all.  The first assumption does not
 * comply with the STUN RFC.
 *
 * @author Jim Dixon
 */

public class TestClient extends TestCase implements StunConst {

    private Random rng = new Random();

    /** Not to be trusted unless set equal to NattedAddress.getLocalHost() */
    private Inet4Address thisHost;
    private Client client;

    private Inet4Address priHost;
    private Inet4Address secHost;
    private final int priPort;
    private final int secPort;

    private final NetworkInterface eth0 = NetworkInterface.getByName("eth0");
    private Server server;

    public TestClient (String name)             throws Exception {
        super(name);
        // XXX THIS WILL BE 127.0.0.1
        thisHost = (Inet4Address)InetAddress.getLocalHost();
        // we use the standard ports because we are testing with
        // remote servers
        priPort = STUN_SERVER_PORT;
        secPort = priPort + 1;
    }

    public void setUp ()                        throws Exception {
        client = null;
        // SERVER USED IS stun.xlattice.ORG ON STANDARD PORTS.  As
        // of 2006-04-15 this was running the Vovida server software.
        // The XLattice STUN server is running on ports 3480 and 3481.
        // 2011-09002 STUN server should now be running on standard
        // ports on stun.xlattice.org
        priHost = (Inet4Address) Inet4Address
                    // .getByName("72.44.80.208"); // stun.xlattice.org
                    .getByName("stun.softjoys.com");
        secHost = (Inet4Address) Inet4Address
                    .getByName("72.44.80.209");

    }
    public void tearDown()                      throws Exception {
        if (client != null)
            client.close();
        if (server != null && server.isRunning())
            server.close();
    }
    public Server setUpLocalServer(boolean authenticating, int authLevel)
                                                throws Exception {
        String pri = System.getProperty("STUN_PRIMARY_HOST");
        String sec = System.getProperty("STUN_SECONDARY_HOST");
        String pwd = System.getProperty("STUN_SERVER_PASSWORD");
        String ks  = System.getProperty("STUN_SERVER_KEYSTORE");

        // XXX NO LONGER USED
        priHost = (Inet4Address) InetAddress.getByName(pri);
        secHost = (Inet4Address) InetAddress.getByName(sec);
        // XXX END NOT USED

        // XXX NO LONGER A SEPARATE THREAD ...
        Server server = new Server(pri, sec, priPort, secPort,
                            ks, pwd, authenticating, false);
        boolean serverRunning = false;
        for (int i = 0; i < 32; i++) {
            if (server.isRunning()) {
                serverRunning = true;
                break;
            } else {
                Thread.sleep(20);
            }
        }
        if (!serverRunning) {
            System.out.println("LOCAL SERVER IS NOT RUNNING");
            return null;
        } else {
            return server;
        }
    }
    // UNIT TESTS ///////////////////////////////////////////////////
    public void testConstructor()               throws Exception {

        System.out.println("DEBUG: entering testConstructor");

        // non-authenticating client //////////////////////
        client = new Client (priHost, priPort);     // uses loopback
        IPAddress clientAddr = client.getClientAddr();
        assertTrue ( thisHost.equals(clientAddr.getHost()) );
        /* port is ephemeral */

        assertEquals(0, client.getChangeFlags());
        assertNull( client.getMappedAddr() );
        assertNull( client.getResponseAddr() );
        assertNull( client.getSecondaryAddr() );
        assertNull( client.getSecretServerAddr() );
        assertNull( client.getSourceAddr() );

        client.setChangeFlags (0x46);
        assertEquals(6, client.getChangeFlags());
        client.setChangeFlags (0);
        assertEquals(0, client.getChangeFlags());

        Inet4Address someHost = (Inet4Address) InetAddress
                                            .getByName("1.2.3.4");
        int somePort = 56789;
        IPAddress someAddr = new IPAddress(someHost, somePort);
        client.setResponseAddr( someAddr );
        assertTrue (someAddr.equals( client.getResponseAddr() ));
        client.close();

        // authenticating /////////////////////////////////
        // XXX HANGS if the next statement is uncommented; client
        // needs a TIMEOUT option XXX THIS IS A SERIOUS FLAW
//      client = new Client (priHost, priPort, eth0,
//                      // no logging, authenticating, verbose
//                            null,       true,        false);

//      IPAddress secretServerAddr = client.getSecretServerAddr();
//      assertTrue ( priHost.equals( secretServerAddr.getHost() ) );
//      assertEquals (priPort, secretServerAddr.getPort()); // FOO

    }
    /**
     * This test runs against stun.xlattice.org.
     */
//  public void testRemoteNoAuth()              throws Exception {
//
//      System.out.println("DEBUG: entering testRemoteNoAuth");

//      // client = new Client (priHost, priPort);
//      NetworkInterface priIface = eth0;
//      assertNotNull(priIface);
//      client = new Client (priHost, priPort, priIface);

//      // DEBUG
//      System.out.printf("DEBUG - testRemoteNoAuth - server %s:%d\n",
//              priHost, priPort);
//      // END

//      // first BindRequest gets the alternative address from the server
//      assertNull ( client.getSecondaryAddr() );
//      try {
//          client.bind(0);
//      } catch (SocketTimeoutException ste) {
//          fail("timed out sending request to BindingServer 0");
//      } catch (java.net.PortUnreachableException pue) {
//          pue.printStackTrace();
//          fail("can't reach port " + priPort);
//      }
//      IPAddress srcAddr = client.getSourceAddr();
//      assertTrue   (priHost.equals( srcAddr.getHost() ) );
//      assertEquals (priPort,        srcAddr.getPort());

//      // The secondary address has been observed to follow this pattern
//      IPAddress secAddr = client.getSecondaryAddr();
//      assertNotNull(secAddr);

//      String priIPAddr = priHost.getHostAddress();
//      String secIPAddr = secAddr.getHost().getHostAddress();
//      int    secPort   = secAddr.getPort();
//
//      // the next is not generally true; for Softjoys the IP addresses
//      // are 69.4.236.239 and 69.4.236.236 respectively
//      // XXX THIS TEST SHOULD BE REMOVED
//      assertEquals (priHost.getHostAddress(),
//                    secAddr.getHost().getHostAddress());
//
//      // This is generally true but obviously need not be.
//      assertEquals (priPort + 1,    secAddr.getPort());
//  }

//  public void doLocalTest(boolean authenticating, int authLevel)
//                                              throws Exception {
//      boolean verbose        = false;
//      server = setUpLocalServer(authenticating, authLevel);
//      assertNotNull("couldn't start server", server);
//      client = new Client (priHost, priPort, eth0,
//                           null, authenticating, verbose);

//      // first BindRequest gets the alternative address from the server
//      assertNull ( client.getSecondaryAddr() );
//      try {
//          client.bind(0);
//      } catch (SocketTimeoutException ste) {
//          fail("timed out sending request to BindingServer 0");
//      }
//      IPAddress srcAddr = client.getSourceAddr();
//      assertTrue   (priHost.equals( srcAddr.getHost() ) );
//      assertEquals (priPort,        srcAddr.getPort());

//      IPAddress secAddr = client.getSecondaryAddr();
//      assertTrue   (secHost.equals( secAddr.getHost() ) );
//      assertEquals (secPort,        secAddr.getPort());

//      IPAddress mapAddr            = client.getMappedAddr();
//      NattedAddress natted         = client.getNattedAddress();
//      Inet4Address nattedLocalHost = natted.getLocalHost();
//      int nattedLocalPort          = natted.getLocalPort();

//      // DEBUG
//      System.out.printf(
//              "this host:    %s\n" +
//              "mapped host:  %s\n",   nattedLocalHost, mapAddr.getHost());
//      // END
//      assertTrue   (nattedLocalHost.equals( mapAddr.getHost() ));
//      assertEquals (nattedLocalPort, mapAddr.getPort());
//      assertEquals (nattedLocalPort, client.getPort());

//  }
//  public void testLocalNoAuth()               throws Exception {
//
//      System.out.println("DEBUG: entering testLocalNoAuth");

//      doLocalTest(false, 0);
//  }

//  public void testLocalWithAuth()             throws Exception {
//
//      System.out.println("DEBUG: entering testLocalWithAuth");

//      doLocalTest(true, ANONYMOUS_TLS);
//  } // FOO 52
//  public void testFoursome()                  throws Exception {
//
//      System.out.println("DEBUG: entering testFoursome");

//      client = new Client (priHost, priPort);
//                                  // priority, weight, servername, port
//      Client.Foursome a = new Client.Foursome ( 4, 0, "A", 100 );
//      Client.Foursome b = new Client.Foursome ( 4, 2, "B", 101 );
//      Client.Foursome c = new Client.Foursome ( 1, 3, "C", 102 );

//      Comparator comp = new Client.FoursomeComp();

//      assertEquals( 0, comp.compare(a, a));
//      // lower priority before higher
//      assertEquals(-1, comp.compare(c, b));
//      assertEquals( 1, comp.compare(b, c));
//      // higher weight before lower
//      assertEquals(-1, comp.compare(b, a));
//      assertEquals( 1, comp.compare(a, b));
//  }
//  /**
//   * Test STUN server discovery.
//   */
//  public void testServerDiscovery()           throws Exception {
//
//      System.out.println("DEBUG: entering testServerDiscovery");

//      client = new Client (priHost, priPort);
//      // test using a specific name server //////////////
//      ServerInfo[] stunServers = client.discoverServers(
//                          "ns.xlattice.org", "xlattice.org", true);
//      assertNotNull(stunServers);
//      assertNotNull(stunServers[0]);
//      assertEquals("stun.xlattice.org.",   stunServers[0].name);

//      // tests with other name servers also succeed but are not
//      // included here because they will generally not be available
//      // to other testers

//      // test using the default name server /////////////
//      stunServers = client.discoverServers("xlattice.org", true);
//      assertNotNull(stunServers);
//      assertNotNull(stunServers[0]);
//      assertEquals("stun.xlattice.org.",   stunServers[0].name);
//  } // GEEP

    // WAS ALREADY COMMENTED OUT
//  public void testBindingLifetime()           throws Exception {
//      Inet4Address host = (Inet4Address) Inet4Address
//                                  .getByName("stun.xlattice.org");
//      client = new Client(host, STUN_SERVER_PORT,
//                                                  false, true);
//      int lifetime = client.bindingLifetime();
//  }
//
}
