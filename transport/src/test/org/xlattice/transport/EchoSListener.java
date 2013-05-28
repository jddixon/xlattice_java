/* EchoSListener.java */
package org.xlattice.transport;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import org.xlattice.util.NonBlockingLog;

/**
 * Echo server connection listener, attached to each new Connection
 * by the Acceptor.  This is a listener for a one-shot echo server.
 * That is, it accepts data from the connection, copies it back, and
 * closes the connection.
 *
 * @author Jim Dixon
 */
public class EchoSListener implements ConnectionListener {

    protected final static int BUFSIZE 
                                    = AbstractSchedulableTest.TEST_BUFSIZE;

    // DEBUG
    private static NonBlockingLog debugLog 
                                = NonBlockingLog.getInstance("debug.log");
    private void DEBUG_MSG(String s) {
        debugLog.message("EchoSListener" + s);
    }
    // END
   
    private       SchedulableConnection cnx;
    private       ByteBuffer dataIn;
    private final ByteBuffer dataOut
                        = ByteBuffer.allocate(BUFSIZE); 

    public EchoSListener () {}

    // INTERFACE ConnectionListener /////////////////////////////////
    public void setConnection (SchedulableConnection cnx, ByteBuffer buffer) {
        if (cnx == null || buffer == null) {
            DEBUG_MSG(".setConnection(): null cnx or buffer");
            throw new IllegalArgumentException ("null cnx or buffer");
        }
        this.cnx = cnx;
        DEBUG_MSG(".setConnection(), channel "
                + cnx.getChannel().hashCode()); 
        dataIn = buffer;
        cnx.initiateReading();
    }
    /**
     * The ByteBuffer contains some data; echo it back.  
     */
    public void dataReceived () {
        DEBUG_MSG(".dataReceived()");                           // DEBUG
        dataIn.flip();
        
        dataOut.clear();
        dataOut.put(dataIn);
        dataOut.flip();                 // prepare for write
        cnx.sendData (dataOut);         // send the reply
    }
    public void dataSent () {
        DEBUG_MSG(".dataSent()");                               // DEBUG
        _close();                       // the connection
    }
    public void reportDisconnect () {
        DEBUG_MSG(": UNEXPECTED DISCONNECTION");                // DEBUG
        _close();
    }
    public void reportException (Exception exc) {
        DEBUG_MSG(": UNEXPECTED REPORTED EXCEPTION: " + exc);   // DEBUG
        _close();
    }
    // OTHER METHODS ////////////////////////////////////////////////
    private void _close() {
        try { 
            cnx.getChannel().close();   // cancels the key
        } catch (IOException e) { /* ignore */ }
        
    }
}
