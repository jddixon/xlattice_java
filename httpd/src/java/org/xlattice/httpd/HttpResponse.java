/* HttpResponse.java */
package org.xlattice.httpd;

import org.xlattice.util.ArrayStack;

/**
 * If the version is 0.9, an HTTP response consists only of an
 * entity.
 *
 * Otherwise, the serialized response consists of
 *   a status line
 *   zero or more headers (general, response, and entity, serialized
 *     in that order)
 *   a blank line
 *   and optionally an entity body
 *
 * Only the status line is here.  The other elements are in the
 * superclass.
 *
 * @author Jim Dixon
 */
public class HttpResponse                       extends HttpMessage {

    private int    code;            // three digit code
    private String responsePhrase;

    private String statusLine;

    private   final ArrayStack responseHeaders;
    // CONSTRUCTORS /////////////////////////////////////////////////
    /**
     * Constructor for a v0.9 response.
     */
    public HttpResponse()               throws MessageFormatException {
        super (HttpParser.V0_9);
        responseHeaders = null;
    }
    /**
     * Constructs a response from parsed input.
     */
    public HttpResponse (int version, int code, String responsePhrase)
                                        throws MessageFormatException {
        super(version);

        if (code < 100 || code >= 600)
            throw new MessageFormatException ("invalid status code");
        this.code = code;

        if (responsePhrase == null || responsePhrase.equals(""))
            throw new MessageFormatException (
                    "null or empty response phrase");

        responseHeaders = new ArrayStack();
    }
    /**
     * Begins construction of a response from what must be a
     * correctly formatted status line.
     */
    protected HttpResponse (String statusLine)
                                        throws MessageFormatException {
        super ( HttpParser.V1_0 + (statusLine.charAt(7) - '0') );
        this.statusLine = statusLine;
        responseHeaders = new ArrayStack();
    }


    // PROPERTIES ///////////////////////////////////////////////////
    private void addHeader (ResponseHeader header) {
        if (header == null)
            throw new IllegalArgumentException("attempt to add null header");
        responseHeaders.push(header);
    }
    public void addHeader (Header header)
                                        throws MessageFormatException {
        if (header instanceof GeneralHeader)
            addHeader ((GeneralHeader) header);
        else if (header instanceof ResponseHeader)
            addHeader ((ResponseHeader) header);
        else if (header instanceof EntityHeader)
            addHeader ((EntityHeader) header);
        else
            throw new MessageFormatException("unexpected header type");
    }
    public ResponseHeader getResponseHeader(int n) {
        return (ResponseHeader)responseHeaders.peek(n);
    }
    public int sizeResponseHeaders() {
        return responseHeaders.size();
    }

    private String myStatusLine() {
        String s = null;
        try {
            s = HttpParser.statusLine(httpVersion, code);
        } catch (IllegalParserStateException ipse) {
            // should be impossible
            s = "CAN'T GET STATUS LINE FOR "
                + httpVersion + ", " + code + " !!";
        }
        return s;
    }

    public String getStatusLine() {
        if (statusLine == null) {
            statusLine = myStatusLine();
        }
        return statusLine;
    }
    // PARSER METHODS ///////////////////////////////////////////////

    // SERIALIZATION ////////////////////////////////////////////////
    /**
     * If the version is 0.9, returns null.
     *
     * For other versions, returns the status line in canonical
     * form, followed by any general headers, then any response
     * headers, then any entity headers, then a blank line.
     */
    public String toString() {
        if (httpVersion <= HttpParser.V0_9)
            return null;
        if (statusLine == null)
            statusLine = myStatusLine();
        StringBuffer sb = new StringBuffer(statusLine);
        for (int i = 0; i < sizeGeneralHeaders(); i++)
            sb.append( getGeneralHeader(i).toString() );
        for (int i = 0; i < sizeResponseHeaders(); i++)
            sb.append( getResponseHeader(i).toString() );
        for (int i = 0; i < sizeEntityHeaders(); i++)
            sb.append( getEntityHeader(i).toString() );
        return sb.append("\n").toString();
    }
}
