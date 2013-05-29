/* MessageID.java */
package org.xlattice.protocol.xl;

import org.xlattice.util.StringLib;

/**
 * A 12-byte array used to identify XL messages.
 *
 * @author Jim Dixion
 */

public class MessageID                      implements XLConst {
    public final byte[] id;
    public MessageID(byte[] m) {
        if (m.length != MSG_ID_LENGTH)
            throw new IllegalArgumentException(
                    "MessageID length wrong: " + m.length);
        id = m;
    }
    public boolean equals(Object o) {
        if (o == null || !(o instanceof MessageID))
            return false;
        MessageID other = (MessageID)o;
        for (int i = 0; i < MSG_ID_LENGTH; i++)
            if (id[i] != other.id[i])
                return false;
        return true;
    }
    public int hashCode() {
        return ( ((0xff & id[0]) << 24) | ((0xff & id[1]) << 16)
               | ((0xff & id[2]) <<  8) | ((0xff & id[3])      ));
    }
    public String toString() {
        return new StringBuffer("msgID:")
            .append(StringLib.byteArrayToHex(id))
            .toString();
    }
} 
