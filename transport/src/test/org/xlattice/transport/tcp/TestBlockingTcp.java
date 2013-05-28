/* TestBlockingTcp.java */
package org.xlattice.transport.tcp;

import java.net.InetAddress;
import junit.framework.*;
import org.xlattice.*;
import org.xlattice.transport.*;

/**
 * @author Jim Dixon
 */

public class TestBlockingTcp extends AbstractBlockingTest {

    InetAddress thisHost;
    int clientPort;
    int serverPort;
    
    public TestBlockingTcp (String name) {
        super(name);
    }
    public void _setUp() {
        try {
            thisHost = InetAddress.getLocalHost();
        } catch (java.net.UnknownHostException uhe) {
            System.err.println("can't get local host's name!");
        }
        clientPort = 0;       // EPHEMERAL PORT 
        serverPort = 7456;
        clientAddr = new IPAddress (thisHost, clientPort);
        serverAddr = new IPAddress (thisHost, serverPort);
        transport  = new Tcp();
        clientEnd  = new EndPoint (transport, clientAddr);
        serverEnd  = new EndPoint (transport, serverAddr);
    }
    public void _tearDown() {
    }
    // UNIT TESTS ///////////////////////////////////////////////////
    protected void _checkTestVariables ()       throws Exception {
    }
    protected void _constructorSetUp()          throws Exception {
        // akc = new TcpAcceptor ((IPAddress)serverAddr);
    }
    protected void _statesSetUp()               throws Exception {
    }
    protected void _ioSetUp()                   throws Exception {
    }
    protected void _connectorSetUp()            throws Exception {
    }
}
