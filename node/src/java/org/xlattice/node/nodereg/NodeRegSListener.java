/* NodeRegSListener.java */
package org.xlattice.node.nodereg;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @author Jim Dixon
 */

import org.xlattice.crypto.RSAPublicKey;
import org.xlattice.crypto.SHA1withRSAVerifier;
import org.xlattice.transport.ConnectionListener;
import org.xlattice.transport.SchedulableConnection;
import org.xlattice.util.NonBlockingLog;

/**
 * NodeReg connection listener, attached to each new Connection
 * by the Acceptor.  This is a listener for a one-shot server.
 * That is, it accepts data from the connection, writes a reply, and
 * closes the connection.
 *
 * NodeReg protocol:
 *
 * CLIENT SENDS (msg 0)
 *   "reg" CRLF
 *   base64-encoded public key CRLF (may be folded)
 *   EndPoint.toString() CRLF
 *   base64-encoded digital signature CRLF
 *
 * SERVER REPLIES 
 *   IF SUCCESSFUL (msg 1)
 *     "ok" CRLF
 *     base64-encoded node ID CRLF
 *     Timestamp.toString() CRLF
 *     base64-encoded digital signature CRLF
 *
 *   ELSE ERROR (msg 2)
 *     "err" CRLF
 *     byte[] errorDescription CRLF
 *     Timestamp.toString() CRLF
 *     base64-encoded digital signature CRLF
 *
 * In each case the digital signature covers all preceding lines
 * including the CRLFs.
 *
 * For msg 0 the digital signature is calculated using the RSA key 
 * whose public key counterpart is on the second line of the message.
 *
 * For the other two messages (msg 1 and msg 2) the digital signature
 * is calculated using the server's well-known private RSA key, which
 * in production use will be nodereg.xlattice.org's private key.
 */
public class NodeRegSListener implements ConnectionListener {

    public static final int NODE_REG_SERVER_PORT = 9999;

    private static NonBlockingLog regLog 
                        = NonBlockingLog.getInstance("nodereg.reg.log");
    private void logReg(String s) {
        regLog.message(s);
    }
    private static NonBlockingLog errLog 
                        = NonBlockingLog.getInstance("nodereg.err.log");
    private void logErr(String s) {
        errLog.message(s);
    }
    // DEBUG
    private static NonBlockingLog debugLog 
                        = NonBlockingLog.getInstance("junk.nodereg.");
    private void logMsg(String s) {
        debugLog.message(s);
    }
    // END
    private       SchedulableConnection cnx;
    private       ByteBuffer dataIn;
    private final ByteBuffer dataOut
                        = ByteBuffer.allocate(1024);  // arbitrary size

    private       InetAddress clientHost;
    private       int         clientPort;
    private       MsgParser   parser;

    public NodeRegSListener() {}

    // INTERFACE ConnectionListener /////////////////////////////////
    public void setConnection (SchedulableConnection cnx, ByteBuffer buffer) {
        if (cnx == null || buffer == null) {
            logMsg("NodeRegSListener.setConnection(): null cnx or buffer");
            throw new IllegalArgumentException ("null cnx or buffer");
        }
        this.cnx = cnx;
        SocketChannel sChan = (SocketChannel)cnx.getChannel();
        logMsg("NodeRegSListener.setConnection(), channel "
                + sChan.hashCode()); 
        Socket sock = sChan.socket();
        clientHost  = sock.getInetAddress();
        clientPort  = sock.getPort();
        dataIn = buffer;
        parser = new MsgParser (dataIn, dataOut, clientHost, clientPort);    

        // apply any blacklist to this client 
        // XXX missing code (if on list, call _close()) else:
        cnx.initiateReading();
    }
    /**
     * The ByteBuffer contains some data; echo it back.  
     */
    public void dataReceived () {
        logMsg("NodeRegSListener.dataReceived()");             // DEBUG
        dataIn.flip();
        ByteBuffer out = parser.parse ();
        if (out == null) {
            _close();
        } else {
            out.flip();                 // prepare for write
            cnx.sendData (out);         // send the reply
        }
    }
    public void dataSent () {
        logMsg("NodeRegSListener.dataSent()");                 // DEBUG
        _close();               // the connection
    }
    public void reportDisconnect () {
        logErr("UNEXPECTED DISCONNECTION");                 // DEBUG
        _close();
    }
    public void reportException (Exception exc) {
        logErr("UNEXPECTED REPORTED EXCEPTION: " + exc);    // DEBUG
        _close();
    }
    // OTHER METHODS ////////////////////////////////////////////////
    private void _close() {
        try { 
            cnx.getChannel().close();   // cancels the key
        } catch (IOException e) { /* ignore */ }
        
    }
    // IMPLEMENTATION ///////////////////////////////////////////////
}
