/* DecimalVersion.java */

package org.xlattice.util;

public final class DecimalVersion {

    private int dv;            // uint32

    // CONSTRUCTORS -------------------------------------------------
    
    public DecimalVersion (int a, int b, int c, int d) {
        dv =   (0xff & a)         |
                ((0xff & b) << 8)  |
                ((0xff & c) << 16) |
                ((0xff & d) << 24);
    }
    public DecimalVersion (int a, int b, int c) {
        this(a, b, c, 0);
    }
    public DecimalVersion (int a, int b) {
        this(a, b, 0, 0);
    }
    public DecimalVersion (int a) {
        this(a, 0, 0, 0);
    }
    /**
     * @return DecimalVersion equivalent to byte array.
     */
    static public DecimalVersion versionFromBytes( byte[] b) {
        if (b.length != 4) {
            throw new IllegalArgumentException(
                "wrong number of bytes for DecimalVersion");
        }
        return new DecimalVersion(b[0], b[1], b[2], b[3]);
    }
   
    // SERIALIZATION ------------------------------------------------
    
    /**
     * @return DecimalVersion in string format.
     */
    public String toString() {
        int val = dv;
        int a = 0xff & val;
        val >>= 8;
        int b = 0xff & val;
        val >>= 8;
        int c = 0xff & val;
        val >>= 8;
        int d = 0xff & val;
        String s; 
        if (c == 0) {
            if (d == 0) {
                s = String.format("%d.%d", a, b);
            } else {
                s = String.format("%d.%d.%d.%d", a, b, c, d);
            }
        } else if (d == 0) {
            s = String.format("%d.%d.%d", a, b, c);
        } else {
            s = String.format("%d.%d.%d.%d", a, b, c, d);
        }
        return s;
    }
    
    /**
     * Convert a string like a.b.c.d back to a uint32 DecimalVersion.  At
     * least one digit must be present.
     */
    static public DecimalVersion parseDecimalVersion(String s) {
    
        int v[] = {0,0,0,0};
        s = s.trim();
        String[] parts = s.split(".");
        if (parts.length > 4) {
            throw new IllegalStateException("too many parts in version number");
        }
        for (int i = 0; i < parts.length; i++) {
            int n = Integer.parseInt(parts[i]);
            v[i] = n;
        }
        return new DecimalVersion(v[0], v[1], v[2], v[3]);
    }
}
