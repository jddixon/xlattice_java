/* AsyncPacketHandler.java */
package org.xlattice.transport;

import java.nio.ByteBuffer;

/**
 * An AsyncPacketHandler and an AsyncPacketConnection cooperate 
 * in synchronizing access to two buffers, one used for output
 * and one used for input.  Typically the buffers are allocated
 * when the connection is created and the handler is an argument
 * to the constructor; the connection then uses setConnection()
 * to pass the handler references to the connection and the
 * two buffers.
 *
 * Initially the output buffer is under the control of the handler
 * and the input buffer under the control of the connection.  The
 * handler passes control of the output buffer to the connection
 * by invoking AsyncPacketConnection.sendData().  The handler then
 * must not modify output buffer contents or rely upon such contents
 * or the buffer flags under it receives a dataSent(), signalling 
 * that control has been passed back.
 *
 * Responsibility for the input buffer passes to the handler when
 * dataReceived() is invoked.  When the handler is through with the
 * buffer, it passes control back to the connection by invoking
 * AsyncPacketConnection.initiateReading().  While the handler has
 * control of the input buffer, it may trust that the connection
 * will not modify the buffer or its flags (or rely upon their value);
 * once control has been passed to the connection, the handler makes 
 * the same guarantee in turn.
 * 
 * At any time the connection may signal that either a disconnection
 * or an exception has occurred.  In the first case the handler must
 * treat the connection as permanently closed.  In the second the 
 * handler may take any appropriate action, possibly choosing to 
 * regard the connection as closed, in which case it should release
 * resources in use.
 * 
 * @see AsyncPacketConnection
 * @author Jim Dixon
 */
public interface AsyncPacketHandler {

    public void setConnection(AsyncPacketConnection cnx, 
                        ByteBuffer outBuffer, ByteBuffer inBuffer);

    public void dataSent();

    public void dataReceived();

    public void reportDisconnect();

    public void reportException(Exception exc);


}
