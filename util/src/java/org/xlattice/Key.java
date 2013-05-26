/* Key.java */
package org.xlattice;

/**
 * An asymmetric cryptographic key.  This will contain the information 
 * necessary to use the key with the particular algorithm.
 * 
 * XXX NEEDS A BETTER NAME
 *
 * @author Jim Dixon
 */
public interface Key {

    /** @return the name of the algorithm, for example, "rsa" */
    public String algorithm();

    /** 
     * Given a message digest algorithm, return a reference to a 
     * digital signature generator suitable for this key.
     *
     * XXX This is an experiment - it might be considered a failure,
     * XXX in that many public key cryptography algorithms are not
     * XXX used for digital signatures.
     *
     * @param digestName case-insensitive name of the signature generator
     */
    public DigSigner getSigner (String digestName)
                                            throws CryptoException;

    /**
     * Another experiment.
     */
    public PublicKey getPublicKey();
}
