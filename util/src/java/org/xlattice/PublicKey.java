/* PublicKey.java */
package org.xlattice;

/**
 * That part of a public key cryptography key which is not secret.
 * In public key cryptography, there is a public key and at least
 * one private key.  The public key is often published or otherwise
 * made generally available.  The private key(s) is or are kept 
 * secret.  
 * 
 * If the key set is used for encryption, either key may be used for
 * encryption.  If the public key is used, the result is a document
 * which can be read only by whoever has the private key.  If the 
 * private key is used for encryption, anyone may decrypt the result
 * using the public key and at the same time they will verify that 
 * the document was encrypted by whoever had the secret part of the key.
 * 
 * If the key set is used for verification, then the private key is
 * used to sign a hash of the document being signed.  Anyone can then
 * verify both the integrity and authenticity of the document by first
 * taking a hash (such as an SHA-1 digest) of the document, then 
 * decrypting the digital signature to produce the original hash.  If
 * the two hashes are the same, then the document is guaranteed to be
 * identical to that originally signed and in addition it is certain 
 * that someone holding the private key signed the document.
 * 
 * For RSA in particular, it is known that there is some risk if the
 * exponent in the public key is small (3 in particular) and it is 
 * suspected that there may be some risk if the same key is used for
 * both purposes, for encryption/decryption and for digital signatures.
 *
 * @author Jim Dixon
 */
public interface PublicKey {

    public boolean equals(Object o);

    public int hashCode();
}
