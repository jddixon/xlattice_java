/* UnknownAttributes.java */
package org.xlattice.protocol.stun;

import org.xlattice.protocol.TLV16;
import org.xlattice.util.UIntLib;

/**
 * STUN attribute carrying a list of unknown attributes.
 * In bytes the value part looks like
 *
 *   +---+---+---+---+
 *   |   a0  |   a1  |
 *   +---+---+---+---+
 *   |   a2  |   a3  |
 *   +---+---+---+---+
 *   |  ....
 *   +---+---
 * 
 * where the a-sub-i represent big-endian UInt16 attribute values.
 * If there are an odd number of such values, as will normally be
 * the case, the last or only value is duplicated.  If there is 
 * only one value, a0, then the data sent is 
 * 
 *   +---+---+---+---+
 *   |   a0  |   a0  |
 *   +---+---+---+---+
 *
 * @author Jim Dixon
 */
public class UnknownAttributes extends StunAttr {

    public final static byte[] makeList(int[] attrs) {
        if (attrs == null || attrs.length == 0)
            throw new IllegalArgumentException(
                    "null or empty attribute value array");
        int len = attrs.length;
        boolean odd = false;
        if ((len % 2) != 0) {
            odd = true;
            len++;
        }
        byte[] value = new byte[len * 2];
        int i, attr;
        int offset = 0;
        for (i = 0; i < attrs.length; i++) {
            attr = attrs[i];
            value[offset++] = (byte)((0xff & attr) << 8);
            value[offset++] = (byte) attr;
        }
        if (odd) {
            attr = attrs[--i];
            value[offset++] = (byte)((0xff & attr) << 8);
            value[offset++] = (byte) attr;
        }
        return value;
    }
    // INSTANCE VARIABLES ///////////////////////////////////////////
    public final int[] attrs;
    
    // CONSTRUCTORS /////////////////////////////////////////////////
    public UnknownAttributes(int[] attrs) {
        super(UNKNOWN_ATTRIBUTES, makeList(attrs));
        if ((attrs.length % 2) == 0) {
            this.attrs = attrs;
        } else {
            int len = attrs.length;
            int[] evenAttrs = new int[ len + 1 ];
            System.arraycopy(attrs, 0, evenAttrs, 0, len);
            evenAttrs[len] = attrs[len - 1];
            this.attrs = evenAttrs;
        }
    }
    // SERIALIZATION ////////////////////////////////////////////////
    /**
     * Type and length have already been decoded.  The offset
     * is to the data structure depicted above.
     */
    protected static TLV16 decodeValue (int length, 
                                     byte[] message, int offset) {
        if ( length <= 0 || ((length % 4) != 0) 
                || (offset + length > message.length) )
            throw new IllegalArgumentException("length out of range: "
                + length);
        // XXX NO DETECTION OF DUPLICATED VALUES
        int count = length / 4;
        int[] attrVals = new int[count];
        
        for (int i = 0; i < count; i++) {
            attrVals[i] = UIntLib.decodeUInt16(message, offset);
            offset += 4;
        }
        return new UnknownAttributes (attrVals);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("UnknownAttributes:        ");
        int i;
        for (i = 0; i < attrs.length - 2;  )
            sb.append( attrs[i++] )
              .append(",")
              .append( attrs[i++] )
              .append(",");
        sb.append( attrs[i++] )
          .append(",")
          .append( attrs[i++] );
        return sb.toString();
    }
}
