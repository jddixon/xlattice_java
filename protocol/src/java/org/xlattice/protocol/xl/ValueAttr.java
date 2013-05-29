/* ValueAttr.java */
package org.xlattice.protocol.xl;

/**
 * Superclass for simple attributes that just carry byte arrays 
 * whose size must be a multiple of four.
 *
 * @author Jim Dixon
 */
public abstract class ValueAttr extends XLAttr {

    public ValueAttr( int type, byte[] value) {
        super(type, value);

        // XXX EXCEPTION if not word-aligned.
    }

    /**
     * Type and length have already been decoded.  The offset
     * is to the data structure depicted above.
     */
    protected static ValueAttr readValue (int type, int length, 
                                     byte[] message, int offset) {
        byte[] value = new byte[length];
        System.arraycopy (message, offset, value, 0, length);
        switch (type) {
            case SOURCE:
                return new Source (value);
            case DESTINATION:
                return new Destination (value);
                
            default:
                throw new IllegalStateException(
                   "unknown value attribute " + type);
        }
    }
}
