/* NonBlockingLog.java */
package org.xlattice.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import org.xlattice.util.Timestamp;

/**
 * A simple non-blocking logger.
 *
 * XXX Todo: add possibly optional timestamps.
 *
 * @author Jim Dixon
 */
public class NonBlockingLog extends Thread {
    private static HashMap byName = new HashMap();
    
    private final String logFileName;
    private boolean running;
    private Queue queue = new Queue();
   
    // CONSTRUCTORS /////////////////////////////////////////////////
    private NonBlockingLog (String fileName) {
        if (fileName == null || fileName.equals(""))
            throw new IllegalArgumentException("null or empty log file name");

        logFileName = fileName;
        start();
    }
    public static NonBlockingLog getInstance(String name) {
        NonBlockingLog myLog;
        synchronized (byName) {
            myLog = (NonBlockingLog) byName.get(name);
            if (myLog == null) 
                myLog = new NonBlockingLog (name);
        }
        return myLog;
    }
    // INTERFACE Runnable ///////////////////////////////////////////
    public void run () {
        running = true;
        FileWriter writer = null;
        try {
            // open log in append mode
            writer = new FileWriter (new File(logFileName), true);
        } catch (IOException ioe) {
            System.err.println("problem opening log file - " 
                    + ioe);
            close();
        }
        while (running) {
            String msg = null;
            synchronized(queue) {
                while (running && queue.isEmpty()) {
                    try {
                        queue.wait();
                    } catch (InterruptedException ie) {
                        /* ignored */
                    }
                }
                // still within queue monitor, restarted by notify()
                // XXX This is wildly inefficient; should get all
                // XXX pending messages as a String array.
                if (running && !queue.isEmpty()) 
                    msg = (String) queue.dequeue();
            }
            if (msg != null) {
                try {
                    // XXX Should be writing String[] to disk
                    writer.write ( new StringBuffer ( msg.trim() )
                                .append("\n").toString() );
                    writer.flush();     // we want this behaviour
                } catch (IOException ioe) {
                    System.err.println(
                            "giving up: error writing log file - "
                            + ioe);
                    close();
                }
            }
        }
        // oh, we're done.  unlock/close the log file
        try {
            writer.close();
        } catch (IOException ioe) { /* don't much care */ }
    }
    // PROPERTIES ///////////////////////////////////////////////////
    public static int size () {
        return byName.size();
    }
    // OTHER METHODS ////////////////////////////////////////////////
    /**
     * Kill the log thread without necessarily completing the writing
     * of all log entries to disk.
     * XXX Respec?
     */
    public void close () {
        synchronized (queue) {
            running = false;
            queue.notifyAll();
        }
    }
    public boolean isOpen() {
        return running;
    }
    /**
     * Queue a message for logging.  The message is trimmed (leading
     * and trailing delimiters are removed) before writing to the 
     * log file.  XXX Might want to reconsider this.
     */
    public void message (String msg) {
        String s = new StringBuffer( new Timestamp().toString() )
                            .append ("  ")
                            .append (msg)
                            .toString();
        synchronized (queue) {
            queue.enqueue(s);
            queue.notify();
        }
    }
} 
