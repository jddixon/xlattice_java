/* ByteBufferOutputStream.java */
package org.xlattice.transport.mockery;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

/**
 * @author Jim Dixon
 **/

public class ByteBufferOutputStream extends OutputStream {

    public static final int DEFAULT_BUFSIZE = 1024;
    
    private boolean closed;
    private final ByteBuffer buffer;
   
    // CONSTRUCTORS /////////////////////////////////////////////////
    public ByteBufferOutputStream() {
        this(DEFAULT_BUFSIZE);
    }
    public ByteBufferOutputStream(int n) {
        buffer = ByteBuffer.allocate(n);
    }

    public ByteBufferOutputStream(ByteBuffer b) {
        if (b == null)
            throw new IllegalArgumentException ("null buffer");
        if (!b.hasArray())
            throw new IllegalArgumentException(
                    "buffer must have backing array");
        // Javadocs say this is public, compiler disagrees :-(
        //if (b.isReadOnly)
        //    throw new IllegalArgumentException ("read-only buffer");
        buffer = b;
    }

    // INTERFACE OutputStream ///////////////////////////////////////
    private void checkWhetherClosed ()          throws IOException {
        if (closed)
            throw new IOException("file is closed");
    }
    public void close () {
        closed = true;
    }
    public void flush ()                        throws IOException {
        checkWhetherClosed();
        /* otherwise do nothing */
    }
    
    private void writeBeyond()                  throws IOException {
        throw new IOException("write beyond end of buffer");
    }
    /**
     * Write a single byte to the output buffer.    The parameter is
     * actually an int, which will be truncated to a byte.
     * 
     * @param b byte to be written to buffer
     */
    public void write(int b)                    throws IOException {
        checkWhetherClosed();
        try {
            buffer.put((byte)b);
        } catch (BufferOverflowException boe) {
            writeBeyond();
        }
    }
    public void write(byte[] b)                 throws IOException {
        write(b, 0, b.length);
    }
    public void write(byte[] b, int offset, int len) 
                                                throws IOException {
        checkWhetherClosed();
        if (b == null)
            throw new NullPointerException();
        try {
            buffer.put(b, offset, len);
        } catch (BufferOverflowException boe) {
            writeBeyond();
        }

    }
    // OTHER METHODS ////////////////////////////////////////////////
    /** Clear the underlying ByteBuffer. */
    public void clear() {
        buffer.clear();
    }
    /** @return a reference to the underlying ByteBuffer */
    public ByteBuffer getBuffer() {
        return buffer;
    }
}
