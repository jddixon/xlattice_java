/* NodeID.java */
package org.xlattice;

import org.xlattice.util.Base64Coder;

/**
 * Quasi-unique 160-bit value serving as a global identifier.
 * This will most often be an SHA-1 digest, but there is no
 * guarantee that it will be.  Any value assigned must have the
 * same or similar levels of randomness.
 *
 * @author Jim Dixon
 */
public class NodeID                         implements Comparable {

    /** length of the node ID in bytes */
    public final static int LENGTH = 20;

    /** the value assigned by the constructor */
    private final byte [] value;

    // CONSTRUCTOR //////////////////////////////////////////////////
    /** 
     * Create the node ID from a byte array.  The constructor makes
     * a copy of the array.
     *
     * @param b the value to be assigned to the NodeID
     * @throws  IllegalArgumentException if the array is not a valid ID
     */
    public NodeID (byte [] b) { 
        if (!isValid(b))
            throw new IllegalArgumentException ("invalid node ID");
        value = new byte[LENGTH];
        System.arraycopy( b, 0, value, 0, LENGTH);
    }
    /**
     * Create the node ID from a base-64 encoded value.
     *
     * XXX May need to chomp(val).
     */
    public NodeID (String val) {
        this (Base64Coder.decode(val));
        if (!isValid(value))
            throw new IllegalArgumentException (
                    "does not represent a valid NodeID");
    }
    // OTHER METHODS ////////////////////////////////////////////////
    public Object clone () {
        // constructor copies the byte array
        return new NodeID(value);
    }
    /**
     * Whether the array reference is non-null and the array of the
     * right length.
     * 
     * @return true if the reference is not null and the length correct.
     */
    public final static boolean isValid (byte[] b) {
        return b != null && b.length == LENGTH;
    }
   
    /**
     * Return a reference to the byte array.  XXX Not comfortable
     * with this; the elements of the array could then be modified.
     */
    public final byte[] value() {
        return value;
    }
    // INTERFACE Comparable /////////////////////////////////////////
    public int compareTo ( Object o ) {
        if (!(o instanceof NodeID))
            throw new IllegalArgumentException("object is not a NodeID");
        NodeID other = (NodeID) o;
        if (other == this)
            return 0;
        for (int i = 0; i < LENGTH; i++) {
            int myByte = value[i];
            if (myByte < 0)
                myByte += 256;
            int oByte = other.value[i];
            if (oByte < 0)
                oByte += 256;
            if ( myByte < oByte)
                return -1;
            else if (myByte > oByte)
                return 1;
        }
        return 0;
    }
    // EQUALS, HASHCODE /////////////////////////////////////////////
    /**
     * @return whether the Object o is a NodeID with the same value
     */
    public final boolean equals (Object o) {
        if (o == null || !(o instanceof NodeID))
            return false;
        NodeID other = (NodeID) o;
        if ( other == this ) 
            return true;
        byte[] b = other.value();
        for (int i = 0; i < LENGTH; i++)
            if (value[i] != b[i])
                return false;
        return true;
    } 
    /**
     * @return a hash useful in NodeID comparisons
     */
    public final int hashCode () {
        return ( (value[0] << 24) | (value[1] << 16 ) 
               | (value[2] <<  8) |  value[3] );
    }
    // SERIALIZATION ////////////////////////////////////////////////
    public String toString() {
        return org.xlattice.util.StringLib
                                    .byteArrayToHex(value, 0, LENGTH);
    }
}
