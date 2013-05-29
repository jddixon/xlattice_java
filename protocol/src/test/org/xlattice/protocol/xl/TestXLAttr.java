/* TestXLAttr.java */
package org.xlattice.protocol.xl;

import java.util.Random;

import junit.framework.*;

import org.xlattice.NodeID;
import org.xlattice.protocol.TLV;
import org.xlattice.protocol.TLV16;
import org.xlattice.util.StringLib;

/**
 * @author Jim Dixon
 */

public class TestXLAttr extends TestCase implements XLConst {

    private Random rng = new Random();

    private XLAttr attr;
    
    public TestXLAttr (String name) {
        super(name);
    }

    public void setUp () {
        attr = null;
    }

    /**
     * This tests Source, Destination, NodeIDAttr, and ValueAttr.
     * XXX NOT YET!
     */
    public void doTestNodeIDAttr(int type)      throws Exception {
        int attrCount = 1 + rng.nextInt(3);
        for (int i = 0; i < attrCount; i++) {
            byte[] buffer = new byte[HEADER_LENGTH + 4 + NodeID.LENGTH];
            
            byte[] attrVal = new byte[NodeID.LENGTH];
            rng.nextBytes(attrVal);
            XLAttr attr = null;
            switch (type) {
                case SOURCE:        
                    attr = new Source(attrVal);         break;
                case DESTINATION:   
                    attr = new Destination(attrVal);    break;
                default:
                    fail ("INTERNAL ERROR: unknown NodeIDAttr type "
                            + type);
            }
            assertEquals( type,           attr.type);
            assertEquals( NodeID.LENGTH,    attr.length());
            for (int j = 0; j < NodeID.LENGTH; j++)
                assertEquals ( attrVal[j],  attr.value[j] );
            
            attr.encode(buffer, HEADER_LENGTH);
            XLAttr decoded = (XLAttr)XLAttr
                                .decode (buffer, HEADER_LENGTH);
            assertEquals( type,           decoded.type );
            assertEquals( NodeID.LENGTH,    decoded.length() );
            for (int j = 0; j < NodeID.LENGTH; j++)
                assertEquals ( attrVal[j],  decoded.value[j] );
        }
    }
    public void testSource()                    throws Exception {
        doTestNodeIDAttr(SOURCE);
    }
    public void testDestination()               throws Exception {
        doTestNodeIDAttr(DESTINATION);
    }
   
}
