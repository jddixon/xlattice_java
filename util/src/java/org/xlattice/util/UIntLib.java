/* UIntLib.java */
package org.xlattice.util;

public class UIntLib {

    private UIntLib () {}

    /**
     * Decode a byte field representing a 16-bit unsigned integer
     * in big-endian form.
     *
     * @param  buffer the byte array containing the encoded value
     * @param  offset its zero-based offset
     * @return the encoded value as an integer
     */
    public static int decodeUInt16 (byte[] buffer, int offset) {
        // PARAMETERS NOT CHECKED
        int uInt16;
        int hiByte = (0xff) & buffer[offset++];
        int loByte = (0xff) & buffer[offset];
        return (hiByte << 8) + loByte;
    }

    /**
     * Encode an integer into a byte array, treating the integer as
     * an unsigned 16-bit value.  The coding is big-endian.
     *
     * @return the initial offset + 2
     */
    public static int encodeUInt16 (int value, byte[]buffer, int offset) {
        // PARAMETERS NOT CHECKED
        buffer[offset++] = (byte) ( 0xff & (value >> 8) );
        buffer[offset++] = (byte)           value;
        return offset;
    }
} 
