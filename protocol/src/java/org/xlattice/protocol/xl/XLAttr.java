/* XLAttr.java */
package org.xlattice.protocol.xl;

import java.io.IOException;

import org.xlattice.protocol.TLV16;
import org.xlattice.util.StringLib;
import org.xlattice.util.UIntLib;

/**
 *
 * @author Jim Dixon
 */
public abstract class XLAttr extends TLV16 implements XLConst {

    public XLAttr (int type, byte[]value) {
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
//      System.out.println("XLAttr.read: offset " + (offset - 4)
//          + ", type " + _type + ", length " + _length + "\n" +
//          StringLib.byteArrayToHex(inBuf, offset - 4, _length) );
//      // END

        switch (_type) {
            case SOURCE:
            case DESTINATION:
                return ValueAttr.readValue(_type, _length, inBuf, offset);

//          case ERROR_CODE:
//              return ErrorCode.readValue(_length, inBuf, offset);

            default:
                throw new IllegalStateException(
                        "unrecognized XLAttr type " + _type);
        }
    } 
}
