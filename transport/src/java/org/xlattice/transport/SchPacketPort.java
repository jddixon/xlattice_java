/* SchPacketPort.java */
package org.xlattice.transport;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

import org.xlattice.Address;

/**
 * Interface to a non-blocking PacketPort.
 * 
 * @author Jim Dixon
 */

public interface SchPacketPort {

    // PROPERTIES ///////////////////////////////////////////////////
    public IOScheduler getScheduler();
    public void  setScheduler(IOScheduler scheduler);
    
    public PacketPortListener getListener();
    public void setListener(PacketPortListener ppl);

    /**
     * Used to get SelectionKey so must be available on this 
     * interface.
     */
    public SelectableChannel getChannel();

    /**
     * Used by IOScheduler to set the key.
     */
    public void setKey(SelectionKey key);
    
    public SelectionKey  getKey();
    
    // INTERFACE TO IOScheduler /////////////////////////////////////
    /**
     * Called by the IOScheduler  when data has been received, that
     * is, when an isReadable() SelectionKey has been received.
     *
     * XXX CONFIRM THIS DESCRIPTION:
     * The input buffer is checked.  If a read returns -1, the
     * PacketPort has been closed and the PacketPortListener's
     * reportDisconnect() method should be called.  If the read
     * returns 0, the call should be silently ignored.  Any other
     * read value should result in a call to the listener's
     * dataReceived() method.
     */
    public void readyToRead();
    
    /**
     * Called by the IOScheduler when data can be written, that is,
     * when an isWritable() SelectionKey has been received.  
     *
     * XXX CONFIRM THIS DESCRIPTION:
     * Any data in the output buffer is written to the PacketPort's
     * channel.  If the write does not empty the output buffer, the
     * PacketPort waits for another readyToWrite.  If the write
     * empties the output buffer, the listener is signaled using
     * its dataSent() method.  The listener then may disable
     * writing.
     */
    public void readyToWrite();
    
    // INTERFACE TO PacketPortListener //////////////////////////////
    /**
     * Called by the PacketPortListener (or assignee) to signal that it 
     * is prepared to receive data.  Reading should be enabled on the 
     * PacketPort's SelectionKey and any data in the input buffer should 
     * be processed just as when readyToRead() has been called.
     *
     * The PacketPortListener may delegate responsibility for making
     * this call to another program component, the assignee.
     *
     * In some protocols, this will be called once and only once 
     * by PacketPortListener.setPacketPort().  In such protocols, 
     * the port is always ready to receive packets.
     */
    public void initiateReading();

    /**
     * Called by the PacketPortListener or assignee to initiate the 
     * sending of data to a PacketPort.
     */
    public void sendData (ByteBuffer outBuf);
}
