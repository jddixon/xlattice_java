/* Worker.java */
package org.xlattice.util.threads;

/**
 * A thread for use in a thread pool.
 *
 * @author Jim Dixon
 */
public class Worker extends Thread implements Killable {

    // INSTANCE VARIABLES ///////////////////////////////////////////
    protected final ThreadList workers;
    protected final JobQueue   jobQueue;

    /** thread is running a job */
    private volatile boolean busy;
    /** kill has been requested */
    private volatile boolean dying;

    // CONSTRUCTORS /////////////////////////////////////////////////
    /** 
     * Create a Worker, a carrier for jobs to be run in a thread pool.
     */
    public Worker (ThreadGroup tg, String name, 
                        ThreadList tl, JobQueue jobs, boolean daemon) {
        super (tg, name);
        workers  = tl;
        jobQueue = jobs;
        this.setDaemon(daemon);     // otherwise no interest in this flag
    }

    // PROPERTIES ///////////////////////////////////////////////////
    /** @return whether the thread is busy */
    protected final boolean isBusy() {
        return busy;
    }
    /** @return whether the thread is dying */
    protected final boolean isDying() {
        return dying;
    }
    // INTERFACE Killable ///////////////////////////////////////////
    /** 
     * Mark this thread as dying. 
     *
     * Implementation: the thread may be either waiting in the 
     * while loop for a job or it may be actually running the job.
     *
     * In the first case, set <code>dying,</code> the variable tested 
     * in run()'s while loop, and then actually interrupt the thread.  
     * This will take an unpredictable amount of time to have an effect, 
     * but eventually the thread will exit the monitor, check the
     * control variable, and do its termination routine.
     *
     * Jobs using this set of thread pool classes should be designed
     * to accept this pattern.  They should either respond in a 
     * sensible way to interrupts, or should periodically inspect a
     * publicly accessible control variable, or do both.
     */
    public final synchronized void die() {
        dying = true;
        //myThread.interrupt();
    }
    // INTERFACE Runnable (from Thread) /////////////////////////////
    /** If there is a job to run, run it. */
    public final void run() {
        while (!dying) {
            runNextJob();
        } 
        // morituri te salutamus
        synchronized (workers) {
            // XXX THIS CREATES PROBLEMS; can affect busyCount
            workers.remove(this);
            workers.notify();   // wake up the guy who told me to die
        }
    }
    /**
     * Split out to allow easy subclassing.  
     */
    protected void runNextJob() {
        Runnable job = null;
        synchronized (jobQueue) {
            while (jobQueue.size() == 0 && !dying) {
                try {
                    synchronized (jobQueue) { jobQueue.wait(); }
                } catch (InterruptedException ie) {
                    // ignored, but forces test of the while condition
                }
            }
            // still within jobQueue monitor, restarted by notify()
            if (!dying && jobQueue.size() > 0) {
                job = (Runnable) jobQueue.dequeue();
            }
        } 
        if (job != null && !dying) {
            synchronized(workers) {
                workers.startJob();     // increments count of busy jobs
                busy = true;
            }
            job.run();
            synchronized(workers) {
                busy = false;
                workers.endJob();       // decrements count
            }
            job = null;                 // GC is good for you
        }
    }
}
