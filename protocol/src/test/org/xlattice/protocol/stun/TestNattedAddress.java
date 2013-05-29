/* TestNattedAddress.java */
package org.xlattice.protocol.stun;

import java.net.Inet4Address;

import junit.framework.*;

/**
 * @author Jim Dixon
 */

public class TestNattedAddress extends TestCase {

    private final Inet4Address hostA;
    private final Inet4Address hostB; 
    private final Inet4Address hostC; 
    private final Inet4Address hostD;
    private final int portA =  96;
    private final int portB =  98;
    private final int portC = 100;
    private final int portD = 102;
    
    NattedAddress nattedW;
    NattedAddress nattedX;
    NattedAddress nattedY;
    NattedAddress nattedZ;
    
    // CONSTRUCTOR //////////////////////////////////////////////////
    public TestNattedAddress (String name)      throws Exception{
        super(name);
        hostA = (Inet4Address)Inet4Address.getByName("1.2.3.4");
        hostB = (Inet4Address)Inet4Address.getByName("1.2.3.5");
        hostC = (Inet4Address)Inet4Address.getByName("1.2.3.6");
        hostD    // address is unchanged
              = (Inet4Address)Inet4Address.getByName("1.2.3.6");
    }
    // SETUP/TEARDOWN ///////////////////////////////////////////////
    public void setUp () {
        nattedW = null;
        nattedX = null;
        nattedY = null;
        nattedZ = null;
    }
    // UNIT TESTS ///////////////////////////////////////////////////
    public void testConstructors()              throws Exception {
        try {
            nattedX = new NattedAddress(null, 0);
            fail ("constructor didn't catch null local host");
        } catch (IllegalArgumentException iae) { /* success */ }
        try {
            nattedX = new NattedAddress(hostA, portA, null, 0);
            fail ("constructor didn't catch null mapped host");
        } catch (IllegalArgumentException iae) { /* success */ }
    }
    public void testEquals ()                   throws Exception {
        nattedW = new NattedAddress(hostA, portA);
        nattedX = new NattedAddress(hostA, portA, hostB, portB);
        nattedY = new NattedAddress(hostC, portC);
        nattedZ = new NattedAddress(hostD, portD);
        
        assertTrue(  nattedW.equals(nattedW) );
        assertFalse( nattedW.equals(nattedX) );
        assertFalse( nattedW.equals(nattedY) );
        assertFalse( nattedY.equals(nattedZ) );     // only ports differ
    }
    public void testGettersAndSetters()         throws Exception {
        nattedW = new NattedAddress(hostA, portA);
        assertTrue ( hostA.equals( nattedW.getLocalHost() ) );
        assertEquals(portA, nattedW.getLocalPort() );
        assertNull ( nattedW.getMappedHost() );
            
        nattedX = new NattedAddress(hostA, portA, hostB, portB);
        assertTrue ( hostB.equals( nattedX.getMappedHost() ) );
        assertEquals(portB, nattedX.getMappedPort() );
        
        nattedY = new NattedAddress(hostC, portC);
        nattedZ = new NattedAddress(hostD, portD);
        try {
            nattedX.setLocalHost(null);
            fail("successfully changed local host to null!");
        } catch (IllegalArgumentException iad) { /* true success */ }
        
        assertFalse( nattedW.equals(nattedX) );
        assertFalse( nattedW.hashCode() == nattedX.hashCode() );
        nattedW.setMappedHost(hostB);
        nattedW.setMappedPort(portB);
        assertTrue ( nattedW.equals(nattedX) );
        assertTrue ( nattedW.hashCode() == nattedX.hashCode() );
        
    }     
}
