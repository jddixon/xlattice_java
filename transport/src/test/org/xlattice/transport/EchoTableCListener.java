/* EchoTableCListener.java */
package org.xlattice.transport;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Random;

/**
 * @author Jim Dixon
 **/

import org.xlattice.util.NonBlockingLog;

/**
 * Echo server connection listener, attached to each new Connection
 * by the Acceptor.  This is a listener for a one-shot echo server.
 * That is, it accepts data from the connection, copies it back, and
 * closes the connection.
 */
public class EchoTableCListener implements ConnectionListener {

    protected final static int BUFSIZE = AbstractSchedulableTest.TEST_BUFSIZE;

    // DEBUG
    private static NonBlockingLog debugLog 
                            = NonBlockingLog.getInstance("debug.log");
    private void DEBUG_MSG(String s) {
        debugLog.message("EchoTableCListener" + s);
    }
    // END
    
    private       SchedulableConnection cnx;
    // XXX make sure that the sense of these is the same in client and
    // XXX server listeners
    private       ByteBuffer dataIn;
    private final ByteBuffer dataOut
                        = ByteBuffer.allocate(BUFSIZE); 

    private final int index;
    private final EchoTable table;
    
    Random rng = new Random( new Date().getTime() );
   
    // CONSTRUCTOR //////////////////////////////////////////////////
    /** Unreachable no-arg constructor */
    private EchoTableCListener () {
        index = -1;
        this.table = null;
    }
    public EchoTableCListener (int n, EchoTable table) {
        DEBUG_MSG("(" + n + ", table)");
        index = n;
        this.table = table;
    }

    // INTERFACE ConnectionListener /////////////////////////////////
    public void setConnection (SchedulableConnection cnx, ByteBuffer buffer) {
        if (cnx == null || buffer == null)
            throw new IllegalArgumentException ("null cnx or buffer");
        this.cnx = cnx;
        DEBUG_MSG(".setConnection(), channel "
                + cnx.getChannel().hashCode()); 
        dataIn = buffer;
        
        dataOut.clear();
        byte[] b = new byte [BUFSIZE];      // another arbitrary number
        rng.nextBytes(b);
        dataOut.put(b);
        dataOut.flip();
       
        table.recordSend (index, b);
        DEBUG_MSG(                                             // DEBUG
          "EchoTableCListener.setConnection(): calling cnx.sendData()"); 
        cnx.sendData(dataOut);
    }
    public void dataReceived () {

        DEBUG_MSG( new StringBuffer(
                    ".dataReceived(), listener ")
                    .append(index)  
                    .toString() );
        dataIn.flip();
        byte[] b = new byte [BUFSIZE];
        for (int i = 0; i < BUFSIZE; i++)
            b[i] = dataIn.array()[i];
        table.recordReceive (index, b);
        _close();               // the connection
    }
    public void dataSent () {
        DEBUG_MSG(".dataSent()");            // DEBUG
        cnx.initiateReading();
    }
    public void reportDisconnect () {
        DEBUG_MSG(": UNEXPECTED DISCONNECTION");                 // DEBUG
        _close();
    }
    public void reportException (Exception exc) {
        DEBUG_MSG(": UNEXPECTED REPORTED EXCEPTION: " + exc);    // DEBUG
        _close();
    }
    // PROPERTIES ///////////////////////////////////////////////////
    public SchedulableConnection getConnection () {
        // DEBUG
        if (cnx == null) 
            DEBUG_MSG ("EchoTableCListener.getConnection: cnx is null");
        else
            DEBUG_MSG ("EchoTableCListener.getConnection: cnx is NOT null");
        // END
        return cnx;
    }
    public int getIndex() {
        return index;
    }
    public EchoTable getTable() {
        return table;
    }
    // OTHER METHODS ////////////////////////////////////////////////
    private void _close() {
        try { 
            cnx.getChannel().close();   // cancels the key
        } catch (IOException e) { /* ignore */ }
        
    }
}
