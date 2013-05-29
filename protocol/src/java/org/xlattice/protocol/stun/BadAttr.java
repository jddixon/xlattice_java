/* BadAttr.java */
package org.xlattice.protocol.stun;


/**
 * This is for testing only; types are restricted to 0x7000 - 0x700f.
 *
 * @author Jim Dixon
 */
public class BadAttr extends ValueAttr {

    public BadAttr (int type, byte[] value) {
        super (type, value);
        if (this.type < 0x7000 || 0x700f < this.type )
            throw new IllegalArgumentException(
                    "illegal type for BadAttr: " + type);
    }
}
