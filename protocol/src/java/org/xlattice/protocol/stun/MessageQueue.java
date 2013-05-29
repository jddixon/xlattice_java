/* MessageQueue.java */
package org.xlattice.protocol.stun;

import java.util.Random;

/**
 * A STUN message queue is initialized with one or nine times
 * representing the time at which copies of a message should be
 * sent and a timeout.  The message has a quasi-unique and 
 * quasi-random message ID.  If a response with the message ID has 
 * been received before timeout occurs, the queue is cleared.
 *
 * @author Jim Dixon
 */

public class MessageQueue {

    public final byte[] messageID;

    public MessageQueue ( StunMsg msg ) {
        /* WORKING HERE */
        messageID = msg.getMsgID();

    }
}
    
