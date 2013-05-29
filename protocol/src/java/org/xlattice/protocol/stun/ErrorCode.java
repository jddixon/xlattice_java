/* ErrorCode.java */
package org.xlattice.protocol.stun;

import org.xlattice.protocol.TLV16;

/**
 * STUN attribute carrying an error code and a string explanation.
 * In nibbles the value part looks like
 *
 *   +---+---+---+---+---+---+---+---+
 *   | 0   0   0   0   0   N     n   |
 *   +---+---+---+---+---+---+---+---+
 *   |   d0      d1      d2      d3  |
 *   +---+---+---+---+---+---+---+---+
 *   |   d4     ...
 *   +---+---+---+---+---+---+---+---+
 *
 * where N is the decimal hundreds digit (and so between 1 and 6
 * inclusive), n is the error code modulo 100, and the d-sub-i 
 * represent the description, which must be padded to a multiple
 * of four.
 *
 * @author Jim Dixon
 */
public class ErrorCode extends StunAttr {

    public final static int[] codes = {
        400,    401,    420,    
        430,    431,    432,    
        433,    500,    600 };
    
    public final static String[] reasonPhrases = {
        "Bad Request",      "Unauthorized",             "Unknown Attribute",
        "Stale Credentials","Integrity Check Failure",  "Missing Username",
        "Use TLS",          "Server Error",             "Global Failure" };
    
    public final static byte[][] errorValues = new byte[codes.length][];
    
    static {
        for (int n = 0; n < codes.length; n++) {
            int code = codes[n];
            byte[] d = reasonPhrases[n].getBytes();
            int len = 4 + d.length;
            if ((len % 4) != 0)
                len += 4;
            byte[] val = new byte[len];
            val[2]    = (byte)(code / 100);
            val[3]    = (byte)(code % 100);
            System.arraycopy(d, 0, val, 4, d.length);
            errorValues[n] = val;
        }
    }
    // XXX LAZINESS
    public final static int index(int code) {
        for (int n = 0; n < codes.length; n++) 
            if (code == codes[n])
                return n;
        throw new IllegalStateException("error code not recognized: " + code);
    }
    // INSTANCE VARIABLES ///////////////////////////////////////////
    public final int code;
    // CONSTRUCTORS /////////////////////////////////////////////////
    public ErrorCode( int code) {
        super(ERROR_CODE, errorValues[index(code)]);
        this.code = code;
    }
    // SERIALIZATION ////////////////////////////////////////////////
    /**
     * Type and length have already been decoded.  The offset
     * is to the data structure depicted above.
     */
    protected static TLV16 decodeValue (int length, 
                                     byte[] message, int offset) {
        // XXX first two bytes must be zero
        for (int i = 0; i < 2; i++)
            if (message[offset++] != 0)
                throw new IllegalStateException(
                        "expected zero byte at offset " + (offset - 1)
                        + " but found " + (message[offset - 1]) );
        int hundreds = message[offset++];
        if (hundreds < 1 || hundreds > 6)
            throw new IllegalStateException(
                    "bad error code: hundreds figure is " + hundreds);
        int subcode = message[offset++];
        if (subcode < 0 | subcode > 99)
            throw new IllegalStateException(
                    "bad error code: encoded last two digits are " 
                    + subcode);
        // XXX REASON PHRASE IS IGNORED, as is length XXX
        return new ErrorCode ( hundreds * 100 + subcode );
    }

    public String toString() {
        return new StringBuffer("ErrorCode:        ")
            .append(code)
            .append(' ')
            .append(reasonPhrases[ index(code) ])
            .toString();
    }
}
