/* TestThreadPool.java */
package org.xlattice.util.threads;

import java.util.Random;
import junit.framework.*;

/**
 *
 * @author Jim Dixon
 */
public class TestThreadPool extends TestCase {

    private Random rng = new Random();
    private ThreadPool     tp;
    
    public TestThreadPool (String name) {
        super(name);
    }

    public void setUp () {
    }
   
    // TESTS ON EMPTY THREADPOOL ////////////////////////////////////
    private void tpCheck(int expectedMaxT, int expectedMaxJ) {
        assertSame   ("testPool", tp.getName());
        assertEquals ("thread pool has wrong capacity",
                                            expectedMaxT, tp.getMaxThreads());
        assertEquals ("should be no threads in pool", 0, tp.size());
    }
    public void testLowPool() {
        tp  = new ThreadPool ("testPool", 
                ThreadList.MIN_CAPACITY - 2,    // should be increased
                JobQueue.MIN_CAPACITY - 3,      // should be increased
                null, false);
        tpCheck (ThreadList.MIN_CAPACITY, JobQueue.MIN_CAPACITY);
    } 
    public void testPoolOverMin() {
        tp  = new ThreadPool ("testPool", 
                ThreadList.MIN_CAPACITY + 15, 
                JobQueue.MIN_CAPACITY   + 13, 
                null, false);
        tpCheck (ThreadList.MIN_CAPACITY + 15, JobQueue.MIN_CAPACITY + 13);
    } 
    public void testNamelessAndJustOverMin() {
        tp = new ThreadPool (null, 
                ThreadList.MIN_CAPACITY + 1, 
                JobQueue.MIN_CAPACITY   + 1, 
                null, false);
        assertTrue(tp.getName().startsWith(
                            "org.xlattice.util.threads.ThreadPool"));
        assertEquals ( ThreadList.MIN_CAPACITY + 1, tp.getMaxThreads());
    }
    // MORE REAL TESTS //////////////////////////////////////////////
    class Twiddle implements Runnable {
        final int delay;
        boolean done;
        public Twiddle (int ms) {
            if (ms <= 0)
                throw new IllegalArgumentException 
                                            ("delay must be positive");
            delay = ms;
        }
        public void run() {
            try {
                Thread.currentThread().sleep(delay);
            } catch (InterruptedException ie) { /* ignored as usual */ }
            done = true;
        }
    }
    public void testTwiddles()                  throws Exception {
        tp  = new ThreadPool ("testPool", 
                16,     // maximum threads
                -1,     // any number of jobs
                null,   // no thread group
                false); // not a daemon
        tpCheck (16, -1);

        // create a bunch of Twiddlers
        int N = 32;
        Twiddle[] twiddlers = new Twiddle[N];
        for (int i = 0; i < N; i++) 
            twiddlers[i] = new Twiddle( 1 + rng.nextInt(64) );
    } 
    public void testJoboids()                   throws Exception {
        int threadCount = 8 + rng.nextInt(32);
        int jobCount    = 2 * threadCount + rng.nextInt(32);
        
        tp  = new ThreadPool ("testPool", 
                threadCount,    // maximum threads
                -1,             // any number of jobs
                null,   // no thread group
                false); // not a daemon
        tpCheck (threadCount, -1);

        // create a bunch of Joboids
        Joboid[] joboids = new Joboid[jobCount];
        for (int i = 0; i < jobCount; i++) 
            joboids[i] = new Joboid();
        //for (int i = 0; i < jobCount; 
        
    }
}
