/* TLV.java */
package org.xlattice.protocol;

import java.io.IOException;
import org.xlattice.util.UIntLib;

/**
 * Represents the classic Type-Length-Value field found in many
 * network protocols.
 *
 * This is an experiment; the implementation may change drastically
 * at a later date.
 * 
 * @author Jim Dixon
 */

public abstract class TLV {

    // INSTANCE VARIABLES ///////////////////////////////////////////
    public final int fieldSize;
    
    public final int type;
    /** in bytes */
    private       int length;    
    
    public final byte[] value;

    // CONSTRUCTORS /////////////////////////////////////////////////
    protected TLV (int fieldSize, int type, int length, byte[] value) {
        if (fieldSize != 1 && fieldSize != 2)
            throw new IllegalArgumentException (
                    "type and length fields must be 1 or 2 bytes but width is "
                    + fieldSize);
        this.fieldSize = fieldSize;
        if (type < 0)
            throw new IllegalArgumentException("negative type: " + type);
        this.type = type;
        if (value == null || value.length == 0) 
            throw new IllegalArgumentException("null or empty value array");
        this.value = value;
        if (length != value.length) {
            // XXX bit of a hack
            throw new IllegalArgumentException(
                    "length specified is different from length of value");
        }
        this.length = length;
    }
    protected TLV (int fieldSize, int type, byte[] value) {
        this(fieldSize, type, value.length, value);
    }
    // ACCESS METHODS ///////////////////////////////////////////////
    public int length() {
        return length;
    }
    // OTHER METHODS ////////////////////////////////////////////////
    protected static TLV decode (int fieldSize, byte [] message, int offset)
                                                throws IOException {
        if (message == null)
            throw new IllegalArgumentException("null message");
        if (offset < 0 || offset > (message.length + 2 * fieldSize))
            throw new IllegalArgumentException( "out of range: offset of " 
                + offset + " into buffer of length " + (message.length) );
        // get big-endian type and length;
        int type;
        int len;
        if (fieldSize == 1) {
            type = message[offset++];
            if (type < 0) 
                type += 256;
            len  = message[offset++];
            if (len < 0) 
                len += 256;
            
        } else if (fieldSize == 2) {
            type = UIntLib.decodeUInt16(message, offset);
            offset += 2;
            len  = UIntLib.decodeUInt16(message, offset);
            offset += 2;
        } else {
            throw new IllegalArgumentException(
                    "unsupported type TLV" + (8 * fieldSize) );
        }
        // offset now points to beginning of value
        if (message.length < offset + len) {
            throw new IllegalStateException(
                "TLV in buffer of length " + message.length 
              + " but offset of value is " + offset 
              + " and length is " + len);
        }
        byte[] val = new byte[len];
        System.arraycopy(message, offset, val, 0, len);
        if (fieldSize == 2)
            return new TLV16 (type, len, val);
        else 
            return null;        // XXX BIT SLOPPY, THIS
        
    }
    /** 
     * Write this TLV onto the message buffer at the offset indicated.
     * 
     * XXX Assumes value.length == length 
     *
     * @param message buffer to write TLV on
     * @param offset  byte offset where we start writing
     * @return offset after writing the values
     * @throws IndexOutOfBoundsException, NullPointerException
     */
    public int encode (byte[] message, int offset) {
        int where = offset;
        if (fieldSize == 1) {
            message[where++] = (byte) type;
            message[where++] = (byte) length;
        } else if (fieldSize == 2) {
            where = UIntLib.encodeUInt16(type,   message, where);
            where = UIntLib.encodeUInt16(length, message, where);
        } else {
            throw new IllegalStateException("not supported: fieldSize = "
                    + fieldSize);
        }
        System.arraycopy (value, 0, message, where, length);
        return where + length;
    }
}
