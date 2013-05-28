/* TestByteBufferInputStream.java */
package org.xlattice.transport.mockery;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Random;

/**
 * @author Jim Dixon
 **/

import junit.framework.*;

public class TestByteBufferInputStream extends TestCase {

    public final static int BUFSIZE = ByteBufferInputStream.DEFAULT_BUFSIZE;
    
    Random rng = new Random ( new Date().getTime() );
    
    ByteBufferInputStream ins;
    ByteBuffer buffer;              // underlies ins
    byte[] array;                   // underlies buffer

    public TestByteBufferInputStream (String name) {
        super(name);
    }

    public void setUp () {
        ins    = null;
        buffer = null;
        array  = null;
    }
    public void tearDown() {
    }
    public void testConstructor ()              throws Exception {
        ins = new ByteBufferInputStream();
        assertNotNull(ins);
        assertEquals(BUFSIZE, ins.available());
        ins.skip(BUFSIZE);
        assertEquals(0, ins.available());
    }

    public void testMark ()                     throws Exception {
        ins = new ByteBufferInputStream();
        buffer = ins.getBuffer();
        assertNotNull(buffer);
        assertTrue (ins.markSupported());
        ins.skip(16);
        ins.mark(512);
        ins.skip(32);
        assertEquals(48, buffer.position());
        ins.reset();
        assertEquals(16, buffer.position());
    }
    public void testReads()                     throws Exception {
        byte[] data = new byte[128];
        rng.nextBytes(data);            // fill array with random junk
        ByteBuffer myBuf = ByteBuffer.allocate(data.length * 2);
        myBuf.put(data);
        myBuf.flip();
        ins    = new ByteBufferInputStream(myBuf);
        buffer = ins.getBuffer();
        array  = buffer.array();

        assertTrue (buffer == myBuf);
        assertEquals ( data.length * 2, array.length);

        byte data0 = (byte) ins.read();
        assertEquals (data[0], data0);
        assertEquals (1, buffer.position());
        
        byte [] threeSome = new byte[3];
        int count = (byte) ins.read(threeSome, 0, 0);
        assertEquals (0, count);
        assertEquals (1, buffer.position());
        count = ins.read(threeSome, 0, 3);
        assertEquals(3, count);
        assertEquals(4, buffer.position());
        for (int i = 0; i < 3; i++) 
            assertEquals (threeSome[i], data[i + 1]);

        byte[] big = new byte[1024];
        count = ins.read(big, 0, 1024);
        assertEquals(128 - 4, count);
        assertEquals(128, buffer.position());
        for (int i = 0; i < 124; i++)
            assertEquals (big[i], data[i + 4]);
    }
}
