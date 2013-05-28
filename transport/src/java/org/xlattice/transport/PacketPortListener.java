/* PacketPortListener.java */
package org.xlattice.transport;

import java.nio.ByteBuffer;

import org.xlattice.Address;

/**
 * A PacketPortListener is associated with each SchPacketPort.
 * The listener enforces a protocol.  If the protocol requires,
 * the listener will begin by sending data to the other end.  If the
 * protocol requires, it may begin by listening for incoming data.
 * Some protocols will require both.
 *
 * XXX APPROPRIATE IF CONNECTION SUPPORTED:
 * When the PacketPort receives data, it does a read on the incoming
 * link.  If this reflects a disconnection (-1 read count), the 
 * PacketPort calls reportDisconnect().  If the read count is zero,
 * the PacketPort silently waits for more data.  If the read count is
 * greater than zero, the input buffer is presented to the listener
 * for analysis by a call to dataReceived().  The listener can then
 * choose to disconnect (data violated a filter constraint), wait for
 * more data, or accept the data and send a reply using a call to 
 * sendData(outBuffer) or sendDataTo(outBuffer, address).  The outBuffer
 * argument may or may not be the same as the input buffer.
 *
 * When the PacketPort has completing transmitting data, it calls
 * dataSent(). 
 *
 * @author Jim Dixon
 */

public interface PacketPortListener {

     /**
      * Tells the listener what PacketPort it is listening to and
      * what input buffer the PacketPort is using.  This method 
      * should be called once and only once.
      * 
      * IOScheduler and SelectionKey are available from the PacketPort.
      * This method should be called once and only once.
      *
      * Some protocols may make an initial sendData() call from this
      * method.  Others might do an initiateReading().
      *
      * @param spt   reporting SchPacketPort, should be immutable
      * @param inBuf input data buffer, should be immutable
      */
    public void setPacketPort (SchPacketPort spt, ByteBuffer inBuf);
    
    /**
     * Report to the listener that a data transmission has been 
     * completed on the PacketPort.  
     *
     * In protocols that alternate between reading and writing, this
     * might cause the listener to make an initiateReading() call.
     */
    public void dataSent ();    

    /**
     * Report to the listener than some data has been received on 
     * the PacketPort. This method will NOT be invoked if zero bytes 
     * were received on a PacketPort.  The data received is in the
     * PacketPort's input buffer.
     *
     * The listener must evaluates whether a complete message has been 
     * received.  If it has not, reading will continue at least until
     * the entire message has been received.
     *
     */
    public void dataReceived ();

    
    /**
     * Report an exception.  This may result in the PacketPort being
     * disconnected and closed.
     */
    public void reportException (Exception exc);

}
