/* TestIPv4Address.java */
package org.xlattice.transport;

import java.net.*;

/**
 * @author Jim Dixon
 **/

import junit.framework.*;

import org.xlattice.AddressException;

public class TestIPv4Address extends TestCase {

    private IPv4Address v4Addr, v4Addr2;
    
    public TestIPv4Address (String name) {
        super(name);
    }

    public void setUp () {
        v4Addr  = null;
        v4Addr2 = null;
    }

    public void testBadAddr()               throws Exception {
        try {
            v4Addr = new IPv4Address("1.2.3.4", -92);
            fail ("didn't catch out of range port number -92");
        } catch (IllegalArgumentException e) { /* success */ }
        try {
            v4Addr = new IPv4Address("1.2.3.4", 65536);
            fail ("didn't catch out of range port number 2^16");
        } catch (IllegalArgumentException e) { /* success */ }
        try {
            v4Addr = new IPv4Address("1.2.3.4.5", 5);
            fail ("didn't catch 1.2.3.4.5");
        } catch (AddressException e) { /* success */ }

        assertFalse(IPv4Address.isValidAddress(null));
        assertFalse(IPv4Address.isValidAddress(new byte[] {0} ));
        assertFalse(IPv4Address.isValidAddress(new byte[] {0, 0} ));
        assertFalse(IPv4Address.isValidAddress(new byte[] {0, 0, 0} ));
        assertTrue (IPv4Address.isValidAddress(new byte[] {0, 0, 0, 0} ));
        assertFalse(IPv4Address.isValidAddress(new byte[] {0, 0, 0, 0, 0} ));
    }

    private byte[] a1234 = new byte[] { 1, 2, 3, 4 };
    private byte[] a1235 = new byte[] { 1, 2, 3, 5 };
    
    public void testV4AddrWithPort()        throws Exception {
        v4Addr = new IPv4Address(a1234, 97);
        assertNotNull(v4Addr);
        assertEquals (97, v4Addr.getPort());
        InetAddress iaddr = v4Addr.getInetAddress();
        byte[] byteAddr   = v4Addr.getIPAddress();
        assertEquals(4, byteAddr.length);
        byte[] addrFromIA = iaddr.getAddress();
        assertEquals(4, addrFromIA.length);
        for (int i = 0; i < 4; i++)
            assertEquals(byteAddr[i], addrFromIA[i]);
        v4Addr2 = new IPv4Address(byteAddr, 97);
        assertNotNull(v4Addr2);
        assertTrue ( v4Addr.equals(v4Addr2) );
        assertEquals ("1.2.3.4:97", v4Addr2.toString());
    }
    public void testEquals()                throws Exception {
        v4Addr  = new IPv4Address(a1234, 52);
        assertTrue ( v4Addr.equals(v4Addr) );

        v4Addr2 = new IPv4Address(a1235, 52);   // different IP
        assertFalse ( v4Addr.equals(null) );
        assertFalse ( v4Addr.equals(v4Addr2) );
        v4Addr2 = new IPv4Address(a1234, 53);   // different port
        assertFalse ( v4Addr.equals(v4Addr2) );
        v4Addr2 = new IPv4Address(a1234, 52);   // same IP and port
        assertTrue  ( v4Addr.equals(v4Addr2) );
    }
    public void testPrivateIPs()            throws Exception {
        // Web server running in 10/8
        v4Addr   = new IPv4Address("10.0.0.1:80");
        byte[] b = v4Addr.getIPAddress();
        assertEquals(10, b[0]);
        assertEquals( 0, b[1]);
        assertEquals( 0, b[2]);
        assertEquals( 1, b[3]);
        assertTrue (IPv4Address.isPrivate(b));
        assertEquals (80, v4Addr.getPort());
        
        // Web server running in 128.0/16
        v4Addr = new IPv4Address("128.0.12.121:8080");
        b      = v4Addr.getIPAddress();
        assertEquals(-128, b[0]);
        assertEquals(   0, b[1]);
        assertEquals(  12, b[2]);
        assertEquals( 121, b[3]);
        assertTrue (IPv4Address.isPrivate(b));
        assertEquals (8080, v4Addr.getPort());
        
        b = new IPv4Address("127.255.0.4:8080").getIPAddress();
        assertFalse (IPv4Address.isPrivate(b));
        b = new IPv4Address("128.1.0.4:8080").getIPAddress();
        assertFalse (IPv4Address.isPrivate(b));
        
        // Web server running in 172.16/12
        v4Addr = new IPv4Address("172.30.0.1:443");
        b      = v4Addr.getIPAddress();
        assertEquals(-84, b[0]);
        assertEquals( 30, b[1]);
        assertEquals(  0, b[2]);
        assertEquals(  1, b[3]);
        assertTrue (IPv4Address.isPrivate(b));
        assertEquals (443, v4Addr.getPort());
        
        b = new IPv4Address("172.15.0.1:443").getIPAddress();
        assertFalse (IPv4Address.isPrivate(b));
        b = new IPv4Address("172.32.0.1:443").getIPAddress();
        assertFalse (IPv4Address.isPrivate(b));
    }
    byte [] test = new byte[] {0, 0, 0, 1};     // gets modified
    public void testRFC3330()               throws Exception {
        // 0/8
        assertTrue (IPv4Address.isRFC3330(test));
        assertTrue (IPv4Address.isRFC3330notPrivate(test));
        // 14/8
        test[0] = 14;
        assertTrue (IPv4Address.isRFC3330(test));
        // 24/8
        test[0] = 24;
        assertTrue (IPv4Address.isRFC3330(test));
        // 127/0
        test[0] = 127;
        assertTrue (IPv4Address.isRFC3330(test));
        
        // 169.254/16, link local
        test = new IPv4Address("169.254.0.1:443").getIPAddress();
        assertEquals(-87, test[0]);
        assertEquals( -2, test[1]);
        assertTrue (IPv4Address.isRFC3330(test));
        assertFalse (IPv4Address.isPrivate(test));

        // 192.0.2.0/24, test net
        test = new IPv4Address("192.0.2.14:443").getIPAddress();
        assertEquals(-64, test[0]);
        assertTrue (IPv4Address.isRFC3330(test));
        assertFalse (IPv4Address.isPrivate(test));

        // 192.88.99.0/24, 6to4 relay anycast
        test = new IPv4Address("192.88.99.14:443").getIPAddress();
        assertEquals(-64, test[0]);
        assertTrue (IPv4Address.isRFC3330(test));

        // 198.18/15, benchmark testing
        test = new IPv4Address("198.18.99.14:443").getIPAddress();
        assertEquals(-58, test[0]);
        assertTrue (IPv4Address.isRFC3330(test));
        test = new IPv4Address("198.19.99.14:443").getIPAddress();
        assertTrue (IPv4Address.isRFC3330(test));

        // 224/4, multicast
        test = new IPv4Address("224.18.99.14:443").getIPAddress();
        assertEquals(-32, test[0]);
        assertTrue (IPv4Address.isRFC3330(test));
        
        // 240/4, reserved
        v4Addr = new IPv4Address("240.18.99.14:443");
        test = v4Addr.getIPAddress();
        assertEquals(-16, test[0]);
        assertTrue (IPv4Address.isRFC3330(test));
        assertEquals ("240.18.99.14:443", v4Addr.toString());
    }
    public void testConstructorWithHostName ()  throws Exception {
        v4Addr = new IPv4Address("www.xlattice.org", 80);
        test   = v4Addr.getIPAddress();
        // known to be globally routable ;-)
        assertFalse (IPv4Address.isRFC3330(test));
    }
}
