/* TestKadID.java */
package org.xlattice.protocol.xlkad;

import java.util.Random;

import junit.framework.*;

import org.xlattice.util.StringLib;

/**
 * @author Jim Dixon
 */

public class TestKadID extends TestCase {

    private Random rng = new Random();
    private KadID  myID;
    private KadID  otherID;
    
    public TestKadID (String name) {
        super(name);
    }

    public void setUp () {
        myID    = null;
        otherID = null;
    }

    public void printHex(byte[] data) {
        System.out.println(StringLib.byteArrayToHex(data, 0, data.length));
    }
    public void testConstructors()              throws Exception {
    }

    public void testDistance()                  throws Exception {
        byte[] id1 = new byte[ KadID.LENGTH ];
        rng.nextBytes(id1);
        KadID myID1 = new KadID(id1);
        byte[] id2 = new byte[ KadID.LENGTH ];
        rng.nextBytes(id2);
        KadID myID2 = new KadID(id2);
        byte[] xor = new byte[ KadID.LENGTH ];
        for (int i = 0; i < KadID.LENGTH; i++) 
            xor[i] = (byte) (id1[i] ^ id2[i]);
        KadID d = myID1.distance(myID2);
        for (int i = 0; i < KadID.LENGTH; i++)
            assertEquals( xor[i], d.value()[i] );
    }
    public byte flipABit (byte x, int n) {
        byte bitmask = (byte)(1 << n);
        if ( (bitmask & x) != 0 ) {
            // bit is set, so clear it
            return (byte)(x & ~bitmask);
        } else {
            // bit is clear, so set it
            return (byte)(x | bitmask);
        }
    }
    public void testLogDistance()               throws Exception {
        byte[] val = new byte[ KadID.LENGTH ];     // or 160 bits
        rng.nextBytes(val);
        myID   = new KadID(val);
        
        byte[] other = new byte[ KadID.LENGTH ];
        for (int k = 0; k < KadID.LENGTH; k++)
            other[k] = val[k];
        
        for (int i = 0; i < KadID.LENGTH; i++) {
            if (i > 0)
                other[i - 1] = val[i - 1];
            
            for (int j = 0; j < 7; j++) {
                other [i] = flipABit ( val[i], 7 - j);
                // value is cloned and so immutable
                otherID = new KadID(other);
                assertEquals ("byte " + i + ", bit " + j ,
                        159 - ((i * 8) + j), myID.logDistance(otherID) );
            }
        }
    }
}
