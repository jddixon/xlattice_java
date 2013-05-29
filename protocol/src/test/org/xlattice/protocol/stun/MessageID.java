/* MessageID.java */
package org.xlattice.protocol.stun;

/**
 * Wrapper around a byte[] that has equals() and hashCode() 
 * methods; a convenience for testing.
 *
 * @author Jim Dixon
 */
public class MessageID {
    public byte[] id;
    public MessageID ( byte[] id ) {
        if (id.length != StunMsg.MSG_ID_LENGTH)
            throw new IllegalArgumentException(
                    "message ID has wrong length: " + id.length);
        this.id = (byte[])id.clone();
    }
    public boolean equals (Object o) {
        if (!(o instanceof MessageID)) {
            return false;
        }
        MessageID other = (MessageID)o;
        for (int i = 0; i < StunMsg.MSG_ID_LENGTH; i++) {
            if (id[i] != other.id[i]) {
                return false;
            }
        }
        return true;
    }
    // if this isn't capitalized correctly, hashtable retrievals fail
    public int hashCode() {
        return ( ((0xff & id[0]) << 24) | ((0xff & id[1]) << 16)
               | ((0xff & id[2]) <<  8) | ((0xff & id[3])      ));
    }
} 
