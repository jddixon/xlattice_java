/* TestHttpResponse.java */
package org.xlattice.httpd;

import junit.framework.*;

/**
 * @author Jim Dixon
 */

import org.xlattice.httpd.headers.*;

/**
 * 
 * @author < A HREF="mailto:jddixon@users.sourceforge.net">Jim Dixon</A>
 */
public class TestHttpResponse extends TestCase {

    HttpResponse response;
    
    public TestHttpResponse (String name)       throws Exception{
        super(name);
    }
    public void setUp()                         throws Exception {
        response = null;
    }
    public void testEmptyV0_9Message()          throws Exception {
        response = new HttpResponse();
        assertNotNull(response);
        assertNull(response.toString());
        
        assertNull(response.getEntity());       // we haven't assigned it
    }
    public void testV1_1MessageFromStatusLine() throws Exception {
        response = new HttpResponse(HttpSListener.V1_1_200);
        assertNotNull(response);
        assertEquals(0, response.sizeGeneralHeaders());
        assertEquals(0, response.sizeResponseHeaders());
        assertEquals(0, response.sizeEntityHeaders());
        assertEquals (HttpSListener.V1_1_200 + "\n", response.toString());
        
        assertNull(response.getEntity());
    }
    public void testV1_1MessageFromParams()     throws Exception {
        response = new HttpResponse(HttpParser.V1_1, 200, "OK");
        assertNotNull(response);
        assertEquals(0, response.sizeGeneralHeaders());
        assertEquals(0, response.sizeResponseHeaders());
        assertEquals(0, response.sizeEntityHeaders());
        assertEquals (HttpSListener.V1_1_200 + "\n", response.toString());
        
        assertNull(response.getEntity());
    }
    public void testV1_1WithHeaders()           throws Exception {
        response = new HttpResponse(HttpParser.V1_1, 200, "OK");
        Header header1 = new ContentLengthHeader (1024);
        Header header2 = new ServerHeader ("CryptoServer/0.0.9");
        response.addHeader (header1); 
        response.addHeader (header2);
        // note reversed order
        String expected = HttpSListener.V1_1_200 
                            + header2.toString() + header1.toString()
                            + "\n";
        assertEquals(expected, response.toString());
    }
}
