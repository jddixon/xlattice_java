/* SHA1Digest.java */
package org.xlattice.crypto;

import java.security.MessageDigest;
import org.xlattice.CryptoException;

/**
 * A very thin wrapper around Java's JSA MessageDigest.  
 *
 * @author Jim Dixon
 */
public final class SHA1Digest implements Digest {

    private final MessageDigest sha1;

    public SHA1Digest ()                    throws CryptoException { 
        try {
            sha1 = MessageDigest.getInstance("sha-1");
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new CryptoException ("can't find JSA SHA-1");
        }
    }

    // Digest INTERFACE /////////////////////////////////////////////
    public final byte[] digest () {
        return sha1.digest();
    }
    public final byte[] digest (byte[] data) {
        return sha1.digest(data);
    }
    public final int length () {
        return 20;
    }
    public final void reset () {
        sha1.reset();
    }
    public void update (byte[] data) {
        sha1.update(data);
    }
    public void update (byte[] data, int offset, int len) {
        sha1.update(data, offset, len);
    }

}
