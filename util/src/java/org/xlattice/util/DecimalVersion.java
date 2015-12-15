/* DecimalVersion */
package org.xlattice.util;

/**
 * An integer value construed as a 4-part little-endian version number,
 * with each part seen as an unsigned byte.
 *
 * @author Jim Dixon
 */

public class DecimalVersion {

    private final int value;

    // CONSTRUCTORS /////////////////////////////////////////////////

    /**
     * Convert a string like 1.2 or 1.2.3 or 1.2.3.4 into a DecimalVersion.
     *
     * @param s the decimal version in string format
     *
     */
    public DecimalVersion (String s) throws IllegalArgumentException {
        int a = 0;
        int b = 0;
        int c = 0;
        int d = 0;
        if (s == null || s == "")
            throw new IllegalArgumentException("null or empty string");
        String[] parts = s.split("\\.");
        switch (parts.length) {
            case 4:
                d = Integer.parseInt(parts[3]);
            case 3:
                c = Integer.parseInt(parts[2]);
            case 2:
                b = Integer.parseInt(parts[1]);
            case 1: 
                a = Integer.parseInt(parts[0]);
                break;
            default:
                throw new IllegalArgumentException("not properly formatted");
        }
        if ((a < 0 || 255 < a) || (b < 0 || 255 < b) ||
            (c < 0 || 255 < c) || (d < 0 || 255 < d)) 
            throw new IllegalArgumentException("a,b,c,d must be in [0..256)");
        this.value = (0xff & a)         | ((0xff & b) << 8)  |
                     ((0xff & c) << 16) | ((0xff & d) << 24) ;
    }

    /** 
     * Convert parameters into a DecimalVersion.  Each parameter is treated
     * as an unsigned byte in the range 0..255.  No error is returned 
     * if a value is out of range.
     */
    public DecimalVersion (int a) {
        this(a, 0, 0, 0);
    }
    public DecimalVersion (int a, int b) {
        this(a, b, 0, 0);
    }
    public DecimalVersion (int a, int b, int c) {
        this(a, b, c, 0);
    }
    public DecimalVersion (int a, int b, int c, int d) {
        this.value = (0xff & a)         | ((0xff & b) << 8)  |
                     ((0xff & c) << 16) | ((0xff & d) << 24) ;
    }

    // PROPERTIES ///////////////////////////////////////////////////
    //
    public int getA() {
        return this.value & 0xff;
    }
    public int getB() {
        return (this.value >> 8) & 0xff;
    }
    public int getC() {
        return (this.value >> 16) & 0xff;
    }
    public int getD() {
        return (this.value >> 24) & 0xff;
    }
    // OTHER METHODS ////////////////////////////////////////////////
    public boolean equals(Object o) {
        if (o == null) 
            return false;
        if (!(o instanceof DecimalVersion))
            return false;
        DecimalVersion other = (DecimalVersion)o;
        return this.value == other.value;
    }
    // SERIALIZATION ////////////////////////////////////////////////

    // Convert the DecimalVersion to dotted string format.  We always include
    // at least the two leading parts, so version 1.0.0.0 in string form 
    // is "1.0".
    
    public String toString() {
        int a = this.getA();
        int b = this.getB();
        int c = this.getC();
        int d = this.getD();
        StringBuffer sb = new StringBuffer();
        sb.append(a).append(".").append(b);
        if (c > 0 || d > 0 )
            sb.append(".").append(c);
        if (d > 0 )
            sb.append(".").append(d);
        return sb.toString();
    }
}



