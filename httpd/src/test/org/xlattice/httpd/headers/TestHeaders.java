/* TestHeaders.java */
package org.xlattice.httpd.headers;

import junit.framework.*;

/**
 * @author Jim Dixon
 */

import org.xlattice.httpd.*;

/**
 * Tests functionality of the 47 RFC2616 Headers plus the catch-all
 * ExtensionHeader.  First implementation and tests on headers will
 * be simplistic, so as to get code out fast.  Once all headers are
 * implemented, code can be fleshed out.
 * 
 * @author <A HREF="mailto:jddixon@users.sourceforge.net">Jim Dixon</A>
 */
public class TestHeaders extends TestCase {

    private byte[] data;
    private Header header;
    
    public TestHeaders (String name)            throws Exception {
        super(name);
    }
    public void setup()                         throws Exception {
        data   = null;
        header = null;
    }
    // UNIT TESTS IN ALPHABETICAL ORDER /////////////////////////////
    // Numbers are RFC 2616 section numbers /////////////////////////
  
    /**
     * Accept, 14.1.
     * 
     */
    public void testAccept()                      throws Exception {
        String mediaTypes = "audio/*; q=0.2, audio/basic";
        header = new AcceptHeader(mediaTypes);
        assertTrue (header instanceof RequestHeader);
        assertEquals ("Accept: " + mediaTypes + "\n", header.toString());
        assertEquals (mediaTypes, ((AcceptHeader)header).getValue());
        assertEquals ("Accept", header.getTag());
    }
    /**
     * AcceptCharset, 14.2.
     * 
     */
    public void testAcceptCharset()                      throws Exception {
        String charSets = "iso-8859-5, unicode-1-1;q=0.8";
        header = new AcceptCharsetHeader(charSets);
        assertTrue (header instanceof RequestHeader);
        assertEquals ("Accept-Charset: " + charSets + "\n", header.toString());
        assertEquals (charSets, ((AcceptCharsetHeader)header).getValue());
        assertEquals ("Accept-Charset", header.getTag());
    }
    /**
     * AcceptEncoding, 14.3.
     * 
     * XXX Must accept nothing and *.
     */
    public void testAcceptEncoding()                      throws Exception {
        String codings = "compress, gzip";
        header = new AcceptEncodingHeader(codings);
        assertTrue (header instanceof RequestHeader);
        assertEquals ("Accept-Encoding: " + codings + "\n", header.toString());
        assertEquals (codings, ((AcceptEncodingHeader)header).getValue());
        assertEquals ("Accept-Encoding", header.getTag());
    }
    /**
     * AcceptLanguage, 14.4.
     * 
     */
    public void testAcceptLanguage()                      throws Exception {
        String languages = "en-gb, en;q=0.5";
        header = new AcceptLanguageHeader(languages);
        assertTrue (header instanceof RequestHeader);
        assertEquals ("Accept-Language: " + languages + "\n", header.toString());
        assertEquals (languages, ((AcceptLanguageHeader)header).getValue());
        assertEquals ("Accept-Language", header.getTag());
    }
    /**
     * Age, 14-6.
     */
    public void testAgeHeader()                 throws Exception {
        String deltaSec = "0";
        header = new AgeHeader(deltaSec);
        assertTrue (header instanceof ResponseHeader);
        assertEquals("Age: " + deltaSec + "\n", header.toString());
        assertEquals(0, ((AgeHeader)header).getValue());

        header = new AgeHeader(13);
        assertEquals(13, ((AgeHeader)header).getValue());
    }
    /**
     * Connection, 14.10.
     * 
     */
    public void testConnection()                throws Exception {
        String option = "close";
        header = new ConnectionHeader(option);
        assertTrue (header instanceof GeneralHeader);
        assertEquals ("Connection: " + option + "\n", header.toString());
        assertEquals (option, ((ConnectionHeader)header).getValue());
        assertEquals ("Connection", header.getTag());
    }
    /**
     * ContentEncoding, 14.11.
     * 
     */
    public void testContentEncoding()                      throws Exception {
        String encoding = "gzip";
        header = new ContentEncodingHeader(encoding);
        assertTrue (header instanceof EntityHeader);
        assertEquals ("Content-Encoding: " + encoding + "\n", header.toString());
        assertEquals (encoding, ((ContentEncodingHeader)header).getValue());
        assertEquals ("Content-Encoding", header.getTag());
    }
    /**
     * ContentLanguage, 14.12.
     * 
     */
    public void testContentLanguage()                      throws Exception {
        String languages = "mi, en";
        header = new ContentLanguageHeader(languages);
        assertTrue (header instanceof EntityHeader);
        assertEquals ("Content-Language: " + languages + "\n", header.toString());
        assertEquals (languages, ((ContentLanguageHeader)header).getValue());
        assertEquals ("Content-Language", header.getTag());
    }
    /**
     * Content-Length, 14-13.
     */
    public void testContentLengthHeader()             throws Exception {
        String value = "14";
        header = new ContentLengthHeader(value);
        assertTrue (header instanceof EntityHeader);
        assertEquals("Content-Length: " + value + "\n", header.toString());
        assertEquals(14, ((ContentLengthHeader)header).getValue());

        header = new ContentLengthHeader(47);
        assertEquals(47, ((ContentLengthHeader)header).getValue());

        try {
            header = new ContentLengthHeader("-2");
            fail("accepted negative content length");
        } catch (MalformedHeaderException mhe) { /* success */ }
    }
    /**
     * ContentLocation, 14.14.
     *
     */
    public void testContentLocation()                 throws Exception {
        String server = "http://www.xlattice.org/";
        header = new ContentLocationHeader(server);
        assertTrue (header instanceof EntityHeader);
        assertEquals ("Content-Location: " + server + "\n", header.toString());
        assertEquals ("Content-Location", header.getTag());
        assertTrue (((ContentLocationHeader)header).relativePermitted());
        assertEquals(server, ((ContentLocationHeader)header).getURI());
    }
    /**
     * ContentType, 14.17.
     * 
     */
    public void testContentType()                      throws Exception {
        String mimeType = "text/html";
        header = new ContentTypeHeader(mimeType);
        assertTrue (header instanceof EntityHeader);
        assertEquals ("Content-Type: " + mimeType + "\n", header.toString());
        assertEquals (mimeType, ((ContentTypeHeader)header).getValue());
        assertEquals ("Content-Type", header.getTag());
    }
    /**
     * Date, 14.18.
     */
    public void testDate()                      throws Exception {
        String time = "Tue, 15 Nov 1994 16:12:31 GMT";
        header = new DateHeader(time);
        assertTrue (header instanceof GeneralHeader);
        assertEquals("Date: " + time + "\n", header.toString());
        assertEquals("Date", header.getTag());

        // accept other timezones, convert to GMT
        String california = "Tue, 15 Nov 1994 08:12:31 PST";
        header = new DateHeader(california);
        assertEquals("Date: " + time + "\n", header.toString());
    }
    /**
     * ETag, 14.19. 
     *
     */
    public void testETag()                 throws Exception {
        String entityTag = "xyzzy";
        header = new ETagHeader(entityTag);
        assertTrue (header instanceof ResponseHeader);
        assertEquals ("ETag: " + entityTag + "\n", header.toString());
        assertEquals ("ETag", header.getTag());
        
        entityTag = "    xyzzy";
        header = new ETagHeader(entityTag);
        assertEquals ("ETag: " + entityTag + "\n", header.toString());
    }
    /**
     * Expect, 14.20. 
     *
     */
    public void testExpect()                 throws Exception {
        String expectation = "100-continue";
        header = new ExpectHeader(expectation);
        assertTrue (header instanceof RequestHeader);
        assertEquals ("Expect: " + expectation + "\n", header.toString());
        assertEquals ("Expect", header.getTag());
    }
    /**
     * Expires, 14.21.
     */
    public void testExpires()                      throws Exception {
        String time = "Tue, 15 Nov 1994 16:12:31 GMT";
        header = new ExpiresHeader(time);
        assertTrue (header instanceof EntityHeader);
        assertEquals("Expires: " + time + "\n", header.toString());
        assertEquals("Expires", header.getTag());
    }
    /**
     * From, 14.22. 
     *
     */
    public void testFrom()                 throws Exception {
        String emailAddr = "foxbatty@xlattice.org";
        header = new FromHeader(emailAddr);
        assertTrue (header instanceof RequestHeader);
        assertEquals ("From: " + emailAddr + "\n", header.toString());
        assertEquals ("From", header.getTag());
    }
    /**
     * Host, 14.23.
     * 
     * At least for now we don't check that the host name is 
     * well-formed.
     */
    public void testHost()                      throws Exception {
        String host = "www.xlattice.org";

        // no port number /////////////////////////////////
        header = new HostHeader(host);
        assertTrue (header instanceof RequestHeader);
        assertEquals ("Host: " + host + "\n", header.toString());
        assertEquals (80, ((HostHeader)header).getPort());
        assertEquals ("Host", header.getTag());

        // single argument including port number //////////
        try {
            header = new HostHeader(host + ":0");
            fail("didn't catch out of range port");
        } catch (MalformedHeaderException mhe) { /* success */ }

        header = new HostHeader(host + ":97");
        assertEquals (97, ((HostHeader)header).getPort());

        // two argument constructor ///////////////////////
        try {
            header = new HostHeader(host, 0);
            fail("didn't catch out of range port");
        } catch (MalformedHeaderException mhe) { /* success */ }
        header = new HostHeader(host, 97);
        assertEquals (97, ((HostHeader)header).getPort());
    }
    /**
     * If-Modified-Since, 14.25.
     */
    public void testIfModifiedSince()                      throws Exception {
        String time = "Tue, 15 Nov 1994 16:12:31 GMT";
        header = new IfModifiedSinceHeader(time);
        assertTrue (header instanceof RequestHeader);
        assertEquals("If-Modified-Since: " + time + "\n", header.toString());
        assertEquals("If-Modified-Since", header.getTag());
    }
    /**
     * If-Unmodified-Since, 14.28.
     */
    public void testIfUnmodifiedSince()                      throws Exception {
        String time = "Tue, 15 Nov 1994 16:12:31 GMT";
        header = new IfUnmodifiedSinceHeader(time);
        assertTrue (header instanceof RequestHeader);
        assertEquals("If-Unmodified-Since: " + time + "\n", header.toString());
        assertEquals("If-Unmodified-Since", header.getTag());
    }
    /**
     * LastModified, 14.29.
     */
    public void testLastModified()                      throws Exception {
        String time = "Tue, 15 Nov 1994 16:12:31 GMT";
        header = new LastModifiedHeader(time);
        assertTrue (header instanceof EntityHeader);
        assertEquals("Last-Modified: " + time + "\n", header.toString());
        assertEquals("Last-Modified", header.getTag());
    }
    /**
     * Location, 14.30.  Should be a single absolute URI.
     *
     */
    public void testLocation()                 throws Exception {
        String server = "http://www.xlattice.org/";
        header = new LocationHeader(server);
        assertTrue (header instanceof ResponseHeader);
        assertEquals ("Location: " + server + "\n", header.toString());
        assertEquals ("Location", header.getTag());
        assertFalse (((LocationHeader)header).relativePermitted());
        assertEquals(server, ((LocationHeader)header).getURI());
    }
    /**
     * MaxForwards, 14-31.
     */
    public void testMaxForwardsHeader()                 throws Exception {
        String count = "7";
        header = new MaxForwardsHeader(count);
        assertTrue (header instanceof RequestHeader);
        assertEquals("Max-Forwards: " + count + "\n", header.toString());
        assertEquals ("Max-Forwards", header.getTag());
        assertEquals(7, ((MaxForwardsHeader)header).getValue());

        header = new MaxForwardsHeader(13);
        assertEquals(13, ((MaxForwardsHeader)header).getValue());
    }
    /**
     * Pragma, 14.32. 
     *
     */
    public void testPragma()                 throws Exception {
        String directive = "no-cache";
        header = new PragmaHeader(directive);
        assertTrue (header instanceof GeneralHeader);
        assertEquals ("Pragma: " + directive + "\n", header.toString());
        assertEquals ("Pragma", header.getTag());
    }
    /**
     * Referer, 14.36. 
     *
     * XXX The value, the referrer, should be an absolute or 
     * XXX relative URI.  We don't check this.
     */
    public void testReferer()                 throws Exception {
        String referrer = "http://www.xlattice.org/index.html";
        header = new RefererHeader(referrer);
        assertTrue (header instanceof RequestHeader);
        assertEquals ("Referer: " + referrer + "\n", header.toString());
        assertEquals ("Referer", header.getTag());
        assertTrue (((RefererHeader)header).relativePermitted());
        assertEquals(referrer, ((RefererHeader)header).getURI());
    }
    /**
     * RetryAfter, 14.37.
     */
    public void testRetryAfter()                      throws Exception {
        String time = "Tue, 15 Nov 1994 16:12:31 GMT";
        header = new RetryAfterHeader(time);
        assertTrue (header instanceof ResponseHeader);
        assertEquals("Retry-After: " + time + "\n", header.toString());
        assertEquals("Retry-After", header.getTag());
    }
    /**
     * Server, 14.38. 
     *
     */
    public void testServer()                 throws Exception {
        String server = "CryptoServer/0.0.8";
        header = new ServerHeader(server);
        assertTrue (header instanceof ResponseHeader);
        assertEquals ("Server: " + server + "\n", header.toString());
        assertEquals ("Server", header.getTag());
    }
    /**
     * User-Agent, 14.43.  
     */
    public void testUserAgent()                 throws Exception {
        String agent = "CERN-LineMode/2.15 libwww/2.17b3";
        header = new UserAgentHeader(agent);
        assertTrue (header instanceof RequestHeader);
        assertEquals ("User-Agent: " + agent + "\n", header.toString());
        assertEquals ("User-Agent", header.getTag());
    }
    /**
     * Warning, 14.46. 
     *
     */
    public void testWarning()                 throws Exception {
        String warningValue = "no-cache";
        header = new WarningHeader(warningValue);
        assertTrue (header instanceof GeneralHeader);
        assertEquals ("Warning: " + warningValue + "\n", header.toString());
        assertEquals ("Warning", header.getTag());
    }
}
