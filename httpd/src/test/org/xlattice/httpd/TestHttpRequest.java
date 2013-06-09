/* TestHttpRequest.java */
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
public class TestHttpRequest extends TestCase {

    HttpRequest request;
    
    public TestHttpRequest (String name)       throws Exception{
        super(name);
    }
    public void setUp()                         throws Exception {
        request = null;
    }
    public void testEmptyV1_1Message()          throws Exception {
        request = new HttpRequest(HttpParser.HTTP_GET, 
                                        "/abc.dat", HttpParser.V1_1);
        assertNotNull(request);
        assertEquals(0, request.sizeGeneralHeaders());
        assertEquals(0, request.sizeRequestHeaders());
        assertEquals(0, request.sizeEntityHeaders());
        assertEquals ("GET /abc.dat HTTP/1.1\n\n", request.toString());
        
        assertNull(request.getEntity());
    }
    public void testEmptyV0_9Message()          throws Exception {
        request = new HttpRequest(HttpParser.HTTP_GET, 
                                        "/abc.dat", HttpParser.V0_9);
        assertNotNull (request);
        assertEquals ("GET /abc.dat\n", request.toString());
    }
}
