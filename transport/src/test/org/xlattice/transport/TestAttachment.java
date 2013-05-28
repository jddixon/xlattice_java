/* TestAttachment.java */
package org.xlattice.transport;

import java.util.Random;
import junit.framework.*;

/**
 * @author Jim Dixon
 */
public class TestAttachment extends TestCase {

    protected Random rng = new Random();
    
    private int            count;
    private Attachment[]   attas;
    private int[]          types;
    private Object[]       objs;
    private AttachmentPool pool;
    
    public TestAttachment (String name)            throws Exception {
        super(name);
    }

    public void setUp ()                        throws Exception {
    }
    public void tearDown()                      throws Exception {
        attas = null;
        types = null;
        objs  = null;
        pool  = null;
    }
    public void testAFew()                      throws Exception {
        pool  = new AttachmentPool(64);
        count = 3 + rng.nextInt(6);     // so 3 to 8
        attas = new Attachment[count];
        types = new int[count];
        objs  = new Object[count];
        
        assertEquals(0,     pool.size());
        for (int i = 0; i < count; i++) {
            types[i] = 1 + ( i % Attachment.PKT_A );
            objs [i] = new Integer( i );
            attas[i] = pool.get( types[i], objs[i] );
        }
        assertEquals(0,     pool.size());
        for (int i = 0; i < count; i++) {
            assertEquals ( types[i],         attas[i].type);
            assertEquals ( (Integer)objs[i], (Integer)attas[i].obj );
        }
        for (int i = 0; i < count; i++) {
            pool.dispose(attas[i]);
            assertEquals (i + 1,            pool.size());
            assertEquals (Attachment.NIX_A, attas[i].type);
            assertNull   (attas[i].obj);
        }
        int newCount = count + 3 + rng.nextInt(6);  // 3 to 8 more
        attas = new Attachment[newCount];
        types = new int[newCount];
        objs  = new Object[newCount];
        assertEquals( count,                pool.size());
        for (int i = 0; i < newCount; i++) {
            types[i] = 1 + ( i % Attachment.PKT_A );
            objs [i] = new Integer( i );
            attas[i] = pool.get( types[i], objs[i] );
            if (i < count) 
                assertEquals( count -i -1,  pool.size());
            else
                assertEquals( 0,            pool.size());
        }
        for (int i = 0; i < newCount; i++) {
            assertEquals ( types[i],         attas[i].type);
            assertEquals ( (Integer)objs[i], (Integer)attas[i].obj );
        }
    }
}
