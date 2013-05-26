/* TestJobQueue.java */
package org.xlattice.util.threads;

import java.util.EmptyStackException;
import junit.framework.*;

/**
 * Tests on JobQueue objects.  
 *
 * @author Jim Dixon
 */
public class TestJobQueue extends TestCase {

    private JobQueue jQ;
    
    public TestJobQueue (String name) {
        super(name);
    }

    public void setUp () {
        Joboid.jobID = 0;           // risky, this ;-)
    }
    /** 
     * A mindless set of JobQueue tests that also sets up a fixture,
     * a list of so-many joboids. 
     *
     * The Joboids are never actually run.
     *
     * @param jQ       the list to be populated
     * @param capacity the number of joboids to put in the list
     */
    public void fillerUp( JobQueue jQ, int capacity) {
        assertEquals (0, jQ.size());
        assertTrue(jQ.isEmpty());

        if (jQ.capacity() != -1)
            assertEquals (capacity, jQ.capacity());
        for (int i = 0; i < capacity; i++) {
            Joboid z = new Joboid();
            jQ.enqueue(z);
            assertEquals (i + 1, jQ.size());
        }
        if (jQ.capacity() != -1) {
            // fixed capacity, no more room in the queue
            if (jQ.enqueue( new Joboid() ))
                fail ("expected queue-full failure");
        }
    }
    /**
     * Empty the list, which is expected to be full. 
     */
    public void dumpEmOut (JobQueue jQ) {
        int capacity = jQ.capacity();
        if (capacity == -1)
            capacity = jQ.size();
        assertEquals (jQ.size(), capacity);
        for (int i = capacity; i > 0; i --) {
            Joboid z = (Joboid)jQ.dequeue();
            assertEquals("joboid has wrong index", capacity - i, z.id);
        }
        assertEquals(0, jQ.size());
        assertTrue(jQ.isEmpty());

        // nothing else in the queue
        Joboid z = (Joboid)jQ.dequeue();
        assertNull(z);
    }
    // ACTUAL TESTS /////////////////////////////////////////////////
    public void testShortQ() {
        jQ  = new JobQueue (JobQueue.MIN_CAPACITY - 2);
        fillerUp(jQ, JobQueue.MIN_CAPACITY);
        dumpEmOut(jQ);
    } 
    public void testDefaultSizeList() {
        jQ  = new JobQueue (JobQueue.MIN_CAPACITY);
        fillerUp(jQ, JobQueue.MIN_CAPACITY);
        dumpEmOut(jQ);
    } 
    public void testQOverMin() {
        jQ  = new JobQueue (JobQueue.MIN_CAPACITY + 2);
        fillerUp(jQ, JobQueue.MIN_CAPACITY + 2);
        dumpEmOut(jQ);
    } 
    public void testVariableLengthQ() {
        jQ  = new JobQueue (-1);
        fillerUp(jQ, 23);
        dumpEmOut(jQ);
    } 
}
