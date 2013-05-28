/* TestSchedulableTcp.java */
package org.xlattice.transport.tcp;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import junit.framework.*;
import org.xlattice.*;
import org.xlattice.transport.*;

/**
 * @author Jim Dixon
 */

public class TestSchedulableTcp extends AbstractSchedulableTest {

    InetAddress thisHost;
    int clientPort;
    int serverPort;
    public TestSchedulableTcp (String name) {
        super(name);
    }
    protected void DEBUG_MSG(String s) {
        debugLog.message("TestSchedTcp" + s);
    }
    public void _setUp() {
        try {
            thisHost = InetAddress.getLocalHost();
        } catch (java.net.UnknownHostException uhe) {
            System.err.println("can't get local host's name!");
        }
        clientPort = 0;       // that is, use any ephemeral port
        serverPort = 8123;
        clientAddr = new IPAddress (thisHost, clientPort);
        serverAddr = new IPAddress (thisHost, serverPort);
        transport = new Tcp();
        clientEnd  = new EndPoint (transport, clientAddr);
        serverEnd  = new EndPoint (transport, serverAddr);
    }
    public void _tearDown() {
    }
    // UNIT TESTS ///////////////////////////////////////////////////
    protected void _checkTestVariables ()       throws Exception {
    }
    protected void _constructorSetUp()          throws Exception {
    }
    protected void _acceptorSetUp()             throws Exception {
        acc = (SchedulableAcceptor) ((Tcp)Tcp.class.newInstance())
                                    .getAcceptor(serverAddr, false);
        ServerSocketChannel srvChan =
            (ServerSocketChannel) acc.getChannel();
        assertTrue(srvChan.isOpen());
        assertFalse(srvChan.isBlocking());
        assertFalse(srvChan.isRegistered());

        ServerSocket srvSock = srvChan.socket();
        assertTrue(srvSock.isBound());
        acc.close();
    }
    protected void _statesSetUp()               throws Exception {
    }
    /**
     * Setup specific to testConnector().
     */
    protected void _connectorSetUp()            throws Exception {
    } 
}
