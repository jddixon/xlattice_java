/* KeyGen.java */
package org.xlattice.crypto;

import org.xlattice.CryptoException;
import org.xlattice.Key;

/**
 * @author Jim Dixon
 **/

public interface KeyGen {

    public String algorithm ();

    public Key generate()                   throws CryptoException;

    public void initialize(int keysize);

}
