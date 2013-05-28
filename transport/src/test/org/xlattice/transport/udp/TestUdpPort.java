/* TestUdpPort.java */
package org.xlattice.transport.udp;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Random;

import junit.framework.*;

import org.xlattice.*;
import org.xlattice.transport.*;
import org.xlattice.transport.udp.*;

/**
 * Tests of blocking I/O.
 * 
 * XXX So far this tests only unconnected I/O.  Also, UdpPort code
 * ignores position and limit in ByteBuffers.
 * 
 * @author Jim Dixon
 */
public class TestUdpPort extends TestCase {

    protected     Random      rng = new Random();

    private       UdpPort     udpPort1;
    private       UdpPort     udpPort2;
    
    private final InetAddress localHost;
    private       IPAddress   addr1;
    private       IPAddress   addr2;
    private       int port1;
    private       int port2;

    public TestUdpPort (String name)            throws Exception {
        super(name);
        localHost = InetAddress.getLocalHost(); 
    }

    public void setUp ()                        throws Exception {
    }
    public void tearDown()                      throws Exception {
        try {
            if (udpPort1 != null && !udpPort1.isClosed())
                udpPort1.close();
        } catch (IOException ioe) {};
        try {
            if (udpPort2 != null && !udpPort2.isClosed())
                udpPort2.close();
        } catch (IOException ioe) {};
    }
    public void testConstructor()               throws Exception {
        // Note that if localHost is null, it is construed as the 
        // wildcard address, 0.0.0.0/0
        try {
            udpPort1 = new UdpPort (localHost, -1);
            fail("UdpPort constructor accepted negative port number");
        } catch (IllegalArgumentException iae) { /* ok */ }
        
        udpPort1 = new UdpPort (localHost, 0);
        int portAssigned = udpPort1.getLocalPort();
        assertFalse ( portAssigned == 0 );
        udpPort1.close();
        assertTrue (udpPort1.isClosed());

        // XXX THIS TEST WILL OCCASIONALLY FAIL, because port 33333
        // will be in use.
        udpPort1 = new UdpPort (localHost, 33333);
       
        // properties /////////////////////////////////////
        assertTrue (localHost.equals( udpPort1.getLocalHost()));
        assertEquals(33333,  udpPort1.getLocalPort());
        assertEquals (0, udpPort1.getTimeout());

        /* STUB */

        udpPort1.close();
        
    }
    public void testUnconnectedIO ()            throws Exception {
        udpPort1 = new UdpPort (localHost);
        port1    = udpPort1.getLocalPort();
        addr1    = (IPAddress)udpPort1.getNearAddress();
        assertEquals (port1, addr1.getPort());
        udpPort1.setTimeout(50);
        assertEquals(50,    udpPort1.getTimeout());

        udpPort2 = new UdpPort(localHost);
        port2    = udpPort2.getLocalPort();
        addr2    = (IPAddress)udpPort2.getNearAddress();
        assertEquals (port2, addr2.getPort());
        udpPort2.setTimeout(50);
        assertEquals(50,    udpPort2.getTimeout());

        assertTrue( port1 != 0 );
        assertTrue( port2 != 0 );
        
        ByteBuffer inBuf1  = ByteBuffer.allocate(128);
        ByteBuffer inBuf2  = ByteBuffer.allocate(128);

        int len1    = 16 + rng.nextInt(129 - 16);   // 16 to 128 bytes
        byte[] out1 = new byte[len1];
        rng.nextBytes(out1);
        int len2    = 16 + rng.nextInt(129 - 16);   // 16 to 128 bytes
        byte[] out2 = new byte[len2];
        rng.nextBytes(out2);
        
        // sets position to 0, limit = capacity = out1.length 
        ByteBuffer outBuf1 = ByteBuffer.wrap(out1);
        ByteBuffer outBuf2 = ByteBuffer.wrap(out2);

        udpPort1.sendTo(outBuf1, addr2);
        udpPort2.sendTo(outBuf2, addr1);

        IPAddress from1 = (IPAddress)udpPort1.receiveFrom( inBuf1 );
        IPAddress from2 = (IPAddress)udpPort2.receiveFrom( inBuf2 );

        assertEquals( len2, inBuf1.limit() );
        assertEquals( len1, inBuf2.limit() );

        for (int i = 0; i < len2; i++) 
            assertEquals( out2[i], inBuf1.array()[i] );
        for (int i = 0; i < len1; i++) 
            assertEquals( out1[i], inBuf2.array()[i] );

        udpPort1.close();
        udpPort2.close();
    }
    public void testConnectedIO ()              throws Exception {
    }
}
