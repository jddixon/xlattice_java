/* DigSigner.java */
package org.xlattice;

/**
 * Digital signature generator.  Instances of this are created
 * by invoking Key.getSigner(String digestName).
 *
 * @see Key
 *
 * @author Jim Dixon
 */
public interface DigSigner {

    public String getAlgorithm();

    /** @return the length of the digital signature generated */
    public int length();
    
    /**
     * Add the binary data referenced to any already processed by
     * the message digest part of the algorithm.
     */
    public DigSigner update (byte[] data)   throws CryptoException;

    public DigSigner update (byte[] data, int offset, int len)
                                            throws CryptoException;
    /**
     * Generate a digital signature and implicitly reset().
     *
     * @return the digital signature as a byte array
     */
    public byte[] sign ()                   throws CryptoException;

}
