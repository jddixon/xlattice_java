/* AsyncTlsConnection.java */
package org.xlattice.transport.tls;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.*;
import static javax.net.ssl.SSLEngineResult.HandshakeStatus.*;
import javax.net.ssl.*;

import java.nio.*;

import org.xlattice.crypto.tls.TlsConst;
import org.xlattice.crypto.tls.TlsEngine;
import org.xlattice.crypto.tls.TlsSession;
import org.xlattice.transport.AsyncPacketConnection;
import org.xlattice.transport.AsyncPacketHandler;
import org.xlattice.transport.ConnectionListener;
import org.xlattice.transport.SchedulableConnection;
import org.xlattice.transport.tcp.SchedulableTcpConnection;

/**
 *
 * @author Jim Dixon
 */
public abstract class AsyncTlsConnection  
            implements AsyncPacketConnection, ConnectionListener, TlsConst {

    // INSTANCE VARIABLES ///////////////////////////////////////////
   
    private         boolean closed;
    protected       AsyncPacketHandler handler;
    protected       ByteBuffer outBuf;
    protected       ByteBuffer inBuf;
    
    // UNDERLYING TCP CONNECTION /////////////////////////
    protected       SchedulableTcpConnection tcpCnx;
    protected       ByteBuffer tcpInBuf;
    
    // TlsEngine /////////////////////////////////////////
    protected final TlsSession tlsSession;
    protected       TlsEngine engine;
    public abstract boolean isClient();

    protected AsyncTlsConnection(TlsSession session)
                            throws GeneralSecurityException, IOException {
        if (session == null)
            throw new IllegalArgumentException("null TLS config");
        tlsSession = session;
    }
    // INTERFACE AsyncPacketConnection //////////////////////////////
    public void close()                         throws IOException {
        closed = true;
        /* STUB */
    }
    public boolean isClosed() {
        return closed;
    }
    
    public AsyncPacketHandler getPacketHandler() {
        /* STUB */
        return null;
    }
    public ByteBuffer getOutBuffer() {
        return outBuf;
    }

    public ByteBuffer getInBuffer() {
        return inBuf;
    }
    
    public void sendData() {
        /* STUB */
    }

    public void initiateReading() {
        /* STUB */
    }
    // INTERFACE ConnectionListener /////////////////////////////////

    /**
     * Method setConnection() in subclass should invoke this method.
     */
    protected void setCnx (SchedulableConnection cnx, ByteBuffer buffer) {
        if (cnx == null) 
            throw new IllegalArgumentException ("null TCP connection");
        tcpCnx = (SchedulableTcpConnection)cnx;
        if (buffer == null)
            throw new IllegalArgumentException("null TCP input buffer");
        tcpInBuf = buffer;
    }
    /**
     * Reports to that a data transmission has been completed on the
     * underlying TCP connection.
     */
    public void dataSent () {
        /* STUB */
    }

    /**
     * Reports that some data has been received on the underlying
     * TCP connection.  This method must NOT be invoked if zero bytes 
     * were received.  If a complete message has been received, 
     * signal the handler, transferring control of the input buffer
     * back to it.  Otherwise, ask the TCP connection for more data.
     */
    public void dataReceived () {
        /* STUB */
    }

    /**
     * Reports that the underlying TCP connection has been closed at the far 
     * end.  Free up any allocated resources, mark the TLS connection as 
     * closed.
     */
    public void reportDisconnect () {
        /* STUB */
    }
    /**
     * Report an exception.  This may result in the connection being
     * closed, or it may cause an error message to be sent to the 
     * other end of the connection.
     */
    public void reportException (Exception exc) {
        /* STUB */
    }
}

