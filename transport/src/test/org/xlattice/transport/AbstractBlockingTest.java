/* AbstractBlockingTest.java */
package org.xlattice.transport;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Random;

import java.net.Socket;         // DEBUG ONLY

import junit.framework.*;
import org.xlattice.*;

/**
 * A common test for blocking versions of the XLattice transport
 * classes.  
 * 
 * A longer term objective may be the development of a superclass
 * which can be used for testing blocking (non-schedulable) 
 * versions of the classes as well.
 *
 * @author Jim Dixon
 */
public abstract class AbstractBlockingTest extends TestCase {

    public static final int TEST_BUFSIZE = 1024;
    private Random rng = new Random ( new Date().getTime() );    
    /** 
     * Generic versions of local variables.  These need to be 
     * initialized in each subclass.
     */
    protected Connector    ktr;
    protected Connection   knx;
    protected Acceptor     akc;
    protected LittleBlockingServer server; 
    protected EndPoint     clientEnd;
    protected EndPoint     serverEnd;
    protected ClientServer transport;

    protected Address      clientAddr;
    protected Address      serverAddr;
    
    // SUBCLASS CONSTRUCTORS SHOULD FOLLOW THIS FORM ////////////////
    // but the class name should begin with Test, as for example
    //   public TestTcp (String name) {
    public AbstractBlockingTest (String name) {
        super(name);
    }
    // TO IMPLEMENT TestCase: ///////////////////////////////////////
    public void setUp    () {
        ktr    = null;
        knx    = null;
        akc    = null;
        server = null;
        
        clientEnd   = null;
        serverEnd    = null;
        clientAddr  = null;
        serverAddr   = null;
       
        _setUp();
    }
    public void tearDown () {
        try { akc.close(); }    catch (Exception e) { }
        try { knx.close(); }    catch (Exception e) { }
        try { server.close(); } catch (Exception e) { }
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
        assertTrue ( transport == serverEnd.getTransport() );

        // addresses must match those declared in EndPoints
        assertTrue (clientAddr.equals(clientEnd.getAddress()));
        assertTrue (serverAddr .equals(serverEnd .getAddress()));

        _checkTestVariables();
    }
    protected abstract void _checkTestVariables()
                                                throws Exception;
    ///////////////////////////////////////////////////////
    /**
     * Any setup specific to testConstructor.
     */
    protected abstract void _constructorSetUp() throws Exception;
    public void testConstructor()               throws Exception {
        _constructorSetUp();
        checkTestVariables();
       
        akc = transport.getAcceptor(serverAddr, true);
        assertNotNull(akc);
        assertFalse(akc.isClosed());
        EndPoint akcEnd = akc.getEndPoint();
        // XXX equals() better
        assertTrue (akcEnd.getTransport() == transport);
        Address akcAddr = akcEnd.getAddress();
        assertTrue (serverAddr.equals(akcAddr));
        akc.close();
        assertTrue(akc.isClosed());
    }
    ///////////////////////////////////////////////////////
    /**
     * Any setup specific to testStates.
     */
    protected abstract void _statesSetUp()      throws Exception;
    
    /**
     * Walk a connection through various states.  Check for (some)
     * illegal transitions.
     *
     * XXX MUST REVISE TO NOT USE getConnection()
     */
//  public void testStates()                    throws Exception {
//      _statesSetUp();
//      knx = transport.getConnection((Address)null, (Address)null, true);
//      assertNotNull(knx);
//      assertEquals(Connection.UNBOUND, knx.getState());
//      try { 
//          knx.bindFarEnd(serverEnd); 
//          fail("connected unbound connection without an exception");
//      } catch (IllegalStateException ioe) { /* success */ }
//      
//      knx.bindNearEnd(clientEnd);
//      assertEquals(Connection.BOUND, knx.getState());
//      try { 
//          knx.bindNearEnd(clientEnd); 
//          fail("bound already bound connection without an exception");
//      } catch (IllegalStateException ioe) { /* success */ }

//      LittleBlockingServer server = new LittleBlockingServer(serverEnd);
//      assertTrue (server.isRunning());

//      knx.bindFarEnd(serverEnd);
//      // on same host so immediately goes to CONNECTED
//      assertEquals(Connection.CONNECTED, knx.getState());
//      try { 
//          knx.bindNearEnd(clientEnd); 
//          fail("bound connected connection without an exception");
//      } catch (IllegalStateException ioe) { /* success */ }
//      try { 
//          knx.bindFarEnd(serverEnd); 
//          fail("connected connected connection without an exception");
//      } catch (IllegalStateException ioe) { /* success */ }
//   
//      knx.close();
//      assertEquals(Connection.DISCONNECTED, knx.getState());
//      try { 
//          knx.bindNearEnd(clientEnd); 
//          fail("bound closed connection without an exception");
//      } catch (IllegalStateException ioe) { /* success */ }
//      try { 
//          knx.bindFarEnd(serverEnd); 
//          fail("connected closed connection without an exception");
//      } catch (IllegalStateException ioe) { /* success */ }
//      server.close();
//  }
    ///////////////////////////////////////////////////////
    /**
     * Any setup specific to testIO.
     */
    protected abstract void _ioSetUp()          throws Exception;
    /**
     * Test non-blocking reading and writing.
     *
     * XXX REVISE TO USER getConnector()
     */
//  public void testIO ()                       throws Exception {
//      _ioSetUp();
//      LittleBlockingServer server = new LittleBlockingServer(serverEnd, true);
//      knx = transport.getConnection(clientAddr, serverAddr, true);

//      OutputStream outs = knx.getOutputStream();
//      InputStream  ins  = knx.getInputStream();
//      
//      byte[] dataOut = new byte[TEST_BUFSIZE];
//      rng.nextBytes(dataOut);                // fill with random bits
//      outs.write(dataOut, 0, TEST_BUFSIZE);
//      byte[] dataIn  = new byte[TEST_BUFSIZE];
//      ins.read(dataIn, 0, TEST_BUFSIZE);
//      for (int i = 0; i < TEST_BUFSIZE; i++)
//          assertEquals (dataOut[i], dataIn[i]);
//      
//      server.close();
//      knx.close();        
//  }
    protected abstract void _connectorSetUp()   throws Exception;
    /**
     * Same as preceding test but uses Connector and is run many times
     * in immediate succession.
     */
    public void testConnector ()                throws Exception {
        _connectorSetUp();
        // XXX Bind error here unless knx.close() at end of previous test.
        LittleBlockingServer server = new LittleBlockingServer(serverEnd, true);
        for (int k = 0; k < 16; k++) {
            ktr = transport.getConnector(serverAddr, true);
            // returns a blocking connection
            knx = ktr.connect(clientEnd, true);
    
            OutputStream outs = knx.getOutputStream();
            InputStream  ins  = knx.getInputStream();
            
            byte[] dataOut = new byte[TEST_BUFSIZE];
            rng.nextBytes(dataOut);                // fill with random bits
            outs.write(dataOut, 0, TEST_BUFSIZE);
            byte[] dataIn  = new byte[TEST_BUFSIZE];
            ins.read(dataIn, 0, TEST_BUFSIZE);
            for (int i = 0; i < TEST_BUFSIZE; i++)
                assertEquals (dataOut[i], dataIn[i]);
        }
        server.close();
    }
        
}
