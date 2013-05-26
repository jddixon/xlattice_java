/* Joboid.java */
package org.xlattice.util.threads;

import java.util.Random;

/**
 * @author Jim Dixon
 **/

public class Joboid implements Runnable {

    static Random rng = new Random();
    static int jobID;
    
    final Thread myThread = Thread.currentThread();
    final int id;
    
    public Joboid () {
        id = jobID++;
    }
    public void run() {
        int ms = 10 + rng.nextInt(15);
        try {
            myThread.sleep(ms);            // sleep 10 - 25ms
        } catch (InterruptedException ie) { /* ignore */ }
        myThread.yield();
        try {
            myThread.sleep(ms);            // so for a total of 20 - 50ms
        } catch (InterruptedException ie) { /* ignore */ }
    }
}
