/* HttpMessage.java */
package org.xlattice.httpd;

import org.xlattice.util.ArrayStack;

/**
 *
 * @author Jim Dixon
 */
public abstract class HttpMessage {

    protected final int        httpVersion;
    private   final ArrayStack generalHeaders;
    private   final ArrayStack entityHeaders;

    private int      contentLength = -1;
    private byte []  entity;

    private int      index = -1;            // DEBUG

    protected HttpMessage (int version) 
                                    throws MessageFormatException {
        if (version < HttpParser.V0_9 || version > HttpParser.V1_1)
            throw new MessageFormatException(
                    "invalid HTTP version");
        httpVersion = version;
        
        generalHeaders = new ArrayStack();
        entityHeaders  = new ArrayStack();
    }
   
    // PROPERTIES ///////////////////////////////////////////////////
    /**
     * @return the entity associated with the HTTP message
     */
    public byte[] getEntity () {
        return entity;
    }
    /**
     * Sets the entity associated with the request.  If there is no
     * such entity, the parameter may be null.
     *
     * @param b entity being passed to the request
     */
    public void setEntity (byte[] b) {
        entity = b;
    }
    protected void addHeader (GeneralHeader header) {
        if (header == null)
            throw new IllegalArgumentException("attempt to add null header");
        generalHeaders.push(header);
    }
    protected void addHeader (EntityHeader header) {
        if (header == null)
            throw new IllegalArgumentException("attempt to add null header");
        entityHeaders.push(header);
    }
    public abstract void addHeader (Header header)
                                        throws MessageFormatException;

    public GeneralHeader getGeneralHeader(int n) {
        return (GeneralHeader)generalHeaders.peek(n);
    }
    public EntityHeader getEntityHeader(int n) {
        return (EntityHeader)entityHeaders.peek(n);
    }
    public int sizeGeneralHeaders() {
        return generalHeaders.size();
    }
    public int sizeEntityHeaders() {
        return entityHeaders.size();
    }
    public int getHttpVersion() {
        return httpVersion;
    }
    public int getIndex() {
        return index;
    }
    public void setIndex(int n) {
        index = n;
    }
    // SERIALIZATION ////////////////////////////////////////////////
    /**
     * Returns the serialization of the message for the HTTP version.
     */
    public abstract String toString(); 
    
    /**
     */
    public byte[] getByteArray() {
        return toString().getBytes();
    }
}
