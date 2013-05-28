/* MockSchConnection.java */
package org.xlattice.transport.mockery;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Jim Dixon
 **/

import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.xlattice.Connection;
import org.xlattice.CryptoException;
import org.xlattice.EndPoint;
import org.xlattice.Key;
import org.xlattice.PublicKey;
import org.xlattice.transport.ConnectionListener;
import org.xlattice.transport.IOScheduler;
import org.xlattice.transport.SchedulableConnection;

public class MockSchConnection implements SchedulableConnection {

    int state;
    boolean blocking = false;           // A HACK
    
    SocketChannel sChan;
    IOScheduler scheduler;
    ConnectionListener listener;
    SelectionKey key;
    
    boolean closed;
    boolean encrypted;
    ByteBuffer dataReceived;
    ByteBuffer dataSent;
    
    EndPoint nearEnd;
    EndPoint farEnd;
    
    public MockSchConnection (SocketChannel sChan, 
                              IOScheduler scheduler,
                              ConnectionListener listener) {
        this.sChan     = sChan;
        this.scheduler = scheduler;
        this.listener  = listener;
        dataReceived   = ByteBuffer.allocate(512);
        listener.setConnection (this, dataReceived);

        state = UNBOUND;
    }
    // XXX SHOULD COPY THIS DATA
    public void fakeDataIn (ByteBuffer inBuf) {
        dataReceived.put(inBuf);
    }
    public ByteBuffer getResults () {
        return dataSent;
    }
    // INTERFACE SchedulableConnection //////////////////////////////

    // PROPERTIES /////////////////////////////////////////
    // XXX NEED A MOCK SCHEDULER??
    public IOScheduler getScheduler() {
        return scheduler;
    }
    // XXX NEED A MOCK CHANNEL
    public SelectableChannel getChannel() {
        return sChan;
    }
    // XXX NEED A MOCK SELECTION KEY
    public SchedulableConnection setKey (SelectionKey key) {
        this.key = key;
        return this;
    }
    public SelectionKey getKey() {
        return key;
    }
    // INTERFACE USED BY IOSCHEDULER //////////////////////
    /**
     * Called by the IOScheduler when data has been received, that
     * is, when an isReadable SelectionKey has been received.
     */
    public void readyToRead() {
        if (dataReceived.array().length == 0) {
            return;
        }
        handleDataIn();
    }

    /**
     * Called by the IOScheduler when data can be written, that is,
     * when an isWritable() SelectionKey has been received.
     */
    public void readyToWrite() {
    }
    // DUMMY INTERNAL METHODS /////////////////////////////
    private void handleDataIn() {
        listener.dataReceived();        
    }
    private void handleDataOut() {

    }
    // INTERFACE USED BY CONNECTION LISTENER //////////////
    /**
     * Called by the ConnectionListener to initiate the reading of
     * data.  Sets the SelectionKey OP_READ flag.
     */
    public void initiateReading() {
    }
    /**
     * Called by the ConnectionListener to initiate the sending of
     * data.
     */
    public void sendData (ByteBuffer buffer) {
        dataSent = buffer;
    }
    // INTERFACE Connection /////////////////////////////////////////
    // STATE //////////////////////////////////////////////
    public void bindNearEnd (EndPoint p) {
        // STUB
        state = BOUND;
    }
    public void bindFarEnd (EndPoint p) {
        // STUB
        state = CONNECTED;
    }
    public int getState() {
        return state;
    }
    public void close () {
        closed = true;
        state = DISCONNECTED;
    }
    public boolean isClosed () {
        return state == DISCONNECTED;
    }
    // END POINTS /////////////////////////////////////////
    public EndPoint getNearEnd() {
        return nearEnd;
    }
    public EndPoint getFarEnd() {
        return farEnd;
    }
    // I/O ////////////////////////////////////////////////
    public boolean isBlocking() {
        return blocking;
    }
    public InputStream getInputStream()         throws IOException {
        if (blocking)
            return sChan.socket().getInputStream();
        else
            throw new UnsupportedOperationException();
    }
    public OutputStream getOutputStream()       throws IOException {
        if (blocking)
            return sChan.socket().getOutputStream();
        else
            throw new UnsupportedOperationException();
    } 
        
    // ENCRYPTION /////////////////////////////////////////
    /** @return whether the connection is encrypted */
    public boolean isEncrypted () {
        return encrypted;
    }
    public void negotiate (Key myKey, PublicKey hisKey)
                                            throws CryptoException {
        // STUB
    }
    
}
