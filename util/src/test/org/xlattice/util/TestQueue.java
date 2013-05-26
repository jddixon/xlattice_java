/* TestQueue.java */
package org.xlattice.util;

import java.util.Random;
import junit.framework.*;

/**
 * @author Jim Dixon
 **/

public class TestQueue extends TestCase {

    /** 
     * Note that Sun's implementation initializes the random number
     * generator with the run time in ms.
     */
    private Random rng = new Random();
    private Queue q;
    
    public TestQueue (String name) {
        super(name);
    }

    public void setUp () {
        q = null;
    }
    private void fillUpAndEmpty(Queue q) {
        int count = 3 + rng.nextInt(10);
        String [] s = new String[count];
        StringBuffer sb;
        for (int i = 0; i < count; i++) {
            s[i] = new StringBuffer("foo").append(i).toString();
            q.enqueue(s[i]);
            assertEquals( i + 1, q.size() );
        }
        for (int i = 0; i < count; i++) {
            assertEquals(count - i, q.size() );
            String out = (String) q.dequeue();
            assertNotNull(out);
            // the FIFO condition
            assertEquals( s[i], out );
        }
        // these should be the same condition, of course:
        assertEquals(0, q.size());
        assertTrue(q.isEmpty());
        
        assertNull(q.dequeue());
    }
    /** 
     * Build a queue, then fill it up and empty it a few times.
     */
    public void testSimpleQueue()               throws Exception {
        q = new Queue();
        assertNotNull(q);
        assertNull(q.dequeue());
        assertEquals(0, q.size());
        assertTrue(q.isEmpty());

        int count = 2 + rng.nextInt(3);
        for (int k = 0; k < count; k++)
            fillUpAndEmpty(q);
    }
}
