/* SchedulableConnection.java */
package org.xlattice.transport;

import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

/**
 * @author Jim Dixon
 **/

import org.xlattice.Connection;

public interface SchedulableConnection extends Connection {

    // PROPERTIES ///////////////////////////////////////////////////
    // normally set by the constructor //////////////////////////////
    /**
     * @return the IOScheduler this Connection is assigned to
     */
    public IOScheduler getScheduler();

    /**
     * Returns the NIO channel.  This is used to get a SelectionKey
     * and so must be available on this interface.
     * 
     * @return the channel involved in the connection.
     */
    public SelectableChannel getChannel();

    /**
     * Set the NIO selection key.  Used by the IOScheduler.
     */
    public SchedulableConnection setKey (SelectionKey key);

    public SelectionKey getKey();

    // INTERFACE TO IOSCHEDULER /////////////////////////////////////
    /**
     * Called by the IOScheduler when data has been received, that
     * is, when an isReadable() SelectionKey has been received.
     *
     * The input buffer is checked.  If a read returns -1, the 
     * connection has been closed and the ConnectionListener's 
     * reportDisconnect() method should be called.  If the read 
     * returns 0, the call should be silently ignored.  Any other 
     * read value should result in a call to the listener's 
     * dataReceived() method.
     */
    public void readyToRead();

    /**
     * Called by the IOScheduler when data can be written, that is,
     * when an isWritable() SelectionKey has been received.  Any 
     * data in the output buffer is written to the connection's 
     * channel.  If the write does not empty the output buffer, the
     * connection waits for another readyToWrite.  If the write
     * empties the output buffer, the listener is signaled using
     * its dataSent() method.  The listener then may disable
     * writing.
     */
    public void readyToWrite();

    // INTERFACE TO CONNECTIONLISTENER //////////////////////////////
    /**
     * Called by the ConnectionListener (or assignee) to signal that it 
     * is prepared to receive data.  Reading should be enabled on the 
     * connection's SelectionKey and any data in the input buffer should 
     * be processed just as when readyToRead() has been called.
     *
     * The ConnectionListener may delegate responsibility for making
     * this call to another program component, the assignee.
     */
    public void initiateReading();

    /**
     * Called by the ConnectionListener or assignee to initiate the 
     * sending of data.
     *
     * @param buffer holds data to be transmitted
     */
    public void sendData (ByteBuffer buffer);
}
