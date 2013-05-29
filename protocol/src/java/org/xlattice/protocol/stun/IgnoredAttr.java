/* IgnoredAttr.java */
package org.xlattice.protocol.stun;

/**
 *
 * @author Jim Dixon
 */
public class IgnoredAttr extends ValueAttr {

    public IgnoredAttr (int type, byte[] value) {
        super (type, value);
        if (this.type < 0x8000)
            throw new IllegalArgumentException(
                    "illegal type for IgnoredAttr: " + type);
    }
}
