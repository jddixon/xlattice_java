/* PingPongPkt.java */
package org.xlattice.transport.udp;

/**
 * A simple ping/pong packet.
 *
 * +-+-+-+-+-+-+-+-+-+-+-+- - -+-+-+
 * |T|S|D|L| msgID |  byte[L] data |
 * +-+-+-+-+-+-+-+-+-+-+-+- - -+-+-+
 *
 * where T = type (PING or PONG), S is the source index, D is the
 * destination index, L is the length of the payload in bytes, 
 * msgID is a random 32-bit value, and data is the payload.
 */
public class PingPongPkt {

    /** packet types */
    public static final byte PING = 1;
    public static final byte PONG = 2;

    byte[] value;

    public PingPongPkt (byte type, byte src, byte dest, 
                                   int msgID, byte[] data) {
        if (type != PING && type != PONG)
            throw new IllegalArgumentException("unknown type " + type);
        if (src < 0) 
            throw new IllegalArgumentException("negative src index");
        if (dest < 0) 
            throw new IllegalArgumentException("negative dest index");
        if (data.length > 127) 
            throw new IllegalArgumentException (
                    "data length cannot exceed 127 but is " + data.length);
        value = new byte[ 8 + data.length ];
        value[0] = type;
        value[1] = src;
        value[2] = dest;
        value[3] = (byte) data.length;
        value[4] = (byte)(msgID >> 24);
        value[5] = (byte)(msgID >> 16);
        value[6] = (byte)(msgID >>  8);
        value[7] = (byte)(msgID      );
        System.arraycopy (data,  0, value, 8, data.length);
    }
    public PingPongPkt ( byte[] buffer ) {
        if (buffer[3] != buffer.length - 8)
            throw new IllegalArgumentException("packet has overall length of "
                + buffer.length + " but payload length of " + buffer[3]);
        if (buffer[0] != PING && buffer[0] != PONG)
            throw new IllegalArgumentException(
                "unrecognized packet type " + buffer[0]);
        value = buffer;
    }
    public void setType(byte n) { value[0] = n; }
    public void setSrc (byte n) { value[1] = n; }
    public void setDest(byte n) { value[2] = n; }
    
    public byte getType() { return value[0]; }
    public byte getSrc () { return value[1]; }
    public byte getDest() { return value[2]; }
    public byte getLen () { return value[3]; }
    
    public int getMsgID() {
        return  ( (0xff & (value[4])) << 24 ) |
                ( (0xff & (value[5])) << 16 ) |
                ( (0xff & (value[6])) <<  8 ) |
                ( (0xff & (value[7]))       ) ;
    }
    public byte[] getData () {
        byte[] data = new byte[getLen()];
        System.arraycopy ( value, 8, data, 0, getLen() );
        return data;
    }
}
