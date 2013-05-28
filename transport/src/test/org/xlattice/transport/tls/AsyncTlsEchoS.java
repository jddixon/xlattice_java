/* AsyncTlsEchoS.java */
package org.xlattice.transport.tls;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;

import org.xlattice.transport.*;

/**
 * Echo any packets received back to the sender.  This handles the
 * server side of a connection.  Expect to receive at least one message.
 * Clients send one or more messages, waiting for the reply to each
 * before dispatching the next.
 * 
 * When disconnect is received, close.
 *
 * @author Jim Dixon
 */
public class AsyncTlsEchoS      implements AsyncPacketHandler {

    protected       AsyncTlsServerConnection cnx = null;
    protected       ByteBuffer outBuf;
    protected       ByteBuffer inBuf;

    private         boolean closed;
    
    public AsyncTlsEchoS() {
        closed      = false;
    }
   
    public boolean isConfigured() {
        return cnx != null;
    }
    public boolean isClosed() {
        return closed;
    }
    public void close() {
        if (!closed) {
            try {
                cnx.close();
            } catch (IOException ioe) {
                System.out.println("unexpected " + ioe);
            }
            closed = true;
        }
    }
    // INTERFACE AsyncPacketHandler /////////////////////////////////
    public void setConnection(AsyncPacketConnection myCnx,
                            ByteBuffer outBuffer, ByteBuffer inBuffer) {
        if (cnx != null)
            throw new IllegalArgumentException(
                    "connection has already been set");
        if (myCnx == null)
            throw new IllegalArgumentException("null connection");
        cnx = (AsyncTlsServerConnection) myCnx;
        if (outBuffer == null || inBuffer == null)
            throw new IllegalArgumentException("null ByteBuffer");
        outBuf = outBuffer;
        inBuf  = inBuffer;
    }
    public void dataSent() {
        /* nothing to do */
    }

    public void dataReceived() {
        outBuf.clear();
        outBuf.put( inBuf );
        outBuf.flip();
        inBuf.clear();
        cnx.sendData();
        cnx.initiateReading();
    }

    /** normal termination */
    public void reportDisconnect() {
        close();
    }
    /** abnormal event */
    public void reportException(Exception exc) {
        System.out.println("exception reported: " + exc);
    }
    
}
