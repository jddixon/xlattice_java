/* AbstractSchedulableTest.java */
package org.xlattice.transport;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Date;
import java.util.Random;

import junit.framework.*;
import org.xlattice.*;
import org.xlattice.transport.*;
import org.xlattice.util.NonBlockingLog;
import org.xlattice.util.Timestamp;

/**
 * A common test for schedulable versions of the XLattice transport
 * classes.
 *
 * Implementation will require the development of a suite of
 * ConnectionListeners (classes that extend ConnectionListener) and
 * possibly of accompanying suites of AcceptorListeners and
 * ConnectorListeners.
 *
 * A longer term objective may be the development of a superclass
 * which can be used for testing blocking (non-schedulable)
 * versions of the classes as well.
 *
 * @author Jim Dixon
 */
public abstract class AbstractSchedulableTest extends TestCase {

    // DEBUG
    protected static NonBlockingLog debugLog 
                            = NonBlockingLog.getInstance("debug.log");
    protected void DEBUG_MSG(String s) {
        debugLog.message("AbsSchedTest" + s);
    }
    // END
 
    // XXX TAKE CARE IN CHANGING THIS - it determines the size of
    // XXX server buffers.  At least a build clean is required.
    public final static int TEST_BUFSIZE = 1024;

    // XXX There is a CNX_BUFSIZE = 65536
    // XXX defined in tcpip/SchedulableTcpConnection

    // XXX THERE IS A KNOWN PROBLEM IN DEALING WITH MESSAGES GREATER
    // XXX IN LENGTH THAN EITHER OF THESE BUFFER SIZES.
 
    /**
     * Generic versions of local variables.  These need to be
     * initialized in each subclass.
     */
    protected SchedulableConnector  ctr;
    protected SchedulableConnection cnx;
    protected SchedulableAcceptor   acc;

    protected EndPoint     clientEnd;
    protected EndPoint     serverEnd;
    protected ClientServer transport;

    protected Address      clientAddr;
    protected Address      serverAddr;

    protected IOScheduler scheduler;

    private final Random rng = new Random (new Date().getTime());

    protected final Object flag = new Object();   // for EchoTable monitor

    // SUBCLASS CONSTRUCTORS SHOULD FOLLOW THIS FORM ////////////////
    // but the class name should begin with Test, as for example
    //   public TestSchedulableTcp (String name) {
    public AbstractSchedulableTest (String name) {
        super(name);
        // DEBUG
        DEBUG_MSG(" constructor");
        // END
    }
    // TO IMPLEMENT TestCase: ///////////////////////////////////////
    public void setUp    () {
        // DEBUG
        DEBUG_MSG(".setUp()");
        // END
        ctr = null;
        cnx  = null;
        acc  = null;

        clientEnd   = null;
        serverEnd   = null;
        clientAddr  = null;
        serverAddr  = null;

        try {
            scheduler = new IOScheduler();
        } catch (IOException ioe) {
            System.err.println("can't create IOScheduler: " + ioe);
        }
        try {
            while (!scheduler.isRunning()) 
                Thread.currentThread().sleep(2);    // ms
        } catch (InterruptedException ie) { /* ignore */ }
        _setUp();
    }
    public void tearDown () {
        // DEBUG
        DEBUG_MSG(".tearDown()");
        // END
        // the next line should never have any effect
        if (scheduler != null)
            try { scheduler.close(); }  catch (Exception e) { /* ignore */ }
        if (acc != null)
            try { acc.close(); }        catch (Exception e) { }
        if (cnx != null)
            try { cnx.close(); }        catch (Exception e) { }
        // no close() for connectors
        _tearDown();
    }
    /*
     * Any transport-specific setup needs to be done in the subclass.
     * EndPoints and other such variables should be initialized here.
     * All addresses will be reused; at least TCP/IP sockets will have
     * to be marked accordingly.  transport must be initialized here.
     */
    protected abstract void _setUp();
    /*
     * Any transport-specific teardown after unit tests.
     */
    protected abstract void _tearDown();

    // UNIT TESTS ///////////////////////////////////////////////////
    /**
     * Test the internal consistency of variables after they have
     * been initialized.  This should be called at the very beginning
     * of at least one test method.
     */
    protected void checkTestVariables()         throws Exception {
        // end points must share same transport
        transport = (ClientServer)clientEnd.getTransport();
        // XXX equals() instead
        assertTrue ( transport == serverEnd.getTransport() );

        // addresses must match those declared in EndPoints
        assertTrue (clientAddr.equals(clientEnd.getAddress()));
        assertTrue (serverAddr .equals(serverEnd .getAddress()));

        assertTrue (scheduler.isRunning());

        _checkTestVariables();
    }
    protected abstract void _checkTestVariables()
                                                throws Exception;
    ///////////////////////////////////////////////////////
    /**
     * Any setup specific to testConstructors.
     */
    protected abstract void _constructorSetUp() throws Exception;
    public void testConstructors()              throws Exception {
        _constructorSetUp();
        // DEBUG
        DEBUG_MSG(".testConstructors");
        // END
        checkTestVariables();
        // Acceptor CONSTRUCTOR /////////////////
        acc = (SchedulableAcceptor)transport
                                        .getAcceptor(serverAddr, false);
        assertNotNull(acc);
        assertNull(acc.getKey());

        SelectableChannel sChan = acc.getChannel();
        assertNotNull(sChan);
        assertFalse(sChan.isBlocking());
        assertTrue(sChan.isOpen());
        try { acc.close();   } catch (Exception e) { /* ignore */ }
        try { sChan.close(); } catch (Exception e) { /* ignore */ }

        // Connection CONSTRUCTOR ///////////////

        // Connector CONSTRUCTOR ////////////////
        ctr = (SchedulableConnector)transport
                                    .getConnector(serverAddr, false);
        assertNotNull(ctr);
        assertNull(ctr.getKey());

        sChan = ctr.getChannel();
        assertNotNull(sChan);
        assertFalse(sChan.isBlocking());
        assertTrue(sChan.isOpen());
        try { sChan.close(); } catch (Exception e) { /* ignore */ }
        try { scheduler.close(); } catch (Exception e) { /* ignore */ }
    } 
    ///////////////////////////////////////////////////////
    /**
     * Any setup specific to testAcceptor.
     */
    protected abstract void _acceptorSetUp()      throws Exception;
    /**
     * Test the schedulable acceptor using blocking connections.
     *
     * This leaves the machine littered with connections in TIME_WAIT.
     * For a discussion see
     * http://forum.java.sun.com/thread.jspa?threadID=556212&messageID=2726932
     */
    public void testAcceptor()                    throws Exception {
        _acceptorSetUp();
        // DEBUG
        DEBUG_MSG(".testAcceptor()");
        // END
        acc = (SchedulableAcceptor)transport
                                        .getAcceptor(serverAddr, false);
        acc.setCnxListenerFactory(new EchoSFactory());
        acc.setReceiver(scheduler);
        scheduler.add(acc);
        // XXX KLUDGE - NEED TEST ON Acceptor - isClosed() ?
        Thread.currentThread().sleep(20);  // WAS 2
        // XXX END
        ServerSocketChannel srvChan = (ServerSocketChannel)acc.getChannel();
        // XXX FAILS if the sleep is for 2 ms
        assertTrue(srvChan.isRegistered());

        // test with blocking connections
        for (int k = 0; k < 16; k++) {
            Connector ktr = transport.getConnector(serverAddr, true);
            Connection knx = ktr.connect(clientEnd, true);
            DEBUG_MSG(".testAcceptor, knx created");

            OutputStream outs = knx.getOutputStream();
            InputStream  ins  = knx.getInputStream();

            byte[] dataOut = new byte[TEST_BUFSIZE];
            rng.nextBytes(dataOut);     // fill with random bits
            outs.write(dataOut, 0, TEST_BUFSIZE);
            byte[] dataIn  = new byte[TEST_BUFSIZE];
            DEBUG_MSG(".testAcceptor, before cnx read");
            ins.read (dataIn, 0, TEST_BUFSIZE);
            DEBUG_MSG(".testAcceptor, after cnx read");
            for (int j = 0; j < TEST_BUFSIZE; j++) {
                assertEquals(dataOut[j], dataIn[j]);
            }
            knx.close();
            assertTrue(knx.isClosed());
        }
        // XXX NEED TEST 
        acc.close();
        // DEBUG - This sleep makes testConnector() succeed.  If the
        // sleep is moved to testConnector() at the beginning, it fails
        // with a 'socket already in use' error.
        Thread.currentThread().sleep(20);   // ms
        // END
        try { scheduler.close(); } catch (Exception e) { /* ignore */ }
        Thread schedThread = scheduler.getThread();
        if (schedThread.isAlive())
            schedThread.join();
        assertFalse(schedThread.isAlive());
        assertFalse(scheduler.isRunning());
    }
    ///////////////////////////////////////////////////////
    /**
     * Any setup specific to testStates.
     */
    protected abstract void _statesSetUp()      throws Exception;
    public void testStates()                    throws Exception {
        _statesSetUp();

        try { scheduler.close(); } catch (Exception e) { /* ignore */ }
        Thread schedThread = scheduler.getThread();
        if (schedThread.isAlive())
            schedThread.join();
        assertFalse(schedThread.isAlive());
    }
    ///////////////////////////////////////////////////////
    /**
     * Any setup specific to testConnector.
     */
    protected abstract void _connectorSetUp()   throws Exception;
    public void testConnector ()                throws Exception {
        _connectorSetUp();
        // DEBUG
        DEBUG_MSG(".testConnector()");
        // END

        // let's have a server
        // XXX GETS SocketException: 'Address already in use'
        acc = (SchedulableAcceptor)transport
                                        .getAcceptor(serverAddr, false);
        acc.setCnxListenerFactory(new EchoSFactory());
        acc.setReceiver(scheduler);
        scheduler.add(acc);
        Thread.currentThread().sleep(2);
        ServerSocketChannel srvChan = (ServerSocketChannel)acc.getChannel();
        assertTrue(srvChan.isRegistered());

        int n = 16;

        EchoTable table = new EchoTable(n);
        assertNotNull(table);
        assertEquals (n, table.size());

        for (int i = 0; i < n; i++) {
            ctr = (SchedulableConnector)transport
                                        .getConnector(serverAddr, false);
//          EchoTableCListener listener = new EchoTableCListener(i, table);
            EchoCFactory factory = new EchoCFactory(i, table);
//          assertEquals(i, listener.getIndex());
//          assertTrue  (table == listener.getTable());
//          assertNull  (listener.getConnection());

            ctr.setCnxListenerFactory(factory);
            ctr.setReceiver(scheduler);
            assertNull (ctr.getKey());

            scheduler.add(ctr);
        }

        DEBUG_MSG(".testConnector, waiting for EchoTable signal");
        synchronized (table) {
            while (!table.isFinished())
                table.wait();
        }
        DEBUG_MSG(".testConnector, EchoTable signal received");
        assertEquals(n, table.seenSoFar());

        for (int i = 0; i < n; i++) {
            byte[] dataOut = table.dataSent[i];
            byte[] dataIn  = table.dataRcvd[i];
            for (int j = 0; j < TEST_BUFSIZE; j++)
                assertEquals (dataOut[j], dataIn[j]);
        }
        acc.close();
        try { scheduler.close(); } catch (Exception e) { /* ignore */ }
        Thread schedThread = scheduler.getThread();
        if (schedThread.isAlive())
            schedThread.join();
        assertFalse(schedThread.isAlive());
    }
    // UTILITIES ////////////////////////////////////////////////////
    /**
     * XXX Replicated in org.xlattice.util.StringLib.
     *
     * XXX This will only work if the length is a multiple of 4.
     */
    public String byteArrayToHex (byte[] b) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < b.length; i += 4) {
            int val = ((0xff & b[i])     << 24) |
                      ((0xff & b[i + 1]) << 16) |
                      ((0xff & b[i + 2]) <<  8) |
                      ((0xff & b[i + 3])      ) ;
            sb.append ( Integer.toHexString(val) );
        }
        return sb.toString();
    }
}
