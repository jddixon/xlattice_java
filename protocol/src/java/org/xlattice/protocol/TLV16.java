/* TLV16.java */
package org.xlattice.protocol;

import java.io.IOException;

/**
 * Represents the classic Type-Length-Value field found in many
 * network protocols, where the type and length are encoded as
 * unsigned 16-bit integers.
 *
 * This is an experiment; the implementation may change drastically
 * at a later date.
 * 
 * @author Jim Dixon
 */

public class TLV16 extends TLV {

    public TLV16 (int type, byte[]value) {
        super (2, type, value);
        // XXX Inelegant to throw the exception here :-(
        if (type > 65536)
            throw new IllegalArgumentException("type exceeds 65536: "
                    + type);
    }
    public TLV16 (int type, int length, byte[] value) {
        super (2, type, length, value);
    }

    public static TLV16 decode (byte[] message, int offset) 
                                                throws IOException {
        return (TLV16) decode (2, message, offset);
    }
}
