/* AsyncPacketConnection.java */
package org.xlattice.transport;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * An AsyncPacketConnection presents a packet-oriented interface
 * to the outside world.  It has two buffers, one for input and one
 * for output.  Each buffer belongs either to the application or to
 * the connection at any given time.
 *
 * When the application wants to write, it puts data in the output buffer 
 * and signals that it is ready by calling sendData().  This passes 
 * ownership of the output buffer to the connection.  The connection 
 * releases the buffer back to the application by calling 
 * AsyncPacketHandler.dataSent().
 *
 * Neither of these calls may block.  Typically sendData() copies and
 * possibly transforms (perhaps encrypts) the data in the application's
 * thread.  dataReceived might similarly copy data from the input buffer
 * to an application buffer.
 *
 * When the application is prepared to receive data, it signals this
 * by a call to initiateReading().  This passes control of the buffer
 * to the connection.  When the connection has data in the buffer, it
 * signals this and passes control of the buffer back to the application
 * by a call to AsyncPacketHandler.dataReceived().
 *
 * @see AsyncPacketHandler
 * @author Jim Dixon
 */
public interface AsyncPacketConnection {

    public void close()                         throws IOException ;
    
    public boolean isClosed();

    public AsyncPacketHandler getPacketHandler();

    // INTERFACE USED BY AsyncPacketHandler /////////////////////////

    public ByteBuffer getOutBuffer();

    public ByteBuffer getInBuffer();
    
    public void sendData();

    public void initiateReading();

}
