/* Header.java */
package org.xlattice.httpd;

import java.util.HashMap;
import java.util.Map;
import org.xlattice.httpd.headers.*;

/**
 * @author Jim Dixon
 */

public abstract class Header            implements AbstractHeader {

    private static Map tagMap = new HashMap(47);
    static {
        tagMap.put("Accept",                new Integer(1));
        tagMap.put("Accept-Charset",        new Integer(2));
        tagMap.put("Accept-Encoding",       new Integer(3));
        tagMap.put("Accept-Language",       new Integer(4));
        tagMap.put("Accept-Ranges",         new Integer(5));
        tagMap.put("Age",                   new Integer(6));
        tagMap.put("Allow",                 new Integer(7));
        tagMap.put("Authorization",         new Integer(8));
        tagMap.put("Cache-Control",         new Integer(9));
        tagMap.put("Connection",            new Integer(10));
        tagMap.put("Content-Encoding",      new Integer(11));
        tagMap.put("Content-Language",      new Integer(12));
        tagMap.put("Content-Length",        new Integer(13));
        tagMap.put("Content-Location",      new Integer(14));
        tagMap.put("Content-MD5",           new Integer(15));
        tagMap.put("Content-Range",         new Integer(16));
        tagMap.put("Content-Type",          new Integer(17));
        tagMap.put("Date",                  new Integer(18));
        tagMap.put("ETag",                  new Integer(19));
        tagMap.put("Expect",                new Integer(20));
        tagMap.put("Expires",               new Integer(21));
        tagMap.put("From",                  new Integer(22));
        tagMap.put("Host",                  new Integer(23));
        tagMap.put("If-Match",              new Integer(24));
        tagMap.put("If-Modified-Since",     new Integer(25));
        tagMap.put("If-None-Match",         new Integer(26));
        tagMap.put("If-Range",              new Integer(27));
        tagMap.put("If-Unmodified-Since",   new Integer(28));
        tagMap.put("Last-Modified",         new Integer(29));
        tagMap.put("Location",              new Integer(30));
        tagMap.put("Max-Forwards",          new Integer(31));
        tagMap.put("Pragma",                new Integer(32));
        tagMap.put("Proxy-Authenticate",    new Integer(33));
        tagMap.put("Proxy-Authorization",   new Integer(34));
        tagMap.put("Range",                 new Integer(35));
        tagMap.put("Referer",               new Integer(36));
        tagMap.put("Retry-After",           new Integer(37));
        tagMap.put("Server",                new Integer(38));
        tagMap.put("TE",                    new Integer(39));
        tagMap.put("Trailer",               new Integer(40));
        tagMap.put("Transfer-Encoding",     new Integer(41));
        tagMap.put("Upgrade",               new Integer(42));
        tagMap.put("User-Agent",            new Integer(43));
        tagMap.put("Vary",                  new Integer(44));
        tagMap.put("Via",                   new Integer(45));
        tagMap.put("Warning",               new Integer(46));
        tagMap.put("WWW-Authenticate",      new Integer(47));
    }
    public Header() {};
   
    // FACTORY METHOD ///////////////////////////////////////////////
    // XXX Can't handle structured values like "hostName:portNumber"
    public static Header makeHeader(String tag, String value) 
                                        throws MalformedHeaderException {
        Integer which = (Integer)tagMap.get(tag);
        if (which == null)
                        return new ExtensionHeader(tag, value);
        switch (which.intValue()) {
            case 1: 	return new AcceptHeader(value);
            case 2: 	return new AcceptCharsetHeader(value);
            case 3: 	return new AcceptEncodingHeader(value);
            case 4: 	return new AcceptLanguageHeader(value);
//          case 5: 	return new AcceptRangesHeader(value);
            case 6: 	return new AgeHeader(value);
//          case 7: 	return new AllowHeader(value);
//          case 8: 	return new AuthorizationHeader(value);
//          case 9: 	return new CacheControlHeader(value);
            case 10: 	return new ConnectionHeader(value);
            case 11: 	return new ContentEncodingHeader(value);
            case 12: 	return new ContentLanguageHeader(value);
            case 13: 	return new ContentLengthHeader(value);
            case 14: 	return new ContentLocationHeader(value);
//          case 15: 	return new ContentMD5Header(value);
//          case 16: 	return new ContentRangeHeader(value);
            case 17: 	return new ContentTypeHeader(value);
            case 18: 	return new DateHeader(value);
            case 19: 	return new ETagHeader(value);
            case 20: 	return new ExpectHeader(value);
            case 21: 	return new ExpiresHeader(value);
            case 22: 	return new FromHeader(value);
            case 23: 	return new HostHeader(value);
//          case 24: 	return new IfMatchHeader(value);
            case 25: 	return new IfModifiedSinceHeader(value);
//          case 26: 	return new IfNoneMatchHeader(value);
//          case 27: 	return new IfRangeHeader(value);
            case 28: 	return new IfUnmodifiedSinceHeader(value);
            case 29: 	return new LastModifiedHeader(value);
            case 30: 	return new LocationHeader(value);
            case 31: 	return new MaxForwardsHeader(value);
            case 32: 	return new PragmaHeader(value);
//          case 33: 	return new ProxyAuthenticateHeader(value);
//          case 34: 	return new ProxyAuthorizationHeader(value);
//          case 35: 	return new RangeHeader(value);
            case 36: 	return new RefererHeader(value);
            case 37: 	return new RetryAfterHeader(value);
            case 38: 	return new ServerHeader(value);
//          case 39: 	return new TEHeader(value);
//          case 40: 	return new TrailerHeader(value);
//          case 41: 	return new TransferEncodingHeader(value);
//          case 42: 	return new UpgradeHeader(value);
            case 43: 	return new UserAgentHeader(value);
//          case 44: 	return new VaryHeader(value);
//          case 45: 	return new ViaHeader(value);
            case 46: 	return new WarningHeader(value);
//          case 47: 	return new WWWAuthenticateHeader(value);
            default:    return new ExtensionHeader(tag, value);
        }
    }
    // INTERFACE AbstractHeader /////////////////////////////////////
    /**
     * Returns the name of the tag in canonical form: first
     * character and other word-starting characters in upper
     * case, others in lower case, in the same form as RFC 2616.
     */
    abstract public String getTag();
    
    /** 
     * The header in String format, including a terminating CR.
     */
    abstract public String toString();

    /**
     * The header as a byte array, including the terminating CR.
     */
    public byte[] toByteArray() {
        return toString().getBytes();
    }
}
