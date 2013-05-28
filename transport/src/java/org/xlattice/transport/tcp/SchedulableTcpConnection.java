/* SchedulableTcpConnection.java */
package org.xlattice.transport.tcp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import java.io.InputStream;     // HACK
import java.io.OutputStream;    // HACK

import org.xlattice.Connection;
import org.xlattice.CryptoException;
import org.xlattice.EndPoint;
import org.xlattice.Key;
import org.xlattice.PublicKey;
import org.xlattice.transport.ConnectionListener;
import org.xlattice.transport.IOScheduler;
import org.xlattice.transport.SchedulableConnection;

import org.xlattice.util.NonBlockingLog;    // DEBUG

/**
 *
 * This implementation restricts a connection to having only one
 * SelectionKey and only one IOScheduler.  This does not seem to be
 * unreasonable.
 *
 * @author Jim Dixon
 */
public class SchedulableTcpConnection implements SchedulableConnection {

    // DEBUG
    private final NonBlockingLog debugLog 
                            = NonBlockingLog.getInstance("debug.log");
    private void DEBUG_MSG(String msg) {
        debugLog.message("SchedTcpCnx" + msg);
    }
    // END
     
    /////////////////////////////////////////////
    // XXX KNOWN TO CAUSE PROBLEMS //////////////
    public final static int CNX_BUFSIZE = 65536;
    // XXX SHOULD THIS BE IN THE PARENT CLASS?
    /////////////////////////////////////////////

    // assigned by the constructor and final, so safe
    public  final SocketChannel      sChan;
    private final ByteBuffer         inBuffer;
    private       ByteBuffer         outBuffer;
    private final IOScheduler        scheduler;

    // possibly assigned later and so not final and not safe
    private       ConnectionListener listener;
    private       TcpConnection      tcpConnection;
    private       boolean            blocking;   // false by default
    // assigned later and so not final and not safe
    private     SelectionKey sKey;

    // CONSTRUCTORS /////////////////////////////////////////////////
    // XXX Dropped because no way to assign IOScheduler
//  public SchedulableTcpConnection (SocketChannel sChan)
//                                              throws IOException {
//      this(sChan, null, null);
//  }
    public SchedulableTcpConnection (
            SocketChannel sChan,
            IOScheduler scheduler,
            ConnectionListener listener)        throws IOException {
        if (sChan == null)
            throw new IllegalArgumentException ("null socket channel");
        this.sChan     = sChan;

        // XXX either of these may be null
        this.scheduler = scheduler;
        this.listener  = listener;

        // XXX why do this?
        // tcpConnection = new TcpConnection (sChan.socket());

        inBuffer = ByteBuffer.allocate(CNX_BUFSIZE);
    }
    // INTERFACE SchedulableConnection //////////////////////////////

    public SelectableChannel getChannel() {
        return sChan;
    }
    /**
     * Set the connection's SelectionKey, which actually starts
     * the I/O running.
     */
    public SchedulableConnection setKey (SelectionKey key) {
        if (sKey != null)
            throw new IllegalStateException("key has already been set");
        // DEBUG_MSG(".setKey() for channel " + sChan.hashCode());
        sKey = key;
        listener.setConnection (this, inBuffer);
        return this;
    }
    public SelectionKey getKey() {
        return sKey;
    }
    public IOScheduler getScheduler() {
        return scheduler;
    }
    // INTERFACE TO ConnectionListener ////////////////////
    public void initiateReading () {
        DEBUG_MSG(".initiateReading(), set OP_READ, chan "
                + sChan.hashCode());
        sKey.interestOps(SelectionKey.OP_READ);
        // could call processInBuffer() here
    }
    public void sendData (ByteBuffer outBuffer) {
        DEBUG_MSG(".sendData(), set OP_WRITE, chan " + sChan.hashCode());
        this.outBuffer = outBuffer;
        sKey.interestOps(SelectionKey.OP_WRITE);
        // could call processOutBuffer() here
        processOutBuffer();                 // 2005-03-06 experiment
    }
    // INTERFACE TO IOScheduler ///////////////////////////
    public void readyToRead() {
        // DEBUG_MSG(".readyToRead(), channel "
        //        + sChan.hashCode());
        processInBuffer(); 
    }
    public void readyToWrite() {
         DEBUG_MSG(".readyToWrite(), channel " + sChan.hashCode());
        processOutBuffer();
        
    }
    // IMPLEMENTATION ///////////////////////////////////////////////
    private void processInBuffer() {
        DEBUG_MSG(".processInBuffer(), channel " + sChan.hashCode());
        int count = 0;
        try {
            count = sChan.read(inBuffer);
        } catch (IOException ioe) {
            listener.reportException (ioe);
        }
        // what follows gives us an infinite loop 
//      DEBUG_MSG(".processInBuffer(): read count = " + count);
//      if (count == 0) {
//          // DEBUG_MSG (".processInBuffer(): setting OP_READ again");
//          sKey.interestOps(SelectionKey.OP_READ);
//          return;
//      }
        if (count == -1)
            listener.reportDisconnect();
        else
            listener.dataReceived();
    }
    private void processOutBuffer() {
        DEBUG_MSG(".processOutBuffer()");
        int count = 0;
        try {
            count = sChan.write(outBuffer);
        } catch (IOException ioe) {
            listener.reportException (ioe);
        }
        DEBUG_MSG(".processOutBuffer: wrote " + count + " bytes");
        if (outBuffer.hasRemaining()) {
            DEBUG_MSG(".processOutBuffer: " 
                    + (outBuffer.remaining()) + " more bytes to send");
            return;
        } else {
            sKey.interestOps(0);
            DEBUG_MSG(".processOutBuffer: calling dataSent()");
            listener.dataSent();
        }
    }
    // INTERFACE Connection /////////////////////////////////////////
    // STATE //////////////////////////////////////////////
    
    public int getState () {
        // XXX STUB
        return Connection.UNBOUND;
    }
    public void bindNearEnd (EndPoint p)    throws IOException {
        // STUB
    }
    public void bindFarEnd (EndPoint p)     throws IOException {
        // STUB
    }
    /**
     * Closes the SocketChannel and cancels the SelectionKey.
     */
    public void close () {
        try {
            sChan.close();
        } catch (IOException ioe) { 
            /* ignore */
        } finally {
            sKey.cancel();
        }
    }
    public boolean isClosed () {
        return !sChan.isOpen();
    }
    // END POINTS /////////////////////////////////////////
    public EndPoint getNearEnd() {
        return tcpConnection.getNearEnd();
    }
    public EndPoint getFarEnd() {
        return tcpConnection.getFarEnd();
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
    /**
     * @return whether the connection is encrypted
     */
    public boolean isEncrypted () {
        // XXX STUB
        return false;
    }
    /**
     * (Re)negotiate the secret used to encrypt traffic over the
     * connection.
     *
     * @param myKey  this Node's asymmetric key
     * @param hisKey Peer's public key
     */
    public void negotiate (Key myKey, PublicKey hisKey)
                                            throws CryptoException {
        // XXX STUB
    }
}

