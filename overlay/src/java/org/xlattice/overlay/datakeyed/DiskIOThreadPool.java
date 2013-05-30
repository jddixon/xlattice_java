/* DiskIOThreadPool.java */
package org.xlattice.overlay.datakeyed;

import org.xlattice.util.NonBlockingLog;
import org.xlattice.util.threads.ThreadPool;

/**
 * @author Jim Dixon
 **/

public class DiskIOThreadPool extends ThreadPool {

    private   final IDiskCache _diskCache;
    protected final String pathToXLattice;

    protected NonBlockingLog debugLog;
    protected NonBlockingLog errorLog;

    public DiskIOThreadPool (IDiskCache diskCache) {
        super("DiskCache", 32, -1, null, false);
        if (diskCache == null)
            throw new IllegalArgumentException("null DiskCache");
        _diskCache     = diskCache;      
        pathToXLattice = _diskCache.getPathToXLattice();
        
        setDebugLog("debug.log");
        setErrorLog("error.log");
        DEBUG_MSG(": constructor");
    }
    // LOGGING //////////////////////////////////////////////////////
    /** 
     * Set the name of the file debug messages are logged to.  If
     * this is null, no logging is done.
     *
     * @param name path to log file from current directory
     */
    protected final void setDebugLog (String name) {
        if (debugLog != null)
            throw new IllegalStateException("can't change debug log name");
        if (name != null)
            debugLog   = NonBlockingLog.getInstance(name);
    }
    /** 
     * Set the name of the file error messages are logged to.  If
     * this is null, no logging is done.
     *
     * @param name path to log file from current directory
     */
    protected final void setErrorLog (String name) {
        if (errorLog != null)
            throw new IllegalStateException("can't change error log name");
        if (name != null)
            errorLog   = NonBlockingLog.getInstance(name);
    }
    
    /**
     * Subclasses should override.
     */
    protected void DEBUG_MSG(String msg) {
        if (debugLog != null)
            debugLog.message("DiskIOThreadPool" + msg);
    }
    protected void ERROR_MSG(String msg) {
        if (errorLog != null)
            errorLog.message("DiskIOThreadPool" + msg);
    }
    /**
     * Specializes this ThreadPool to handle DiskIOJobs.
     *
     * @param job DiskIOJob to be scheduled for running.
     */
    public boolean accept (Runnable job) {
        boolean succeeded = false;
        DiskIOJob myJob = (DiskIOJob) job;
        synchronized (workers) {
            if (workers.allBusy() && workers.size() < maxThreads) {
                int count = workers.size();  // wasteful
                DEBUG_MSG(".accept(): " + count + " workers before adding");
                DiskIOJobThread worker 
                    = new DiskIOJobThread (threadGroup, name + count, 
                            workers, jobQ, daemon, 
                            new FlatDisk(pathToXLattice));
                worker.start();
                workers.add (worker);
            }
            // DEBUG
            else 
                DEBUG_MSG(".accept(): couldn't add another worker:"
                        + "\n    allBusy     = " + workers.allBusy()
                        + "\n    worker size = " + workers.size()
                        + "\n    maxThreads  = " + maxThreads);
            // END
        }
        synchronized (jobQ) {
            DEBUG_MSG(".accept: enqueuing job");
            jobQ.enqueue(myJob);
            jobQ.notify();      // start any thread waiting for a job
        }
        // XXX THIS IS A BIT SILLY XXX
        succeeded = true;
        return succeeded;
    }
}
