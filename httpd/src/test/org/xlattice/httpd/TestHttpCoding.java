/* TestHttpCoding.java */
package org.xlattice.httpd;

/**
 * @author Jim Dixon
 */

//import java.util.Coding;

import junit.framework.*;

/**
 * Tests methods for HTTP encoding and decoding (%XX codes).
 *
 * @author < A HREF="mailto:jddixon@users.sourceforge.net">Jim Dixon</A>
 */
public class TestHttpCoding extends TestCase {

    public TestHttpCoding (String name)         throws Exception{
        super(name);
    }
    public void testDecodingOneByte()           throws Exception {
        assertEquals ("\t", HttpParser.httpDecode("%09".getBytes()));
        assertEquals (" ",  HttpParser.httpDecode("%20".getBytes()));
        assertEquals ("/",  HttpParser.httpDecode("%2F".getBytes()));
        assertEquals (";",  HttpParser.httpDecode("%3B".getBytes()));
        assertEquals ("?",  HttpParser.httpDecode("%3F".getBytes()));
        assertEquals ("`",  HttpParser.httpDecode("%60".getBytes()));
    }
    public void testDecodingStrings()           throws Exception {
        assertEquals("",              HttpParser.httpDecode("".getBytes()));
        assertEquals("abc =? {def};", HttpParser.httpDecode(
                     "abc%20%3D%3F%20%7Bdef%7D%3B".getBytes()));
    }
    public void testCharEncoding()              throws Exception {
        assertEquals("%09", HttpParser.encodeChar('\t'));
        assertEquals("%20", HttpParser.encodeChar(' '));
        assertEquals("%26", HttpParser.encodeChar('&'));
        assertEquals("%5B", HttpParser.encodeChar('['));
    }
    public void testEncodingOneByte()           throws Exception {
        assertEquals("%09", HttpParser.httpEncode("\t"));
        assertEquals("%20", HttpParser.httpEncode(" "));
        assertEquals("%26", HttpParser.httpEncode("&"));
        assertEquals("%5B", HttpParser.httpEncode("["));
        for (char c = 'A'; c <= 'Z'; c++) {
            String s = new String( new char[] { c } );
            assertEquals (s, HttpParser.httpEncode(s));
        }
    }
    public void testEncodingStrings()           throws Exception {
        assertEquals("abc%20%3D%3F%20%7Bdef%7D%3B", 
                     HttpParser.httpEncode("abc =? {def};"));
    }
}
