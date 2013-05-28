/* TestMockSocket.java */
package org.xlattice.transport.mockery;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Random;

/**
 * @author Jim Dixon
 **/

import junit.framework.*;

public class TestMockSocket extends TestCase {

    Random rng = new Random ( new Date().getTime() );
   
    Socket socket;
    InetAddress loopback;
    InetAddress remoteHost;
    InetAddress anyLocalAddress;
   
    InetSocketAddress thisEnd = new InetSocketAddress(loopback, 1234);
    InetSocketAddress farEnd  = new InetSocketAddress(remoteHost, 5678);

    ByteBufferInputStream ins;
    ByteBufferOutputStream outs;
    
    public TestMockSocket (String name) {
        super(name);
    }

    public void setUp () {
        socket = null;
        try {
            loopback        = InetAddress.getByName("127.0.0.1");
            remoteHost      = InetAddress.getByName("1.2.3.4");
            anyLocalAddress = InetAddress.getByName("0.0.0.0");
        
        } catch (UnknownHostException uhe) { /* assume it won't happen */ }
        ins  = null;
        outs = null;
    }
    public void tearDown() {
        if (ins != null)
            try {
                ins.close();
            } catch (Exception e) { /* ignore */ }
        if (outs != null)
            try {
                outs.close();
            } catch (Exception e) { /* ignore */ }
    }
    
    public void testConstructor ()              throws Exception {
        socket = new MockSocket();
        assertNotNull (socket);
        assertNull (socket.getInputStream());
        assertNull (socket.getOutputStream());
        
        assertEquals (anyLocalAddress.toString(),
                      socket.getLocalAddress().toString());
        assertEquals (-1, socket.getLocalPort());   // INCONSISTENT; yes, -1
        assertNull (socket.getLocalSocketAddress());

        assertNull (socket.getInetAddress());
        assertEquals (0, socket.getPort());         // INCONSISTENT; yes, 0
        assertNull (socket.getRemoteSocketAddress());

        assertNull (socket.getChannel());
    }
    
    // test transition from each state to its successor, and some
    // illegal transitions
    public void testStates ()                   throws Exception {
        MockSocket mock = new MockSocket();
        assertEquals (MockSocket.UNBOUND, mock.getState());
        try {
            mock.connect(farEnd);
            fail("succeeded in connecting unbound socket!");
        } catch (IOException ioe) { /* success */ }
        try {
            mock.close();
            fail("succeeded in closing unbound socket!");
        } catch (IOException ioe) { /* success */ }

        mock.bind(thisEnd);
        assertEquals (MockSocket.BOUND, mock.getState());
        
        mock.connect(farEnd);
        assertEquals (MockSocket.CONNECTED, mock.getState());
        try {
            mock.connect(farEnd);
            fail("succeeded in connecting connected socket!");
        } catch (IOException ioe) { /* success */ }
        
        mock.close();
        assertEquals (MockSocket.DISCONNECTED, mock.getState());
        try {
            mock.close();
            fail("succeeded in closing closed socket!");
        } catch (IOException ioe) { /* success */ }
    }
    
    public void testStreams()                   throws Exception {
        MockSocket mock = new MockSocket();
        ins  = new ByteBufferInputStream();
        outs = new ByteBufferOutputStream();
        mock.setStreams (ins, outs);
        assertTrue (ins  == mock.getInputStream());
        assertTrue (outs == mock.getOutputStream());
        try {
            mock.setStreams (ins, outs);
            fail("second setStreams() call should not succeed");
        } catch (Exception e) { /* ignore */ }
        
        mock = new MockSocket();
        try {
            mock.setStreams(null, null);
            fail("null setStream() parameter should not succeed");
        } catch (Exception e) { /* success */ }
        
        mock.setStreams(ins, null);
        assertTrue (ins == mock.getInputStream());
        ByteBufferOutputStream outStream 
                = (ByteBufferOutputStream)mock.getOutputStream();
        assertNotNull(outStream);
        ByteBuffer outBuf = outStream.getBuffer();
        assertEquals (ByteBufferOutputStream.DEFAULT_BUFSIZE,
                      outBuf.capacity());
    }

    // There is very little here other than what is already tested
    // in TestByteBufferInputStream
    public void testReading()                   throws Exception {
       
        byte[] data = new byte[128];
        rng.nextBytes(data);
        ByteBuffer wrappedData = ByteBuffer.wrap(data);
        
        MockSocket mock = new MockSocket();
        ins  = new ByteBufferInputStream(wrappedData);
        outs = new ByteBufferOutputStream();
        mock.setStreams (ins, outs);
        mock.bind(thisEnd);
        mock.connect(farEnd);
        // XXX the user can get and use the I/O streams even though 
        // XXX the socket is not connected; need some sort of callback
        // XXX to report socket state
        InputStream  inFromSock  = mock.getInputStream();
        byte data0 = (byte)inFromSock.read();
        byte data1 = (byte)inFromSock.read();
        byte[] more = new byte[256];
        int count = inFromSock.read (more, 0, 256);
        assertEquals (126, count);
        assertEquals (data[0], data0);
        assertEquals (data[1], data1);
        for (int i = 0; i < 126; i++)
            assertEquals (more[i], data[i + 2]);

        mock.close();
    }
    public void testWriting()                   throws Exception {
        byte[] data = new byte[128];
        rng.nextBytes(data);
        
        MockSocket mock = new MockSocket();
        ins  = new ByteBufferInputStream();
        outs = new ByteBufferOutputStream();
        mock.setStreams (ins, outs);
        mock.bind(thisEnd);
        mock.connect(farEnd);
        OutputStream outToSock = mock.getOutputStream();

        outToSock.write(data[0]);
        outToSock.write(data,  1, 15);
        outToSock.write(data, 16, 48);
        outToSock.write(data, 64,  1);
        outToSock.write(data, 65, 63);

        assertTrue (outToSock == outs);

        byte[] array = outs.getBuffer().array();
        
        for (int i = 0; i < 128; i++)
            assertEquals (data[i], array[i]);

        mock.close();
    }
}
