/* BindingSender.java */
package org.xlattice.protocol.stun;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;

import org.xlattice.util.NonBlockingLog;
import org.xlattice.util.Queue;

/**
 * A BindingSender manages a queue of outgoing messages, sending each
 * from its address/port combination to the desired target.  The
 * four BindingServers can dispatch outgoing messages to any of the
 * four BindingSenders.
 *
 * @author Jim Dixon
 */
public class BindingSender                          extends Thread {
    public static final int OUTBUF_SIZE = 512;
   
    private volatile boolean running;
    private Object lock = new Object();
    private Thread myThread;

    private DatagramSocket socket;
    private final Inet4Address myHost;
    private final int          myPort;
    private final String       myName;

    private final Queue outQ;
    
    // LOGGING //////////////////////////////////////////////////////
    protected final NonBlockingLog serverLog;
    protected void LOG_MSG(String s) {
        serverLog.message(
                new StringBuffer("BindingSender: ").append(s).toString());
    }
    // CONSTRUCTORS /////////////////////////////////////////////////
    protected BindingSender (Inet4Address host, int port, String logDir) 
                                                throws SocketException {
        // we assume Server has checked logDir for reasonableness
        serverLog = NonBlockingLog.getInstance(logDir + "stun.server.log");
        if(host == null)
            badArg("null IP address");
        if(port < 0 || 65535 < port)
            badArg("port number out of range: " + port);
        myHost = host;
        myPort = port;
        myName = new StringBuffer(myHost.toString())
                    .append(':')
                    .append(myPort)
                    .toString();
        outQ   = new Queue();

        try {
            socket= new DatagramSocket(myPort, myHost);
        } catch (SocketException se) {
            LOG_MSG("can't open socket for " + myName + " - " 
                    + se);
            throw se;
        }
        start();
    }
    protected BindingSender (Inet4Address host, int port) 
                                                throws SocketException {
        this(host, port, "." + File.separator);
    }
    private final void badArg(String msg) {
        LOG_MSG(msg);
        throw new IllegalArgumentException(msg);
    }
    // ACCESS METHODS ///////////////////////////////////////////////
    public DatagramSocket getSocket() {
        return socket;
    }
    // INTERFACE Runnable ///////////////////////////////////////////
    public void run() {
        LOG_MSG("BindingSender.run() " + myName);
        // you aren't running until you have a socket, boy
        running = true;
        while (running) {
            DatagramPacket outPkt;
            byte[] buffer = new byte[OUTBUF_SIZE];
            synchronized (outQ) {
                while (outQ.size() == 0 && running) {
                    try {
                        synchronized (outQ) { outQ.wait(); }
                    } catch (InterruptedException ie) {
                        // ignored, but forces test of the while condition
                    }
                }
                // still within outQ monitor, restarted by notify()
                if (running && outQ.size() > 0) {
                    Outgoing out = (Outgoing)outQ.dequeue();
                    outPkt = new DatagramPacket(out.msg, 
                                out.msg.length, out.addr, out.port);
                    try {
                        socket.send (outPkt);
                    } catch (IOException ioe) {
                        LOG_MSG("send failed, discarding packet: " 
                                + ioe);
                    }
                }
            }
        } 
        socket.close();
    }
    public void schedule(Outgoing out) {
        if (out == null)
            throw new IllegalArgumentException("null Outgoing");
        synchronized(outQ) {
            outQ.enqueue(out);
            outQ.notify();
        }
    }
    /**
     * Closes the Acceptor and then blocks until this thread stops
     * running.
     */
    public void close()                     throws Exception {
        synchronized (lock) {
            running = false;
            if (socket != null && !socket.isClosed())
                socket.close();
        }
        if ( Thread.currentThread() != myThread && myThread != null ) 
            myThread.join();
    }
    public Thread getThread() {
        return myThread;
    }
    public synchronized boolean isRunning() {
        return running;
    }
}
