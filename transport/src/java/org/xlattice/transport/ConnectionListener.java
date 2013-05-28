/* ConnectionListener.java */
package org.xlattice.transport;

import java.nio.ByteBuffer;

/**
 * A ConnectionListener is associated with each SchedulableConnection.
 * The listener enforces a protocol.  If this is a client connection,
 * the listener will begin by sending data to the other end.  If it is
 * a server connection, it will begin by receiving data.  
 *
 * When the connection receives data, it does a read on the incoming
 * link.  If this reflects a disconnection (-1 read count), the 
 * connection calls reportDisconnect().  If the read count is zero,
 * the connection silently waits for more data.  If the read count is
 * greater than zero, the input buffer is presented to the listener
 * for analysis by a call to dataReceived().  The listener can then
 * choose to disconnect (data violated a filter constraint), wait for
 * more data, or accept the data and send a reply using a call to 
 * sendData(outBuffer).  The argument may or may not be the same as
 * the input buffer.
 *
 * When the connection has completing transmitting data, it calls
 * dataSent().  If this is a client connection, this will be followed
 * by a call to initiateReading() on the connection.  If it is a 
 * server connection and the protocol is simple, the connection is
 * closed after receiving a dataSent() call.
 *
 * @author Jim Dixon
 */
public interface ConnectionListener {

     /**
      * Tells the listener what connection it is listening to and
      * what input buffer the connection is using.
      * 
      * IOScheduler and SelectionKey are available from the connection.
      * This method should be called once and only once.
      *
      * If this is a client connection, this call will result in an
      * initial sendData() call.  If it is a server connection, it
      * will result in an initiateReading() call on the connection.
      *
      * @param cnx    reporting SchedulableConnection
      * @param buffer input data buffer
      */
    public void setConnection (SchedulableConnection cnx, ByteBuffer buffer);
    
    /**
     * Report to the listener that a data transmission has been 
     * completed on cnx.  If this is a client connection, this will cause
     * an initiateReading() call.  If it is a server connection
     * and the protocol is simple, the connection will be closed.
     * If the protocol involves a series of messages, the listener
     * will call initiateReading() on the connection to begin the
     * next message cycle.
     */
    public void dataSent ();    

    /**
     * Report to the listener than some data has been received on cnx.  
     * This method will NOT be invoked if zero bytes were received on a 
     * connection.  The listener evaluates whether a complete message
     * has been received.  If it has, it initiates a write.  
     */
    public void dataReceived ();

    /**
     * Report that the SchedulableConnection has been closed at the far 
     * end.  The listener should free up any allocated resources.
     */
    public void reportDisconnect ();

    /**
     * Report an exception.  This may result in the connection being
     * closed, or it may cause an error message to be sent to the 
     * other end of the connection.
     */
    public void reportException (Exception exc);

}
