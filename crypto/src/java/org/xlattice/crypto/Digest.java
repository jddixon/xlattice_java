/* Digest.java */
package org.xlattice.crypto;

/**
 * A Digest is something that converts a (normally larger) byte array
 * into a second (normally smaller) byte array.  The Digest is a 
 * cryptographically secure one-way hash function.  The output byte
 * array is of fixed size.
 *
 * @author Jim Dixon
 */
public interface Digest {

    /**
     * @return the hash of the data seen so far.
     */ 
    public byte[] digest ();

    /**
     * Add data block to whatever has been seen so far and return
     * digest of the combination.
     */
    public byte[] digest (byte[] data);
    
    /**
     * The length of the byte array to be returned.
     *
     * @return the size of the digest in bytes
     */
    public int length();
    
    /**
     * Reinitialize the hash function.  Any data passed in earlier
     * calls to update() will be lost.
     */
    public void reset();

    /**
     * Add to the data being hashed.
     * @param data byte array of arbitrary length
     */
    public void update (byte[] data);
    
}
