/* TestHttpParser.java */
package org.xlattice.httpd;

import java.nio.ByteBuffer;
import java.util.Random;

/**
 * @author Jim Dixon
 */

import junit.framework.*;

import org.xlattice.httpd.headers.*;

/**
 * Primarily tests Header parsing.
 *
 * @author < A HREF="mailto:jddixon@users.sourceforge.net">Jim Dixon</A>
 */
public class TestHttpParser extends TestCase {

    private Random rng = new Random();

    private byte[] b;
    private ByteBuffer buf;

    private class MockParser extends HttpParser {
        MockParser() {
            super (null, null);
        }
    }
    public TestHttpParser (String name)         throws Exception{
        super(name);
    }
    public void setUp() {
        b   = null;
        buf = null;
    }
    public void testLowLevel()                  throws Exception {
        byte[] b = "HTTP/1.1 200 ok\n".getBytes();
        int offset = 0;
        try {
            offset = HttpParser.expect("HTTP/1.", b, 0);
        } catch (MessageFormatException mfe) {
            fail("unexpected, from expect (String, byte[],int): " + mfe);
        }
        assertEquals(7, offset);
        HttpParser.expect('1', b[offset++]);
        offset = HttpParser.skipW(b, offset);
        int code = HttpParser.expect3Digits(b, offset);
        assertEquals(200, code);
        offset += 3;
        offset = HttpParser.skipW(b, offset);
        int start = offset;
        offset = HttpParser.skipToEOL(b, offset);
        int len = offset - start;
        String s = new String (b, start, len);
        assertEquals ("ok", s);
    }
    // same test, less regular input
    public void testLowLevel2()                 throws Exception {
        byte[] b = "HTTP/1.0  200 \t ok\n\n".getBytes();
        int offset = 0;
        offset = HttpParser.expect("HTTP/1.", b, 0);
        HttpParser.expect('0', b[offset++]);
        offset = HttpParser.skipW(b, offset);
        int code = HttpParser.expect3Digits(b, offset);
        offset += 3;
        offset = HttpParser.skipW(b, offset);
        int start = offset;
        offset = HttpParser.skipToEOL(b, offset);       // at first LF
        int len = offset - start;
        String s = new String (b, start, len);
        assertEquals ("ok", s);

        offset   = HttpParser.skipEOL(b, offset);
        assertEquals ('\n', b[offset]);
        offset = HttpParser.skipEOL(b, offset);
        assertEquals (b.length, offset);
    }
    public void testLowLevel3()                 throws Exception {
        byte[] b = "GET   /abc  HTTP/1.2\n\n".getBytes();
        int len    = HttpParser.skipToW(b, 0);
        assertEquals("GET",     new String(b, 0, len));
        int start  = HttpParser.skipW(b, len);
        int offset = HttpParser.skipToW(b , start);
        assertEquals ("/abc",   new String(b, start, offset - start));
        offset     = HttpParser.skipW(b, offset);
        offset     = HttpParser.expect("HTTP/1.", b, offset);
        HttpParser.expect('2', b[offset++]);
        offset     = HttpParser.skipEOL(b, offset);
        assertTrue ( HttpParser.isEOL(b[offset]) );
        offset     = HttpParser.skipEOL(b, offset);
        assertEquals (b.length, offset);
    }
    public void testCollectResponseHeaders()    throws Exception {
        EntityHeader   header1 = new ContentLengthHeader (1024);
        ResponseHeader header2 = new ServerHeader ("CryptoServer/0.0.9");
        // note incorrect order
        String headers  = header1.toString() + header2.toString() + "\n";

        HttpResponse response = new HttpResponse(
                                    HttpParser.V1_1, 200, "OK");
        HttpParser parser = new MockParser ();

        buf = ByteBuffer.wrap (headers.getBytes());
        parser.collectHeaders(response, buf);
        int offset = buf.position();
        // XXX depends upon EOL being 1 byte
        assertEquals (headers.length() - 1, offset);
        assertEquals (1024, parser.getContentLength());
        assertNull(parser.getHost());                   // no HostHeader
        assertEquals (80,   parser.getHostPort());      // the default
        // note reversed order
        String expected = HttpSListener.V1_1_200
                            + header2.toString() + header1.toString()
                            + "\n";
        assertEquals(expected, response.toString());
    }
    public void testCollectRequestHeaders()     throws Exception {
        EntityHeader  header1 = new ContentLengthHeader (4096);
        RequestHeader header2 = new HostHeader ("www.xlattice.org:8080");
        // note incorrect order
        String headers  = header1.toString() + header2.toString() + "\n";

        String requestLine  = "GET /abc/def HTTP/1.1\n";
        HttpRequest request = new HttpRequest(HttpParser.HTTP_GET,
                                    "/abc/def", HttpParser.V1_1);
        HttpParser parser   = new MockParser ();

        buf = ByteBuffer.wrap (headers.getBytes());
        parser.collectHeaders(request, buf);
        int offset = buf.position();
        // XXX depends upon EOL being one byte
        assertEquals (headers.length() - 1, offset);
        assertEquals (4096, parser.getContentLength());
        assertEquals ("www.xlattice.org", parser.getHost()); //  HostHeader
        assertEquals (8080, parser.getHostPort());
        // note reversed order
        String expected = requestLine
                            + header2.toString() + header1.toString()
                            + "\n";
        String reqStr = request.toString();
        assertEquals(expected, reqStr);
    }
    // HttpCListener PARSE METHODS //////////////////////////////////
    public void testParseFullResponse()         throws Exception {
        HttpRequest  request  = new HttpRequest(HttpParser.HTTP_GET,
                                    "/something", HttpParser.V1_1);
        HttpCListener listener = new HttpCListener(request);

        HttpResponse response = new HttpResponse(
                                    HttpParser.V1_1, 200, "OK");
        byte[] b = new byte[473];
        rng.nextBytes(b);
        Header contentLen = new ContentLengthHeader(b.length);
        Header conn       = new ConnectionHeader("close");
        Header date       = new DateHeader();
        Header server     = new ServerHeader("CryptoServer/0.0.9");
        response.addHeader(conn);             // General
        response.addHeader(contentLen);       // Entity
        response.addHeader(date);             // General
        response.addHeader(server);           // Response
        byte[] header = response.toString().getBytes();
        buf = ByteBuffer.allocate(header.length + b.length);
        buf.put(header);
        buf.put(b);
        buf.flip();

        HttpResponse parsedResponse = listener.parseStatusLine(buf);
        assertEquals (HttpParser.statusLine(HttpParser.V1_1, 200),
                      parsedResponse.getStatusLine());

        listener.collectHeaders (parsedResponse, buf);
        assertEquals(2, parsedResponse.sizeGeneralHeaders());
        assertEquals(1, parsedResponse.sizeResponseHeaders());
        assertEquals(1, parsedResponse.sizeEntityHeaders());
        assertEquals(b.length,
                ((ContentLengthHeader)(parsedResponse.getEntityHeader(0)))
                    .getValue());

        // XXX confirm that we are on an EOL and skip it
        assertTrue(listener.isEOL(buf));
        listener.skipEOL(buf);

        listener.collectEntity (parsedResponse, buf);
        byte[] entity = parsedResponse.getEntity();
        assertNotNull(entity);
        assertEquals(b.length, entity.length);
        for (int i = 0; i < b.length; i++) {
            assertEquals (b[i], entity[i]);
        }
        assertEquals (buf.limit(), buf.position());

    }
    // HttpSListener PARSE METHODS //////////////////////////////////
    // XXX test %XX encode/decode here
    //
    /**
     * Entity is tagged on to a GET, just to exercise the parser.
     */
    public void testFullRequest ()              throws Exception {
        // SET UP /////////////////////////////////////////
        HttpRequest  request  = new HttpRequest(HttpParser.HTTP_GET,
                                    "/something", HttpParser.V1_1);
        HttpSListener listener = new HttpSListener();

        byte[] b = new byte[473];
        rng.nextBytes(b);
        Header contentLen = new ContentLengthHeader(b.length);
        Header conn       = new ConnectionHeader("close");
        Header date       = new DateHeader();
        Header agent      = new UserAgentHeader("CryptoBrowser/0.0.1");
        request.addHeader(conn);             // General
        request.addHeader(contentLen);       // Entity
        request.addHeader(date);             // General
        request.addHeader(agent);            // Request
        byte[] header = request.toString().getBytes();
        buf = ByteBuffer.allocate(header.length + b.length);
        buf.put(header);
        buf.put(b);
        buf.flip();

        // PARSE //////////////////////////////////////////
        HttpRequest parsedRequest = listener.parseRequestLine (buf);
        assertEquals (HttpParser.HTTP_GET, parsedRequest.getMethod());
        assertEquals ("/something",        parsedRequest.getURI());
        assertEquals (HttpParser.V1_1,     parsedRequest.getHttpVersion());

        listener.collectHeaders (parsedRequest, buf);
        assertEquals(2, parsedRequest.sizeGeneralHeaders());
        assertEquals(1, parsedRequest.sizeRequestHeaders());
        assertEquals(1, parsedRequest.sizeEntityHeaders());
        assertEquals(b.length,
                ((ContentLengthHeader)(parsedRequest.getEntityHeader(0)))
                    .getValue());

        assertTrue(listener.isEOL(buf));
        listener.skipEOL(buf);

        listener.collectEntity (parsedRequest, buf);
        byte[] entity = parsedRequest.getEntity();
        assertNotNull(entity);
        assertEquals(b.length, entity.length);
        for (int i = 0; i < b.length; i++) {
            assertEquals (b[i], entity[i]);
        }
        assertEquals (buf.limit(), buf.position());
    }
}
