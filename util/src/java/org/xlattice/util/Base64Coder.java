/* Base64Coder.java */
package org.xlattice.util;

/**
 * Implementation of the 'filesafe' variant of Base64 encoding
 * and decoding.  See RFC 3548.
 *
 * The 'filesafe' variant of the encoding scheme changes the 
 * Base64 encoding by replacing characters 62 and 63, "+/", with 
 * "-_".  This makes the encoding usable in HTTP transmissions as
 * well as in file names.
 * 
 * @author Jim Dixon
 */
public class Base64Coder {

    private static byte[] charSet = new byte[] {
        (byte)'A', (byte)'B', (byte)'C', (byte)'D', 
        (byte)'E', (byte)'F', (byte)'G', (byte)'H', 
        (byte)'I', (byte)'J', (byte)'K', (byte)'L', 
        (byte)'M', (byte)'N', (byte)'O', (byte)'P', 
        (byte)'Q', (byte)'R', (byte)'S', (byte)'T', 
        (byte)'U', (byte)'V', (byte)'W', (byte)'X', 
        (byte)'Y', (byte)'Z', (byte)'a', (byte)'b', 
        (byte)'c', (byte)'d', (byte)'e', (byte)'f', 
        (byte)'g', (byte)'h', (byte)'i', (byte)'j', 
        (byte)'k', (byte)'l', (byte)'m', (byte)'n', 
        (byte)'o', (byte)'p', (byte)'q', (byte)'r', 
        (byte)'s', (byte)'t', (byte)'u', (byte)'v', 
        (byte)'w', (byte)'x', (byte)'y', (byte)'z', 
        (byte)'0', (byte)'1', (byte)'2', (byte)'3', 
        (byte)'4', (byte)'5', (byte)'6', (byte)'7', 
        (byte)'8', (byte)'9', (byte)'-', (byte)'_' 
    };
    public static final byte PADCHAR = (byte) '=';
    /* private */ static int reverseMap(char c) {
        if (c >= 'A' && c <= 'Z')
            return (c - 'A');
        if (c >= 'a' && c <= 'z')
            return (c - 'a' + 26);
        if (c >= '0' && c <= '9')
            return (c - '0' + 52);
        if (c == '-')
            return 62;
        if (c == '_')
            return 63;
        throw new IllegalArgumentException ("character "
                + c + " not in Base64character set");
    }

    /** 
     * Given a byte array, returns a reference to a String 
     * containing the base-64 encoded equivalent.  The String
     * contains neither spaces nor new lines.
     * 
     * Users should remember that Strings are immutable and 
     * therefore this method should NOT be used to encode secret
     * key materials, although it is perfectly appropriate for
     * use with public keys.
     *
     * TODO: add a 'folding' boolean
     */
    public static String encode(byte[] b) {
        int encodedLen  = ((b.length + 2)/3) * 4;
        byte[] val = new byte[encodedLen];
        int triplets = (b.length/3)*3;
        int k = 0;
        byte b0, b1, b2;
        int i;
        for (i = 0; i < triplets; ) {
            b0 = b[i++];
            b1 = b[i++];
            b2 = b[i++];
            val[k++] = charSet[(b0 & 0xfc) >> 2];
            val[k++] = charSet[((b0 & 0x03) << 4) | ((b1 & 0xf0) >> 4)];
            val[k++] = charSet[((b1 & 0x0f) << 2) | ((b2 & 0xc0) >> 6)];
            val[k++] = charSet[(b2 & 0x3f)];
        }
        // there may be 1 or 2 bytes not yet handled
        if (i < b.length) {
            b0 = b[i++];
            val[k++] = charSet[(b0 & 0xfc) >> 2];
            if (i < b.length) {
                b1 = b[i];
                val[k++] = charSet[((b0 & 0x03) << 4) | ((b1 & 0xf0) >> 4)];
                val[k++] = charSet[ (b1 & 0x0f) << 2];
            } else
                val[k++] = charSet[ (b0 & 0x03) << 4];
        }
        for ( ; k < encodedLen ; k++)
            val[k] = PADCHAR;
        return new String (val);
    }

    /**
     * Given a String containing a base-64 encoded value, returns
     * a reference to a byte array containing the decoded equivalent.
     * 
     * @return base64 string converted to a byte array
     */
    public static byte[] decode (String s) {
        char[] chars = s.toCharArray();
        int len      = chars.length;
        int quartets = len/4;
        if (quartets * 4 != len)
            throw new IllegalArgumentException(
                    "string length not a multiple of four");
        // 0, 1, or 2  of the last quartet may be PADCHARs
        int padChars = 0;
        if (chars[--len] == PADCHAR) {
            quartets--;
            padChars++;
            if (chars[--len] == PADCHAR) {
                padChars++;
            }
        }
        int outCount = 3 * quartets;
        if (padChars == 1)
            outCount += 2;
        else if (padChars == 2)
            outCount += 1;
        byte[] out = new byte[outCount];
        int i;
        int j = 0;
        int b0, b1, b2, b3;
        for (i = 0; i < quartets * 4;  ) {
            b0 = reverseMap(chars[i++]);
            b1 = reverseMap(chars[i++]);
            b2 = reverseMap(chars[i++]);
            b3 = reverseMap(chars[i++]);
            out[j++] = (byte)((b0 << 2) | (b1 >> 4));
            out[j++] = (byte)((b1 << 4) | (b2 >> 2));
            out[j++] = (byte)((b2 << 6) |  b3      );
        }
        if (padChars > 0) {
            b0 = reverseMap(chars[i++]);
            b1 = reverseMap(chars[i++]);
            out[j++] = (byte)((b0 << 2) | (b1 >> 4));
            if (padChars == 1) {
                b2 = reverseMap(chars[i++]);
                out[j++] = (byte)((b1 << 4) | (b2 >> 2));
            }
        }
        return out;
    }
}
