/* TestHttpVersion.java */
package org.xlattice.httpd;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @author Jim Dixon
 */

import junit.framework.*;

import org.xlattice.httpd.headers.*;
import org.xlattice.transport.ConnectionListener;
import org.xlattice.transport.SchedulableConnection;
import org.xlattice.transport.mockery.MockSchConnection;
import org.xlattice.transport.mockery.MockSelectionKey;
import org.xlattice.transport.mockery.MockSocket;
import org.xlattice.transport.mockery.MockSocketChannel;
/**
 * 
 * @author <A HREF="mailto:jddixon@users.sourceforge.net">Jim Dixon</A>
 */
public class TestHttpVersion extends TestCase {

    HttpSListener serverListener;
    HttpCListener clientListener;
    HttpRequest   request;
    HttpResponse  response;
    
    SchedulableConnection mockCnx;
    SocketChannel mockSChan;
    
    public TestHttpVersion (String name) {
        super(name);
    }

    public void setUp () {
        serverListener = null;
        clientListener = null;
        request        = null;
        response       = null;
        mockCnx        = null;
        
        mockSChan      = new MockSocketChannel();
        MockSocket sock = (MockSocket)mockSChan.socket();
        try {
            sock.bind(null);
            sock.connect(new InetSocketAddress("1.2.3.4", 5678));
        } catch (IOException ioe) {
            System.err.println("IOException from MockSocket??");
        }
        serverListener = new HttpSListener (); 
        mockCnx = new MockSchConnection (mockSChan,
                                         null,  // IOScheduler scheduler
                                         serverListener);
        ((MockSchConnection)mockCnx).setKey(new MockSelectionKey());
    }
    
    public void testConstructor ()              throws Exception {
        assertNotNull(serverListener);
        assertNotNull(mockCnx);
        assertEquals (0, serverListener.curByte);
        assertFalse  (serverListener.haveCR);
        assertFalse  (serverListener.haveCRLF);
    }
    public void testParseSimpleRequest ()       throws Exception {
        request = new HttpRequest(
                    HttpParser.HTTP_GET, "/something", HttpParser.V0_9);
        ByteBuffer fakery = ByteBuffer.wrap(request.toString().getBytes());
        ((MockSchConnection)mockCnx).fakeDataIn(fakery);
        mockCnx.readyToRead();
        assertEquals (404, serverListener.getStatus());
        assertEquals ("GET", serverListener.getMethod());
        assertEquals ("/something", serverListener.getPath());
        assertEquals (HttpParser.V0_9, serverListener.getVersion());
    }
    public void testParseRequestLine ()         throws Exception {
        request = new HttpRequest(
                    HttpParser.HTTP_GET, "/something", HttpParser.V1_0);
        ByteBuffer fakery = ByteBuffer.wrap(request.toString().getBytes());
        ((MockSchConnection)mockCnx).fakeDataIn(fakery);
        mockCnx.readyToRead(); 
        // missing Host header
        assertEquals (400, serverListener.getStatus());
        assertEquals ("GET", serverListener.getMethod());
        assertEquals ("/something", serverListener.getPath());
        assertEquals (HttpParser.V1_0, serverListener.getVersion());
    }
    public void testParseMultiLineRequest ()         throws Exception {
        request = new HttpRequest(
                    HttpParser.HTTP_GET, "/something", HttpParser.V1_0);
        request.addHeader((Header) new HostHeader("dunno") );
        ByteBuffer fakery = ByteBuffer.wrap(request.toString().getBytes());
        ((MockSchConnection)mockCnx).fakeDataIn(fakery);
        mockCnx.readyToRead();
        assertEquals (404, serverListener.getStatus());
        assertEquals ("GET", serverListener.getMethod());
        assertEquals ("/something", serverListener.getPath());
        assertEquals (HttpParser.V1_0, serverListener.getVersion());
    }
    /** Bad version number. */
    public void testBadRequest400a ()            throws Exception {
        try {
            request = new HttpRequest(
                    HttpParser.HTTP_GET, "/something", HttpParser.V1_1 + 2);
            fail("expected MFE");
        } catch (MessageFormatException mfe) { /* success */ }
    }
    /** Bad version number. */
    public void testBadRequest400b ()            throws Exception {
        try {
            request = new HttpRequest(
                    HttpParser.HTTP_GET, "/something", HttpParser.V0_9 - 1);
            fail("expected MFE");
        } catch (MessageFormatException mfe) { /* success */ }
    }
    /** Malformed path. */
    public void testBadRequest400c ()           throws Exception {
        try {
            request = new HttpRequest(
                    HttpParser.HTTP_GET, "something", HttpParser.V0_9);
            fail("expected MFE");
        } catch (MessageFormatException mfe) { /* success */ }
    }
    /** Malformed path, above 0.9. */
    public void testBadRequest501a ()           throws Exception {
        try {
            request = new HttpRequest(
                    HttpParser.HTTP_GET, "something", HttpParser.V1_1);
            fail("expected MFE");
        } catch (MessageFormatException mfe) { /* success */ }
    }
    /** Unsupported command. */
    public void testBadRequest501b ()           throws Exception {
        try {
            request = new HttpRequest(
                    HttpParser.HTTP_OPTIONS, "something", HttpParser.V1_1);
            fail("expected NIE");
        } catch (NotImplementedException mfe) { /* success */ }
    }
}
