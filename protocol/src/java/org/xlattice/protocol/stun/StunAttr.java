/* StunAttr.java */
package org.xlattice.protocol.stun;

import java.io.IOException;

import org.xlattice.protocol.TLV16;
import org.xlattice.util.StringLib;
import org.xlattice.util.UIntLib;

/**
 *
 * @author Jim Dixon
 */
public abstract class StunAttr extends TLV16 implements StunConst {

    public StunAttr (int type, byte[]value) {
        super (type, value);

    }

    public static TLV16 decode (byte[] inBuf, int offset) 
                                                throws IOException {
        if (inBuf == null)
            throw new IllegalArgumentException("null in buffer");
        if (inBuf.length < offset + 4)
            throw new IllegalStateException("in buffer too short: "
                    + inBuf.length);
        int _type   = UIntLib.decodeUInt16(inBuf, offset);
        offset += 2;
        int _length = UIntLib.decodeUInt16(inBuf, offset);
        offset += 2;
        if (_length < 0) 
            throw new IllegalStateException("negative attribute length");
        // XXX CHECK MULTIPLE OF 4, ETC
//      // DEBUG
//      System.out.println("StunAttr.decode: offset " + (offset - 4)
//          + ", type " + _type + ", length " + _length + "\n" +
//          StringLib.byteArrayToHex(inBuf, offset - 4, _length) );
//      // END

        switch (_type) {
            case MAPPED_ADDRESS: 
            case RESPONSE_ADDRESS:
            case SOURCE_ADDRESS:
            case CHANGED_ADDRESS:
            case REFLECTED_FROM:

            // Extensions, not in the RFC.
            case XOR_MAPPED_ADDRESS:
            case SECONDARY_ADDRESS:
                if (_length != 8)
                    throw new IllegalStateException (
                        "address attribute length must be 8, is " 
                        + _length);
                return AddrAttr.decodeValue(_type, inBuf, offset);
            
            case CHANGE_REQUEST:
            case USERNAME:
            case PASSWORD:
            // Another extension, not in the RFC.
            case SERVER_NAME:

            // XXX Needs better handling
            case MESSAGE_INTEGRITY:
                return ValueAttr
                            .decodeValue(_type, _length, inBuf, offset);

            case ERROR_CODE:
                return ErrorCode
                            .decodeValue(_length, inBuf, offset);

            case UNKNOWN_ATTRIBUTES:
                return UnknownAttributes
                            .decodeValue(_length, inBuf, offset);

            default:
                // FOR TESTING: Bad and IgnoredAttr
                if ( (0x7000 <= _type && _type < 0x7010)
                        || (_type >= 0x8000) )
                    return ValueAttr
                            .decodeValue(_type, _length, inBuf, offset);
                // END TESTING
                throw new IllegalStateException(
                        "unrecognized StunAttr type " + _type);
        }
    } 
}
