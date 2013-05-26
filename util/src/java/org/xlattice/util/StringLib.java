/* StringLib.java */
package org.xlattice.util;

/**
 * @author Jim Dixon
 **/

public class StringLib {

    // BYTE ARRAY TO HEX ////////////////////////////////////////////
    private static final char[] HEX = {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
    
    public static String byteArrayToHex (byte[] b) {
        return byteArrayToHex(b, 0, b.length);
    }
    /**
     *
     * @throws IndexOutOfBoundsException
     */
    public static String byteArrayToHex(byte[] b, int offset, int len) {
        StringBuffer sb = new StringBuffer();
        int i;
        for (i = offset ; i < offset + len; i++) 
            sb.append( HEX[(b[i] & 0xf0) >> 4] )
              .append( HEX[(b[i] & 0xf)      ] );
        return sb.toString();
    } 
    // CHOMP ////////////////////////////////////////////////////////
    /**
     * Remove any combination of line-ending characters from the
     * end of a String.  The characters in question are CR (ASCII 13) 
     * and LF (ASCII 10).
     *
     * XXX REVISIT THIS LATER, CHECKING EFFICIENCY
     *
     * @param s the String being edited
     * @return  a copy of the String without the terminating characters
     */
    public static String chomp (String s) {
        if (s == null)
            return s;
        int pos = s.length() - 1;
        if (pos < 0)
            return s;
        for (char c = s.charAt(pos); 
                            c == '\r' || c == '\n'; c = s.charAt (pos)) {
            if (--pos < 0)
                return "";
        }
        return s.substring (0, pos + 1);
    }
    // LC FIRST /////////////////////////////////////////////////////
    public static final String lcFirst(final String s) {
        if (s == null || s.equals(""))
                throw new IllegalArgumentException("null or empty name");
        char first = s.charAt(0);
        if ( Character.isUpperCase(first) ) 
            return Character.toLowerCase(first) + s.substring(1);
        else 
            return s;
    } 
    // UC FIRST /////////////////////////////////////////////////////
    public static final String ucFirst(final String s) {
        if (s == null || s.equals(""))
                throw new IllegalArgumentException("null or empty name");
        char first = s.charAt(0);
        if ( Character.isLowerCase(first) ) 
            return Character.toUpperCase(first) + s.substring(1);
        else 
            return s;
    } 
}
