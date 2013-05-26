/* SigVerifier.java */
package org.xlattice;

/**
 * Given a PublicKey, instances of this class can verify digital
 * signatures.
 *
 * @author Jim Dixon
 */
public abstract class SigVerifier {

    protected SigVerifier() {}

    public abstract String getAlgorithm();

    /**
     * Initialize the verifier for use with a particular PublicKey.
     *
     * @param pubkey PublicKey against which digital signature is verified
     */
    public abstract void init (PublicKey pubkey)
                                            throws CryptoException;

    /**
     * Add block of data to that being checked.
     *
     * @param data byte array being added
     * @return     reference to this verifier as a convenience in chaining
     */
    public abstract SigVerifier update (byte[] data)
                                            throws CryptoException;
    public abstract SigVerifier update (byte[] data, int offset, int len)
                                            throws CryptoException;
    
    /**
     * Check the digital signature passed against the data accumulated.
     *
     * @param digSig signature being checked
     * @return       whether the check is successful
     */
    public abstract boolean verify(byte[] digSig)
                                            throws CryptoException;
    public abstract boolean verify(byte[] digSig, int offset, int len)
                                            throws CryptoException;

}
