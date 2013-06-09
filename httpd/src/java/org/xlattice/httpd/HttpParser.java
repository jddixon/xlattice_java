/* HttpParser.java */
package org.xlattice.httpd;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jim Dixon
 */

import org.xlattice.httpd.headers.*;
import org.xlattice.transport.SchedulableConnection;
import org.xlattice.util.NonBlockingLog;
import org.xlattice.util.StringLib;

/**
 */
public abstract class HttpParser {

    // XXX THERE MAY BE PROBLEMS IF THIS EXCEEDS ////////////////////
    // XXX     transport/tcpip/SchedulableTcpConnection.CNX_BUFSIZE,
    // XXX currently 64KB ///////////////////////////////////////////
    public final static int HTTP_BUFSIZE   = 65536;

    public final static int HTTP_GET       = 0;
    public final static int HTTP_HEAD      = 1;
    public final static int HTTP_POST      = 2;
    public final static int HTTP_PUT       = 3;
    public final static int HTTP_DELETE    = 4;
    public final static int HTTP_TRACE     = 5;
    public final static int HTTP_CONNECT   = 6;
    public final static int HTTP_OPTIONS   = 7;
    public final static int HTTP_EXTENSION = 8;

    public final static String[] METHODS = {
        "GET", "HEAD",   "POST",
        "PUT", "DELETE", "TRACE", "CONNECT", "OPTIONS"
    };
    public final static Map methodMap = new HashMap(7);
    static {
        methodMap.put("GET",     new Integer(HTTP_GET));
        methodMap.put("HEAD",    new Integer(HTTP_HEAD));
        methodMap.put("POST",    new Integer(HTTP_POST));
        methodMap.put("PUT",     new Integer(HTTP_PUT));
        methodMap.put("DELETE",  new Integer(HTTP_DELETE));
        methodMap.put("TRACE",   new Integer(HTTP_TRACE));
        methodMap.put("CONNECT", new Integer(HTTP_CONNECT));
        methodMap.put("OPTIONS", new Integer(HTTP_OPTIONS));
    }
    // STATUS LINES /////////////////////////////////////////////////
    // version SP code SP reasonPhrase eol
    
    // HTTP/1.0 status lines //////////////////////////////
    // 302 has different reason phrase in 1.0
    public final static String V1_0_200 = "HTTP/1.0 200 OK\n";
    public final static String V1_0_201 = "HTTP/1.0 201 Created\n";
    public final static String V1_0_202 = "HTTP/1.0 202 Accepted\n";
    public final static String V1_0_204 = "HTTP/1.0 204 No Content\n";
    
    public final static String V1_0_301 = "HTTP/1.0 301 Moved Permanently\n";
    public final static String V1_0_302 = "HTTP/1.0 302 Moved Temporarily\n";
    public final static String V1_0_304 = "HTTP/1.0 304 Not Modified\n";
    
    public final static String V1_0_400 = "HTTP/1.0 400 Bad Request\n";
    public final static String V1_0_401 = "HTTP/1.0 401 Unauthorized\n";
    public final static String V1_0_403 = "HTTP/1.0 403 Forbidden\n";
    public final static String V1_0_404 = "HTTP/1.0 404 Not Found\n";
    
    public final static String V1_0_500 = "HTTP/1.0 500 Internal Server Error\n";
    public final static String V1_0_501 = "HTTP/1.0 501 Not Implemented\n";
    public final static String V1_0_502 = "HTTP/1.0 502 Bad Gateway\n";
    public final static String V1_0_503 = "HTTP/1.0 503 Service Unavailable\n";

    // HTTP/1.1 status lines //////////////////////////////
    public final static String V1_1_100 = "HTTP/1.1 100 Continue\n";
    public final static String V1_1_101 = "HTTP/1.1 101 Switching Protocols\n";
    public final static String V1_1_200 = "HTTP/1.1 200 OK\n";
    public final static String V1_1_201 = "HTTP/1.1 201 Created\n";
    public final static String V1_1_202 = "HTTP/1.1 202 Accepted\n";
    public final static String V1_1_203 = "HTTP/1.1 203 Non-Authoritative Information\n";
    public final static String V1_1_204 = "HTTP/1.1 204 No Content\n";
    public final static String V1_1_205 = "HTTP/1.1 205 Reset Content\n";
    public final static String V1_1_206 = "HTTP/1.1 206 Partial Content\n";
    public final static String V1_1_300 = "HTTP/1.1 300 Multiple Choices\n";
    public final static String V1_1_301 = "HTTP/1.1 301 Moved Permanently\n";
    public final static String V1_1_302 = "HTTP/1.1 302 Found\n";
    public final static String V1_1_303 = "HTTP/1.1 303 See Other\n";
    public final static String V1_1_304 = "HTTP/1.1 304 Not Modified\n";
    public final static String V1_1_305 = "HTTP/1.1 305 Use Proxy\n";
    // 306 not in standard
    public final static String V1_1_307 = "HTTP/1.1 307 Temporary Redirect\n";
    public final static String V1_1_400 = "HTTP/1.1 400 Bad Request\n";
    public final static String V1_1_401 = "HTTP/1.1 401 Unauthorized\n";
    public final static String V1_1_402 = "HTTP/1.1 402 Payment Required\n";
    public final static String V1_1_403 = "HTTP/1.1 403 Forbidden\n";
    public final static String V1_1_404 = "HTTP/1.1 404 Not Found\n";
    public final static String V1_1_405 = "HTTP/1.1 405 Method Not Allowed\n";
    public final static String V1_1_406 = "HTTP/1.1 406 Not Acceptable\n";
    public final static String V1_1_407 = "HTTP/1.1 407 Proxy Authentication Required\n";
    public final static String V1_1_408 = "HTTP/1.1 408 Request Time-out\n";
    public final static String V1_1_409 = "HTTP/1.1 409 Conflict\n";
    public final static String V1_1_410 = "HTTP/1.1 410 Gone\n";
    public final static String V1_1_411 = "HTTP/1.1 411 Length Required\n";
    public final static String V1_1_412 = "HTTP/1.1 412 Precondition Failed\n";
    public final static String V1_1_413 = "HTTP/1.1 413 Request Entity Too Large\n";
    public final static String V1_1_414 = "HTTP/1.1 414 Request-URI Too Large\n";
    public final static String V1_1_415 = "HTTP/1.1 415 Unsupported Media Type\n";
    public final static String V1_1_416 = "HTTP/1.1 416 Requested range not satisfiable\n";
    public final static String V1_1_417 = "HTTP/1.1 417 Expectation Failed\n";
    public final static String V1_1_500 = "HTTP/1.1 500 Internal Server Error\n";
    public final static String V1_1_501 = "HTTP/1.1 501 Not Implemented\n";
    public final static String V1_1_502 = "HTTP/1.1 502 Bad Gateway\n";
    public final static String V1_1_503 = "HTTP/1.1 503 Service Unavailable\n";
    public final static String V1_1_504 = "HTTP/1.1 504 Gateway Time-out\n";
    public final static String V1_1_505 = "HTTP/1.1 505 Http Version not supported\n";

    public final static int BAD_HTTP_VERSION = -1;
    public final static int V0_9 =  0;
    public final static int V1_0 =  1;
    public final static int V1_1 =  2;
    public final static String[] VERSIONS = {
                                "HTTP/0.9", "HTTP/1.0", "HTTP/1.1"};
    
    /** parser states */
    public final static int BAD_STATE    = 0;
    public final static int START_HEAD   = 1;
    public final static int IN_HEAD      = 2;
    public final static int START_ENTITY = 3;
    public final static int IN_ENTITY    = 4;
    public final static int END_ENTITY   = 5;
    public final static int ABORT_PARSE  = 6;
   
    public final static String[] STATES = {
        "BAD_STATE", "START_HEAD", "IN_HEAD", "START_ENTITY", 
        "IN_ENTITY", "END_ENTITY", "ABORT_PARSE"};

    /** global instance counter */
    protected    static int counter;

    // PRIVATE MEMBERS //////////////////////////////////////////////
    /** index of this instance */
    protected       int index;
    protected       int parserState = BAD_STATE;

    protected       ByteBuffer wrappedHeaderSection;
    protected       ByteBuffer wrappedEntity;
    
    protected       NonBlockingLog debugLog;
    protected       NonBlockingLog errorLog;

    /** time of instantiation */
    protected final HttpDate now;
    /** HTTP version of message being parsed */
    protected       int version = BAD_HTTP_VERSION;
    /** -1 or value in Content-Length header if seen */
    protected       int contentLength = -1;
    /** null or name in Host header if seen */
    protected       String host;
    /** 80 or value in Host header, if specified */
    protected       int hostPort = 80;

    /** null or reference to object created if parsing request */
    protected       HttpRequest  request;
    /** null or reference to object created if parsing response */
    protected       HttpResponse response;

    protected       SchedulableConnection cnx;
    protected       SocketChannel sChan;
    protected       ByteBuffer cnxInBuf;
    protected       ByteBuffer dataIn;      // accumulates from cnxInBuf
    protected       ByteBuffer dataOut;

    // CONSTRUCTORS /////////////////////////////////////////////////
    /**
     * @param eLog name of file to log error messages to; null means don't log
     * @param dLog name of file to log debug messages to, or null
     */
    public HttpParser (String eLog, String dLog) {
        now = new HttpDate();
        setDebugLog(dLog);
        setErrorLog(eLog);
    }
    // LOGGING //////////////////////////////////////////////////////
    /** 
     * Set the name of the file debug messages are logged to.  If
     * this is null, no logging is done.
     *
     * @param name path to log file from current directory
     */
    protected final void setDebugLog (String name) {
        if (debugLog != null)
            throw new IllegalStateException("can't change debug log name");
        if (name != null)
            debugLog   = NonBlockingLog.getInstance(name);
    }
    /** 
     * Set the name of the file error messages are logged to.  If
     * this is null, no logging is done.
     *
     * @param name path to log file from current directory
     */
    protected final void setErrorLog (String name) {
        if (errorLog != null)
            throw new IllegalStateException("can't change error log name");
        if (name != null)
            errorLog   = NonBlockingLog.getInstance(name);
    }
    
    /**
     * Subclasses should override.
     */
    protected void DEBUG_MSG(String msg) {
        if (debugLog != null)
            debugLog.message("HttpParser" + msg);
    }
    protected void ERROR_MSG(String msg) {
        if (errorLog != null)
            errorLog.message("HttpParser" + msg);
    }
    // PROPERTIES ///////////////////////////////////////////////////
    public int getContentLength() {
        return contentLength;
    }
    public String getHost () {
        return host;
    }
    public int getHostPort() {
        return hostPort;
    }
    /**
     * @return the request generated or received
     */
    public HttpRequest getRequest() {
        return request;
    }
    /**
     * @return the response generated or received
     */
    public HttpResponse getResponse () {
        return response;
    }
    /**
     * Returns the HTTP version, 0 meaning HTTP/0.9, 1 meaning HTTP/1.0,
     * and 2 meaning HTTP/1.1. 
     *
     * @return the HTTP version of the message being parsed
     */
    public int getVersion() {
        return version;
    }

    // UTILITY METHODS //////////////////////////////////////////////
    /** Oh for an unsigned char... */
    protected static char castByte(byte b) {
        // 0x7f is 127, 0x80 (-128) is 128, 0xff (-1) is 255
        return b >= 0 ? (char)b : (char)(0xff & b);
    }
    /**
     * Collect the entity from the ByteBuffer, given its length in
     * contentLength.  The entity is returned in the HttpMessage.
     * The buffer position will be advanced by contentLength bytes
     * if successful.
     *
     * @param message HttpMessage which we are constructing
     * @param inBuf    ByteBuffer being parsed
     */
    protected void collectEntity (HttpMessage message, ByteBuffer inBuf) 
                                            throws MessageFormatException {
        DEBUG_MSG (".collectEntity: content length = " + contentLength);
        if (contentLength >0) {
            if (inBuf.remaining() >= contentLength) {
                byte[] entity = new byte[contentLength];
                //inBuf.flip();               // change 2005-03-08
                DEBUG_MSG(".collectEntity before inBuf.get(entity)\n"
                        + "    contentLength = " + contentLength
                        + "    inBuf.limit   = " + inBuf.limit()
                      + "\n    inBuf starts with " + firstTen(inBuf.array())
                );
                inBuf.get(entity);
                DEBUG_MSG(".collectEntity after inBuf.get(entity)");
                message.setEntity(entity);
                DEBUG_MSG (".collectEntity:\n    collected: content length is " 
                     + contentLength + "\n    begins with " 
                     + firstTen(entity) );
            } else {
                throw new MessageFormatException ("entity should be "
                        + contentLength + " bytes long but only "
                        + inBuf.remaining() + " bytes available");
            }
        }
    }  
    /**
     * Enter with the cursor (offset) positioned just beyond the end
     * of the first line of an HTTP message.  If we are not on an end
     * of line, we should be at the beginning of a colon-delimited header 
     * tag.  Get the tag, skip the colon and space(s), get the value,
     * skip the end of line, and loop.  Continue until the blank line
     * is found or the end of the buffer.  Leave the cursor positioned
     * on the blank line marking the end of the header section.
     *
     * @param msg HttpMessage being constructed
     * @param buf ByteBuffer whose contents are being parsed
     */
    protected void collectHeaders(HttpMessage msg, ByteBuffer buf)
                                            throws MessageFormatException {
            if (msg == null || buf == null)
                throw new IllegalArgumentException(
                        "null HttpMessage or ByteBuffer");
            byte[] b = buf.array();
            int offset = buf.position();
            String tag = null;
            while ( !isEOL(b[offset]) && offset < buf.limit() ) {
                int start  = offset;
                offset     = skipToColon(b, start);
                tag        = new String(b, start, offset - start);
                DEBUG_MSG(" getting value for " + tag);

                offset      = skipW(b, ++offset);     // skip the colon first
                // sometimes the value part is missing
                String value;
                if (isEOL(b[offset])) {
                    DEBUG_MSG(tag + " header has no value");
                    value = " ";
                } else {
                    start  = offset;
                    offset = skipToEOL(b, offset);
                    // 2005-06-20: this causes server to crash if 
                    // the parameters are out of range.  We have been
                    // getting "java.lang.StringIndexOutOfBoundsException: 
                    //   String index out of range: -53" messages
                    // and crash after hacking attempts
                    if (start < 0 || offset < start) {
                        String errMsg = 
                            "error in parameters after skipToEOL: "
                            + "\n\tstart  = " + start
                            + "\n\toffset = " + offset;
                        DEBUG_MSG(errMsg);
                        throw new MessageFormatException(errMsg);
                    }
                    value  = new String(b, start, offset - start);
                }
                // XXX COULD PREPARSE value here
                Header header = Header.makeHeader(tag, value);
                msg.addHeader(header);
                if (tag.equals("Content-Length")) {
                    contentLength = ((ContentLengthHeader)header).getValue();
                    DEBUG_MSG(".collectHeaders, contentLength = " 
                            + contentLength);
                } else if (tag.equals("Host")) {
                    HostHeader hh = (HostHeader) header;
                    host     = hh.getHost();
                    hostPort = hh.getPort();
                }
                // we skipped to an EOL, so there's one here
                offset = skipEOL (b, offset);   
            }
            if (offset >= buf.limit())
                throw new MessageFormatException("unexpected end of buffer");
            buf.position(offset);
    }
    protected static void expect(char c, byte b) 
                                            throws MessageFormatException {
        if ( castByte(b) != c)
            throw new MessageFormatException(
                    "expected " + c + ", found " + castByte(b) );
    }
    protected static int expect(String s, byte[] b, int offset) 
                                            throws MessageFormatException {
        int len = s.length();
        for (int i = 0; i < len; i++)
            expect(s.charAt(i), b[i + offset]);
        return offset + len;
    }

    protected static int expectDigit (byte b) 
                                            throws MessageFormatException {
        char c = castByte(b);
        if ( c < '0' || c > '9')
            throw new MessageFormatException("expected digit, found " + c);
        return c - '0';
    }
    protected static int expect3Digits(byte [] b, int offset)
                                            throws MessageFormatException {
        if (offset + 3 > b.length)
            throw new MessageFormatException(
                "buffer too short for 3-digit code");
        int hi  = expectDigit(b[offset++]);
        int mid = expectDigit(b[offset++]);
        int low = expectDigit(b[offset++]);
        return hi * 100 + mid * 10 + low;
    }
    // DEBUG
    protected static String firstTen(byte[] b){
        if (b == null)
            return "<null>";
        else
            return StringLib.byteArrayToHex(b, 0,
                    b.length > 10? 10 : b.length);
    }
    // END
    /**
     * Construing b as a hex character, convert it to a char value.
     *
     * @throws IllegalArgumentException if not 0-9 or A-F
     */
    protected static char hex2val (byte b) {
        char c = castByte(b);
        char value;
        if (c >= '0' && c <='9')
            return (char)(c - '0');
        else if (c >= 'A' && c <= 'F')
            return (char)((c - 'A') + 10);
        else
            throw new IllegalArgumentException("invalid hex character");
    }
    /**
     *
     */
    protected static String httpDecode (byte[] b) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            char c = castByte(b[i]);
            if (c == '%') {
                if (i + 2 >= b.length)
                    break;              // corrupt; don't try to fix
                else {
                    char c0 = hex2val(b[++i]);
                    char c1 = hex2val(b[++i]);
                    sb.append ((char)((c0  * 16) + c1));
                }
            } else
                sb.append(c);
        }
        return sb.toString();
    }
    private final static char[] hexChar = new char[] {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
    protected final static String encodeChar(char c) {
        if (c >= 256)
            throw new IllegalArgumentException(
                    "cannot encode character with value 256 or above");
        return new StringBuffer("%")
                .append( hexChar[c / 16]  )
                .append( hexChar[c & 0xf] )
                .toString();
    }
    /**
     * The intricacies of Unicode/UTF-8 are ignored.  Some sources
     * claim that quite a few more characters need to be encoded.
     * These include:
     *   A{1,3,4,5,7}, C{4,5}, E{0,4,5}, C{6,7,9}, E{8,9,C},
     *   D{1,6,8,C,F}, F{1,2,6,9,C}
     * For now, these are ignored too.
     */
    protected static String httpEncode (String s) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if ( c <= 0x20 || c == 0x22   || c == 0x23 || c == 0x25
              || c == 0x26 || c == 0x2C   || c == 0x2E || c == 0x2F
              || (c >= 0x3A && c <= 0x3F) || c == 0x40
              || (c >= 0x5B && c <= 0x5E)
              || (c >= 0x7B && c <= 0x7E)
            )
                sb.append ( encodeChar(c) );
            else
                sb.append (c);
        }
        return sb.toString();
    }
    protected static boolean isEOL (byte b) {
        char c = castByte(b);
        return c == '\r' || c == '\n';
    }
    protected static boolean isEOL (ByteBuffer buf) {
       return isEOL(buf.array()[buf.position()]);
    }
    /**
     * Expect to be positioned on an end of line marker, throwing
     * an exception if not.
     *
     * Skip the end of line marker (any of CR, LF, CRLF), returning
     * the offset after this operation.  If the EOL is at the end of
     * the buffer, the value returned will be the length of the
     * buffer.
     */
    protected static int skipEOL (byte[] b, int offset) 
                                            throws MessageFormatException {
        char c = castByte(b[offset++]);
        if (c == '\n')
            return offset;
        else if (c != '\r')
            throw new MessageFormatException ("expected EOL");
        
        if (offset >= b.length)
            return offset;
        c = castByte(b[offset]);
        if (c == '\n')
            return ++offset;
        else
            return offset;
    }
    protected static void skipEOL (ByteBuffer buf) 
                                            throws MessageFormatException {
        buf.position( skipEOL(buf.array(), buf.position()) );
    }
    protected static int skipToColon(byte[] b, int offset)
                                            throws MessageFormatException {
        char c = castByte(b[offset++]);
        if (c == ':')
            throw new MessageFormatException ("already at colon");
        for (int i = offset; i < b.length; i++) {
            c = castByte(b[i]);
            if (c == ':')
                return i;
            if (c == '\r' | c == '\n')
                break;
        }
        throw new MessageFormatException("colon not found");
    }
    /**
     * Expect to be positioned on a non-EOL character; throw an 
     * error if the current byte is an EOL.
     * 
     * Skip to the first EOL character, returning its offset or -1
     * if not found.
     */
    protected static int skipToEOL (byte[] b, int offset) 
                                            throws MessageFormatException {
        char c = castByte(b[offset++]);
        if (c == '\r' || c == '\n')
            throw new MessageFormatException ("already at EOL");
        for (int i = offset; i < b.length; i++) {
            c = castByte(b[i]);
            if (c == '\r' || c == '\n')
                return i;
        }
        return -1;
    }
    /**
     * Expect to be positioned on a byte which is neither tab nor
     * space; throw an exception if you are.
     * 
     * Skip to the first blank or tab, returning its offset. 
     */
    protected static int skipToW (byte[] b, int offset) 
                                            throws MessageFormatException {
        char c = castByte(b[offset++]);
        if (c == ' ' || c == '\t')
            throw new MessageFormatException(
                    "expected NOT to be on whitespace");
        for (int i = offset; i < b.length; i++) {
            c = castByte(b[i]);
            if (c == ' ' || c == '\t')
                return i;
        }
        throw new MessageFormatException("no whitespace found");
    }
    /**
     * Expect to be positioned on a byte which is not whitespace
     * (tab, space, CR, LF here); throw an exception if you are.
     *
     * Skip to the first whitespace and return its offset; throw
     * an exception if you can't find one.
     */
    protected static int skipToWorEOL (byte[] b, int offset) 
                                            throws MessageFormatException {
        char c = castByte(b[offset++]);
        if (c == ' ' || c == '\t' || c == '\r' || c == '\n')
            throw new MessageFormatException(
                    "expected NOT to be on whitespace");
        for (int i = offset; i < b.length; i++) {
            c = castByte(b[i]);
            if (c == ' ' || c == '\t' || c == '\r' || c == '\n')
                return i;
        }
        throw new MessageFormatException("no whitespace found");
    }


    /**
     * Skip to the first byte neither a blank or tab, returning its offset 
     * or -1 if not found.
     *
     * @throws MessageFormatException if first byte not whitespace
     */
    protected static int skipW(byte[]b, int offset) 
                                            throws MessageFormatException {
        char c = castByte(b[offset++]);
        if ( c != ' ' && c != '\t')
            throw new MessageFormatException("expected white space");
        
        for (int i = offset; i < b.length; i++) {
            c = castByte(b[i]);
            if ( c != ' ' && c != '\t')
                return i;
        }
        return -1;
    }
    /**
     * Given a status code, and knowing the HTTP version number,
     * returns the complete status line in String form.  There is
     * at least one anomaly: because there is no HTTP/1.0 300 status
     * code, unrecognized 300 series status codes get turned into 
     * the HTTP/1.1 300 status code.  Only the pedantic will care ;-)
     */
    protected final static String statusLine (int version, int code) 
                                    throws IllegalParserStateException {
        if (version == V1_0) {
            switch (code) {
                case 200: return V1_0_200;
                case 201: return V1_0_201;
                case 202: return V1_0_202;
                case 204: return V1_0_204;
                          
                case 301: return V1_0_301;
                case 302: return V1_0_302;
                case 304: return V1_0_304;
                
                case 400: return V1_0_400;
                case 401: return V1_0_401;
                case 403: return V1_0_403;
                case 404: return V1_0_404;
                
                case 500: return V1_0_500;
                case 501: return V1_0_501;
                case 502: return V1_0_502;
                case 503: return V1_0_503;
                default:
                    if (code < 200 || code >=600) {
                        throw new IllegalParserStateException (
                            "invalid status code " + code);
                    }
                    switch (code/100) {
                        case 2: return V1_0_200;
                        case 3: return V1_1_300;   // no HTTP/1.0 300
                        case 4: return V1_0_400;
                        case 5: return V1_0_500;
                        default: 
                            throw new IllegalParserStateException(
                                    "internal error: unreachable state");
                    }
            }
        } else if (version == V1_1) {
            switch (code) {
                case 100: return V1_1_100;
                case 101: return V1_1_101;
                case 200: return V1_1_200;
                case 201: return V1_1_201;
                case 202: return V1_1_202;
                case 203: return V1_1_203;
                case 204: return V1_1_204;
                case 205: return V1_1_205;
                case 206: return V1_1_206;
                case 300: return V1_1_300;
                case 301: return V1_1_301;
                case 303: return V1_1_303;
                case 304: return V1_1_304;
                case 305: return V1_1_305;
                case 307: return V1_1_307;
                case 400: return V1_1_400;
                case 401: return V1_1_401;
                case 402: return V1_1_402;
                case 403: return V1_1_403;
                case 404: return V1_1_404;
                case 405: return V1_1_405;
                case 406: return V1_1_406;
                case 407: return V1_1_407;
                case 408: return V1_1_408;
                case 409: return V1_1_409;
                case 410: return V1_1_410;
                case 411: return V1_1_411;
                case 412: return V1_1_412;
                case 413: return V1_1_413;
                case 414: return V1_1_414;
                case 415: return V1_1_415;
                case 416: return V1_1_416;
                case 417: return V1_1_417;
                case 500: return V1_1_500;
                case 501: return V1_1_501;
                case 502: return V1_1_502;
                case 503: return V1_1_503;
                case 504: return V1_1_504;
                case 505: return V1_1_505;
                default:
                    if (code < 100 || code >=600) {
                        throw new IllegalParserStateException (
                            "invalid status code " + code);
                    }
                    switch (code/100) {
                        case 1: return V1_1_100;
                        case 2: return V1_1_200;
                        case 3: return V1_1_300;
                        case 4: return V1_1_400;
                        case 5: return V1_1_500;
                        default: 
                            throw new IllegalParserStateException(
                                    "internal error: unreachable state");
                    }
            } 
        } else {
            throw new IllegalParserStateException("invalid HTTP version");
        }
    } 
}
