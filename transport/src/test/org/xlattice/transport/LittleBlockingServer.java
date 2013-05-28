/* LittleBlockingServer.java */
package org.xlattice.transport;

import java.io.InputStream;
import java.io.OutputStream;
import org.xlattice.*;

/**
 * @author Jim Dixon
 */

public class LittleBlockingServer extends Thread {

    private static final int TEST_BUFSIZE 
                                = AbstractBlockingTest.TEST_BUFSIZE;
    private final ClientServer transport;
    private final Address   address;
    private final boolean   echoing;

    private volatile boolean running;
    private Object  lock = new Object();
    private final byte[] buffer = new byte[TEST_BUFSIZE];
    private       Thread myThread;
    
    Acceptor acceptor;
  
    LittleBlockingServer (EndPoint e)                throws Exception {
        this (e, false);
    }
    LittleBlockingServer (EndPoint e, boolean echoing)
                                            throws Exception {
        transport = (ClientServer)e.getTransport();
        address   = e.getAddress();
        acceptor  = transport.getAcceptor(address, true);
        this.echoing = echoing;
        running = true;
        start();
    }
    public void run () {
        myThread = Thread.currentThread();
        while (true) {
            synchronized (lock) {
                if (!running)
                    break;
            }
            try {
                Connection conn = acceptor.accept();
                if (echoing) {
                    InputStream ins = conn.getInputStream();
                    int bytesIn = ins.read(buffer, 0, TEST_BUFSIZE);
                    OutputStream outs = conn.getOutputStream();
                    outs.write(buffer, 0, TEST_BUFSIZE);
                } else {
                    // just ignore the input
                }
                // XXX jdd 2006-02-20: should let client close the connection
                conn.close();
            } catch (Exception e) { 
                try {
                    close();
                } catch (Exception e2) { }
            }
        }
    }
    /**
     * Closes the Acceptor and then blocks until this thread stops
     * running.
     */
    public void close()                     throws Exception {
        synchronized (lock) {
            running = false;
            acceptor.close();
        }
        if ( Thread.currentThread() != myThread && myThread != null ) 
            myThread.join();
    }
    public synchronized boolean isRunning() {
        return running;
    }
} 
