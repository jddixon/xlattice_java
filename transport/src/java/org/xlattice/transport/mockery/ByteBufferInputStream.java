/* ByteBufferInputStream.java */
package org.xlattice.transport.mockery;

import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author Jim Dixon
 **/

public class ByteBufferInputStream extends InputStream {

    public final static int DEFAULT_BUFSIZE = 1024;

    private ByteBuffer buffer;
    private boolean closed;
    
    // CONSTRUCTORS /////////////////////////////////////////////////
    public ByteBufferInputStream() {
        this(DEFAULT_BUFSIZE);
    }
    public ByteBufferInputStream(int n) {
        buffer = ByteBuffer.allocate(n);
    }
    public ByteBufferInputStream(ByteBuffer buf) {
        if (buf == null)
            throw new IllegalArgumentException("null buffer");
        if (!buf.hasArray())
            throw new IllegalArgumentException(
                    "buffer must have backing array");
        buffer = buf;
    }
    // INTERFACE InputStream ////////////////////////////////////////
    public int available() {
        return buffer.limit() - buffer.position();
    } 
    public void close () {
        closed = true;
    }
    /**
     * Mark the current position, allowing at least readlimit bytes
     * to be read before the mark is invalidated.
     *
     * XXX The parameter is currently ignored
     * 
     * @param readlimit max number of bytes that can be read before mark
     *                      is invalidated
     */
    public void mark (int readlimit) {
        buffer.mark();
    }
    /** @return whether mark() and reset() are supported */
    public boolean markSupported() {
        return true;
    }
    public void reset ()                        throws IOException {
        try {
            buffer.reset();
        } catch (java.nio.InvalidMarkException ime) {
            throw new IOException ("no mark set - " + ime);
        }
    }
    
    public int read() {
        try {
            return buffer.get();
        } catch (java.nio.BufferOverflowException boe) {
            return -1;
        } 
    }
    public int read(byte[] b) {
        return read(b, 0, b.length);
    }
    /**
     * @param b      byte array data is read into
     * @param offset starting position in that byte array
     * @param len    maximum number of bytes to transfer
     */
    public int read(byte[] b, int offset, int len) {
        if (b == null)
            throw new NullPointerException();
        int posNow = buffer.position();
        int limit  = buffer.limit();
        if (posNow >= limit)
            return -1;
        if (len == 0)
            return 0;
        if (offset < 0 || len < 0 || (offset + len) > b.length)
            throw new IndexOutOfBoundsException();
        
        if (posNow + len > limit) 
            len = limit - posNow;
        buffer.get(b, offset, len);
        return len;
            
    }
    public long skip (long n) {
        if (n < 0)
            throw new IllegalArgumentException (
                    "negative skips not supported");
        int intN = (int) n;
        if ( intN + buffer.position() > buffer.limit() ) {
            int posNow = buffer.position();
            buffer.position(buffer.limit());
            return buffer.limit() - posNow;
        } else {
            buffer.position( buffer.position() + intN );
            return intN;
        }
    }
    // OTHER METHODS ////////////////////////////////////////////////
    public ByteBuffer getBuffer() {
        return buffer;
    }
}
