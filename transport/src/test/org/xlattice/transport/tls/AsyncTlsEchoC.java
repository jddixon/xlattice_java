/* AsyncTlsEchoC.java */
package org.xlattice.transport.tls;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.xlattice.transport.*;

/**
 * Echo any packets received back to the sender.  This handles the
 * client side of a connection. Clients send one or more messages, 
 * waiting for the reply to each before dispatching the next.
 * 
 * When disconnect is received, close.
 *
 * Test results are available in outIndex, inIndex, messages, replies.
 * 
 * @author Jim Dixon
 */
public class AsyncTlsEchoC      implements AsyncPacketHandler {

    protected    Timer  timer;
    protected    Random rng; // source of cheap randomness

    /** number of messages to send */
    public final int  N;        
    /** delay in ms before first message */
    public final long D;        
    /** period between messages, multiple of D */
    public final int  J;        

    /** index of last message sent */
    public       int  outIndex; 
    /** index of next reply to be received */
    public       int  inIndex;

    /** array of quasi-random messages sent */
    public final ByteBuffer[] messages;
    /** array of replies received */
    public final ByteBuffer[] replies;
    
    protected       AsyncTlsClientConnection cnx = null;
    protected       ByteBuffer outBuf;
    protected       ByteBuffer inBuf;

    private         boolean closed;

    /**
     *
     */
    public AsyncTlsEchoC(final long seed, 
                         final int n, final int delay, final int interval) {
        rng = new Random(seed);
        if (n < 1)
            N = 1;
        else
            N = n;
        if (delay < 1)
            D = 1;
        else 
            D = (long)delay;
        if (interval < 1)
            J = 1;
        else 
            J = interval;

        outIndex    = -1;
        inIndex     = 0;
        closed      = false;

        messages = new ByteBuffer[N];
        replies  = new ByteBuffer[N];
        // set up the N messages
        for (int i = 0; i < N; i++) {
            int len = 16 + rng.nextInt (129 - 16);  // so 16 to 128 
            messages[i] = ByteBuffer.allocate(len);
            rng.nextBytes( messages[i].array() );
            messages[i].position(0);
            messages[i].limit(len);
        }
        timer = new Timer();
        for (int i = 0; i < N; i++) {
            TimerTask sender = new MessageSender(messages[i]);
            timer.schedule( sender, D + i * D * J);
        }
    }
    protected class MessageSender extends TimerTask {
        private final ByteBuffer theBuf;
        public MessageSender(final ByteBuffer buf) {
            theBuf = buf;
        }
        public void run() {
            cnx.outBuf.put( theBuf );
            cnx.outBuf.flip();
            theBuf.rewind();
            cnx.sendData();
            outIndex++;                 // index of last message sent
            cnx.initiateReading();
        }
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
            timer.cancel();
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
        cnx = (AsyncTlsClientConnection) myCnx;
        if (outBuffer == null || inBuffer == null)
            throw new IllegalArgumentException("null ByteBuffer");
        outBuf = outBuffer;
        inBuf  = inBuffer;
    }
    public void dataSent() {
        /* nothing to do */
    }

    public void dataReceived() {
        ByteBuffer reply = ByteBuffer.allocate( inBuf.limit() );
        reply.put( inBuf );
        reply.flip();
        replies[ inIndex++ ] = reply;

        inBuf.clear();
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
