/* DiskIOJobThread.java */
package org.xlattice.overlay.datakeyed;

import org.xlattice.overlay.DataKeyed;
import org.xlattice.util.NonBlockingLog;
import org.xlattice.util.threads.JobQueue;
import org.xlattice.util.threads.Worker;
import org.xlattice.util.threads.ThreadList;

/**
 * @author Jim Dixon
 **/

public class DiskIOJobThread extends Worker {

    protected final static NonBlockingLog debugLog 
        = NonBlockingLog.getInstance("debug.log");
    protected final static NonBlockingLog errorLog 
        = NonBlockingLog.getInstance("error.log");
    
    private final DataKeyed myDisk;

    public DiskIOJobThread (ThreadGroup tg, String jobName,
            ThreadList tList, JobQueue jobs, boolean daemon,
                                             DataKeyed disk) {
        super (tg, jobName, tList, jobs, daemon);
        if (disk == null)
            throw new IllegalArgumentException("null disk");
        myDisk = disk;

        DEBUG_MSG(": constructor, job " + jobName);
    }
    // LOGGING //////////////////////////////////////////////////////
    /**
     * Any subclasses should override.
     */
    protected void DEBUG_MSG(String msg) {
        if (debugLog != null)
            debugLog.message("DiskIOJobThread" + msg);
    }
    protected void ERROR_MSG(String msg) {
        if (errorLog != null)
            errorLog.message("DiskIOJobThread" + msg);
    }
    // OVERRIDES ////////////////////////////////////////////////////
    /** 
     * Overrides the method in the superclass, calling DiskIOJob.setDisk().
     */
    protected void runNextJob() {
        DEBUG_MSG(".runNextJob");
        DiskIOJob job = null;
        synchronized (jobQueue) {
            while (jobQueue.size() == 0 && !isDying()) {
                try {
                    synchronized (jobQueue) { jobQueue.wait(); }
                } catch (InterruptedException ie) {
                    // ignore it
                }
            }
            // still within jobQueue monitor, restarted by notify()
            if (!isDying() && jobQueue.size() > 0) {
                job = (DiskIOJob) jobQueue.dequeue();
            }
        }
        if (job != null && !isDying()) {
            synchronized (workers) { workers.startJob(); }
            job.setDisk(myDisk);
            DEBUG_MSG(".runNextJob: calling job.run");
            job.run();
            synchronized (workers) { workers.endJob(); }
        }
        // DEBUG
        else
            DEBUG_MSG(".runNextJob: couldn't run job");
        // END
    }
}
