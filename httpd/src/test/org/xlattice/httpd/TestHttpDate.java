/* TestHttpDate.java */
package org.xlattice.httpd;

//import java.util.Date;

import junit.framework.*;

/**
 * @author Jim Dixon
 */
public class TestHttpDate extends TestCase {

    public TestHttpDate (String name)           throws Exception{
        super(name);
    }
    public void testNow()                       throws Exception {
        HttpDate now  = new HttpDate();
        String nowStr = now.toString();
        assertTrue (nowStr.endsWith("GMT"));

        HttpDate then = new HttpDate(
                "Tue, 15 Nov 1994 08:12:31 PST");
        String thenStr = then.toString();
        assertEquals ("Tue, 15 Nov 1994 16:12:31 GMT", thenStr);
    }
}
