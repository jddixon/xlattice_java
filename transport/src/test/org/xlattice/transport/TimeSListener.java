/* TimeSListener.java */
package org.xlattice.transport;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Date;

/**
 * @author Jim Dixon
 **/

import org.xlattice.util.NonBlockingLog;

/**
 * Time server connection listener.
 */
public class TimeSListener implements ConnectionListener {

    protected final static int BUFSIZE 
                                = AbstractSchedulableTest.TEST_BUFSIZE;

    // DEBUG
    private static NonBlockingLog log = NonBlockingLog.getInstance("junk.TSL");
    private void logMsg(String s) {
        log.message(s);
    }
    // END
    private SchedulableConnection cnx;
    private ByteBuffer dataIn;
    private String callersAddress;

    private final ByteBuffer dataOut = ByteBuffer.allocate(BUFSIZE); 
    private boolean connectionClosePending;
    private boolean schedulerClosePending;

    public TimeSListener (String logName) {
    }

    // PROPERTIES ///////////////////////////////////////////////////
    public boolean shouldCloseConnection() {
        return connectionClosePending;
    }
    public boolean shouldCloseScheduler() {
        return schedulerClosePending;
    }
    // INTERFACE ConnectionListener /////////////////////////////////
    public void setConnection (SchedulableConnection cnx, ByteBuffer buffer) {
        if (cnx == null || buffer == null)
            throw new IllegalArgumentException ("null cnx or buffer");
        this.cnx = cnx;
        dataIn = buffer;

        SocketChannel sChan = (SocketChannel)cnx.getChannel();
        Socket sock = sChan.socket();
        callersAddress = new StringBuffer(": ")
                            .append(sock.getInetAddress().toString())
                            .append(":")
                            .append(sock.getPort())
                            .append(" ")
                            .toString();
                                            
    }
    /**
     * The ByteBuffer contains some text data.  Build a reply which
     * contains the IP address and port number of the sender, the 
     * current time, and the text received.
     */
    public void dataReceived () {
        dataIn.flip();
        String s = new String (dataIn.array(), 0, dataIn.limit())
                        .trim();
        dataIn.clear();
        
        String msg = new StringBuffer()
            .append(new Date().toString())
            .append(callersAddress)
            .append(s)
            .append("\r\n")
            .toString();
        dataOut.clear();
        dataOut.put(msg.getBytes());
        dataOut.flip();                 // prepare for write

        log.message (msg.trim());       // log the transaction
        cnx.sendData (dataOut);         // send the reply
       
        if (s.startsWith("quit")) {
            // seem to need both
            //sChan.close();
            //sk.cancel();
            connectionClosePending = true;
        } else if (s.startsWith("QUIT")) {
            //sChan.close();  // for neatness
            //selector.close();        
            //log.close();
            schedulerClosePending = true;
        } 
        //else {
        //  sk.interestOps(SelectionKey.OP_READ);
        //}
    }
    /** 
     */
    public void dataSent () {
        // THIS SORT OF THING IS VERY BAD PRACTICE 
        // but permitted by our interface
        if (connectionClosePending || schedulerClosePending) {
            try { 
                cnx.getChannel().close(); 
            } catch (IOException e) { /* ignore */ }
        }
        if (schedulerClosePending) {
            cnx.getScheduler().close();
            log.close();
        }
    }
    public void reportDisconnect () {
        System.out.println("UNEXPECTED DISCONNECTION");
    }
    public void reportException (Exception exc) {
        System.out.println("UNEXPECTED REPORTED EXCEPTION: " + exc);
    }
}
