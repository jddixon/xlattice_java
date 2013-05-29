/* ValueAttr.java */
package org.xlattice.protocol.stun;

/**
 * Superclass for simple attributes that just carry byte arrays 
 * whose size must be a multiple of four.
 *
 * @author Jim Dixon
 */
public abstract class ValueAttr extends StunAttr {

    public ValueAttr( int type, byte[] value) {
        super(type, value);

        // XXX EXCEPTION if not word-aligned.
    }

    /**
     * Type and length have already been decoded.  The offset
     * is to the data structure depicted above.
     */
    protected static ValueAttr decodeValue (int type, int length, 
                                     byte[] message, int offset) {
        byte[] value = new byte[length];
        System.arraycopy (message, offset, value, 0, length);
        switch (type) {
            case CHANGE_REQUEST:
                if (length != 4)
                    throw new IllegalStateException(
                            "illegal value length in ChangeRequest: "
                            + length);
                int which = 
                    ((0xff & value[0]) << 24) | ((0xff & value[1]) << 16) |
                    ((0xff & value[2]) <<  8) | ((0xff & value[3])      ) ;
                return new ChangeRequest(which);
            case USERNAME:
                return new UserNameAttr (value);
            case PASSWORD:
                return new PasswordAttr (value);
            case SERVER_NAME:
                return new ServerNameAttr (value);
            // XXX NEEDS TO BE HANDLED DIFFERENTLY, because validation
            // requires access to the 'password' and the entire message
            case MESSAGE_INTEGRITY:
                return new MessageIntegrity (value);
            default:
                // THESE ARE FOR TESTING
                if (0x7000 <= type && type < 0x7010)
                    return new BadAttr(type, value);
                if (0x8000 <= type)
                    return new IgnoredAttr(type, value);
                throw new IllegalStateException(
                   "unknown value attribute " + type);
        }
    }
}
