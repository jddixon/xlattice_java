/* TestHttpOverTcp.java */
package org.xlattice.httpd;

import java.net.InetAddress;

/**
 * @author Jim Dixon
 */

import junit.framework.*;

import org.xlattice.Address;
import org.xlattice.EndPoint;
import org.xlattice.transport.IPAddress;
import org.xlattice.transport.tcp.Tcp;

/**
 *
 * @author <A HREF="mailto:jddixon@users.sourceforge.net">Jim Dixon</A>
 */
public class TestHttpOverTcp extends AbstractHttpTest {

    InetAddress thisHost;
    int[] serverPort;
        
    public TestHttpOverTcp (String name)        throws Exception{
        super(name);
    }
    public void implSetUp ()                    throws Exception {
        try {
            thisHost = InetAddress.getLocalHost();
        } catch (java.net.UnknownHostException uhe) {
            System.err.println("can't get local host's name!");
        }
        protocol   = Tcp.class;
        serverPort = new int[SITE_COUNT];
        serverAddr = new Address[SITE_COUNT];
        serverEnd  = new EndPoint[SITE_COUNT];

        for (int i = 0; i < SITE_COUNT; i++) {
            serverPort[i] = 8080 + i;
            serverAddr[i] = new IPAddress (thisHost, serverPort[i]);
            // XXX 2011-08-22 replaces Tcp.class with new Tcp()
            serverEnd[i]  = new EndPoint (new Tcp(), serverAddr[i]);
        }
    }
    public void implTearDown ()                 throws Exception {

    }
}
