/* HttpRequest.java */
package org.xlattice.httpd;

import org.xlattice.util.ArrayStack;

/**
 * If the version is 0.9, an HTTP request consists only of a 
 * single line without a version number.
 *
 * Otherwise the request consists of 
 *   a request line, which specifies the version number
 *   zero or more headers
 *   a blank line
 *   and optionally an entity body
 *
 * @author Jim Dixon
 */
public class HttpRequest                    extends HttpMessage {

    final int method;
    final String uri;
    private   final ArrayStack requestHeaders;
   
    // CONSTRUCTORS /////////////////////////////////////////////////
    /** 
     * Constructor with sufficient information to build the request
     * line.  Headers and entity may be added later.
     *
     * @param method  index of method
     * @param uri     for now, absolute path (so must begin with /)
     * @param version index into HTTP versions (0.9 = 0 and so forth)
     */
    public HttpRequest (int method, String uri, int version) 
                throws MessageFormatException, NotImplementedException {
        super(version);
        if (method != HttpParser.HTTP_GET 
         && method != HttpParser.HTTP_HEAD)
            throw new NotImplementedException("not implemented: " 
                    + HttpParser.METHODS[method]);
        this.method = method;
        if (uri == null || uri.equals(""))
            throw new MessageFormatException(
                    "null or empty URI");
        if (uri.charAt(0) != '/')
            throw new MessageFormatException(
                    "first character of URI is not forward slash: "
                    + uri);
        this.uri = uri;

        requestHeaders = new ArrayStack();
    }
    // PROPERTIES ///////////////////////////////////////////////////
    public int getMethod() {
        return method;
    }
    public String getURI() {
        return uri;
    }

    private void addHeader (RequestHeader header)
                                        throws MessageFormatException {
        if (header == null)
            throw new IllegalArgumentException("attempt to add null header");
        requestHeaders.push(header);
    }
    public void addHeader (Header header) 
                                        throws MessageFormatException {
        if (header instanceof GeneralHeader)
            addHeader ((GeneralHeader) header);
        else if (header instanceof RequestHeader)
            addHeader ((RequestHeader) header);
        else if (header instanceof EntityHeader)
            addHeader ((EntityHeader) header);
        else
            throw new MessageFormatException("unexpected header type");
    }
    public RequestHeader getRequestHeader(int n) {
        return (RequestHeader)requestHeaders.peek(n);
    }
    public int sizeRequestHeaders() {
        return requestHeaders.size();
    }
    
    // SERIALIZATION ////////////////////////////////////////////////
    /**
     * If the version if 0.9, this returns only the simple request line
     * in canonical form; that is, with any sequence of spaces reduced
     * to a single space, and any of (CR, LF, CRLF) reduced to a single
     * CRLF.
     *
     * Otherwise this returns 
     *   the full request line, which includes the version number,
     *     in canonical form
     *   zero or more headers (first general, then request, then entity)
     *   a blank line terminated by CRLF
     * The entity body is not returned in this String.
     *
     * XXX The URI needs to be %XX-encoded during serialization. RFC 2396.
     */
    public String toString() {
        // request line /////////////////////////
        StringBuffer sb = new StringBuffer( HttpParser.METHODS[method] )
            .append(" ")
            .append(uri); 
        if (httpVersion <= HttpParser.V0_9) {
            return sb.append("\n").toString();
        }
        sb.append(" HTTP/1.");
        if (httpVersion == HttpParser.V1_1)
            sb.append("1\n");
        else if (httpVersion == HttpParser.V1_0)
            sb.append("0\n");
        else 
            throw new IllegalStateException("unsupported version number");

        // headers //////////////////////////////
        for (int i = 0; i < sizeGeneralHeaders(); i++)
            sb.append( getGeneralHeader(i).toString() );
        for (int i = 0; i < sizeRequestHeaders(); i++)
            sb.append( getRequestHeader(i).toString() );
        for (int i = 0; i < sizeEntityHeaders(); i++)
            sb.append( getEntityHeader(i).toString() );
                   
        // blank line ///////////////////////////
        return sb.append("\n").toString();
    }
}
