/* ThreadPool.java */
package org.xlattice.util.threads;

/**
 * Fixed size pool of Killable threads which consume a (possibly
 * variable in size) queue of Killable jobs, Runnables with a die()
 * method.  
 * 
 * XXX Modify to allow change in pool capacity.  
 *
 * @author Jim Dixon
 */
public class ThreadPool {

    // INSTANCE VARIABLES ///////////////////////////////////////////
    protected final String name;
    protected final ThreadGroup threadGroup;
    protected final int maxThreads;
    /** we synchronize access to this */
    protected final ThreadList workers;
    /** 
     * If -1, length varies dynamically with no specific max;
     * otherwise, rejects jobs if max queue length exceeded.
     */
    protected final int maxLenJobQ;
    /** we synchronize access to this */
    protected final JobQueue jobQ;
    protected final boolean daemon;
    
    /** count of all threads ever created */
    private long workerIndex;
   
    // CONSTRUCTORS /////////////////////////////////////////////////
    /** 
     * Create a thread pool with a default queue size of 128 and using
     * a default ThreadGroup
     */
    public ThreadPool (String s, int maxP) {
        this(s, maxP, 128, null, false);
    }
    /** 
     * Create a thread group with a default queue size of 128
     */
    public ThreadPool (String s, int maxP, ThreadGroup tg) {
        this (s, maxP, 128, tg, false);
    }
    /** 
     * Create a thread pool, making up a default name if necessary.
     * XXX Probably should force a minimum maxP and maxJ
     *
     * @param s    name of thread pool, base for thread names
     * @param maxP maximum number of idle threads in pool
     * @param maxJ maximum number of jobs in the queue
     * @param tg   thread group threads will belong to
     */
    public ThreadPool (String s, int maxP, int maxJ, ThreadGroup tg, 
                                                        boolean b) {
        if (s == null) {
            if (tg != null) {
                s = tg.getName();
            } else {
                s = new StringBuffer().append(this).toString();
            }
        }
        name = s;
        if (tg == null) {
            tg = new ThreadGroup(name);
            // defaults to normal priority
        }
        threadGroup = tg;
        maxThreads     = Math.max(maxP, ThreadList.MIN_CAPACITY); 
        workers     = new ThreadList(maxThreads);
        if (maxJ == -1)         // variable length
            maxLenJobQ = maxJ;
        else
            maxLenJobQ  = Math.max(maxJ, JobQueue.MIN_CAPACITY); 
        jobQ        = new JobQueue(maxLenJobQ);
        daemon      = b;
    }

    // PROPERTIES ///////////////////////////////////////////////////
    /** @return base name of this thread group */
    public String getName()        { return name; }
    /** @return maximum number of jobs allowed in the queue */
    public int getMaxLenJobQueue() { return maxLenJobQ; }
    /** @return maximum number of threads in the pool */
    public int getMaxThreads()     { return maxThreads; }
    /** @return number of threads in the pool */
    public int size()  { 
        synchronized (workers) {
            return workers.size();
        }
    }
    // JOB QUEUE MANAGEMENT /////////////////////////////////////////
    /**
     * Accept a Runnable into the job queue.  If there are no idle
     * workers, then if possible a new worker is created.  The job
     * is then queued, if there is enough room in the queue.
     *
     * There is no guarantee that the job causing a new worker to
     * be created will be run in that new thread or even added to
     * the queue.
     *
     * @return whether the job could be queued
     */
    public boolean accept (Runnable job) {
        synchronized (workers) {
            if (workers.allBusy() && (workers.size() < maxThreads)) {
                workers.add ( 
                    new Worker (threadGroup, 
                        new StringBuffer(name).append(workerIndex++)
                                                            .toString(), 
                        workers, jobQ, daemon) );
            }
        }
        synchronized (jobQ) {
            if (jobQ.enqueue (job)) {
                jobQ.notify();      // start any thread waiting for a job
                return true;
            } else {
                return false;       // no room in queue for job
            }
        }
    }
    /**
     * XXX Doesn't actually wait for worker threads to stop running.
     *
     * Preferred behavior: take a snapshot of all workers, tell each
     * one to die, and then wait until isAlive() is false for each
     * thread.  Could use join() instead.  Better: build a multi-level
     * kill() that first checks isAlive(), then sets the dying flag
     * using die() and waits a short time, and finally actually 
     *
     */
    public void stop () {
        synchronized (workers) {
            workers.die();
            while (workers.size() > 0) {
                try {
                    workers.wait();
                } catch (InterruptedException e) {
                    // ignore it
                }
            }
        }
    }
}
