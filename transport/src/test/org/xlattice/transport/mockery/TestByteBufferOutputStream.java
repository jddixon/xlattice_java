/* TestByteBufferOutputStream.java */
package org.xlattice.transport.mockery;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Random;

/**
 * @author Jim Dixon
 **/

import junit.framework.*;

public class TestByteBufferOutputStream extends TestCase {

    Random rng = new Random ( new Date().getTime() );
    
    ByteBufferOutputStream buffer;
    
    public TestByteBufferOutputStream (String name) {
        super(name);
    }

    public void setUp () {
        buffer = null;
    }
    public void tearDown() {
    }
    public void testConstructor ()              throws Exception {
        buffer = new ByteBufferOutputStream();
        assertNotNull(buffer);
        ByteBuffer backingBuf = buffer.getBuffer();
        assertNotNull(backingBuf);
        assertEquals( ByteBufferOutputStream.DEFAULT_BUFSIZE,
                backingBuf.array().length );
    }
    public void testWrites ()                   throws Exception {
        buffer = new ByteBufferOutputStream();
        assertNotNull(buffer);
        ByteBuffer backingBuf = buffer.getBuffer();
        byte[] array = backingBuf.array();
        byte[] data  = new byte[20];
        rng.nextBytes(data);
        buffer.write(data[0]);
        assertEquals ( data[0], array[0] );
        buffer.write(data[1]);
        assertEquals ( data[1], array[1] );
        buffer.write(data);
        for (int i = 0; i < data.length; i++)
            assertEquals (data[i], array[i + 2]);
    }
    public void testExceptions ()               throws Exception {
        buffer = new ByteBufferOutputStream();
        buffer.close();
        try {
            buffer.flush();
            fail("flushed closed stream without generating error");
        } catch (IOException ioe) { /* expected */ }
        try {
            buffer.write(1);
            fail("wrote to closed stream without generating error");
        } catch (IOException ioe) { /* expected */ }

        buffer = new ByteBufferOutputStream (128);
        byte[] data = new byte[132];
        rng.nextBytes(data);
        try {
            buffer.write(data);
            fail("wrote beyond end of buffer without generating error");
        } catch (IOException ioe) { /* expected */ }
    }
}
