/* ServerConfig.java */
package org.xlattice.node.nodereg;

import org.xlattice.crypto.RSAKey;
import org.xlattice.node.RSAInfo;
import org.xlattice.util.Base64Coder;

/**
 * Container for Node registry configuration information, for use with
 * org.xlattice.corexml.bind.  The assumption is that this will be 
 * elaborated in future.  At present the only configuration information
 * is the RSA private key.
 *
 * @author Jim Dixon
 */
public class ServerConfig {

    /** 
     * RSA Key; the RSA public key used to sign Node registration 
     * certificates 
     */
    private RSAInfo rsa_;

    public ServerConfig () {}

    // RSA KEY //////////////////////////////////////////////////////
    /**
     * @return a set of numbers specifying the RSA key
     */
    public RSAInfo getKey () {
        return rsa_;
    }
    /**
     * Set the RSA key specifier.
     * @param rsa data structure whose numbers specify the key
     */
    public void setKey (RSAInfo rsa) {
        rsa_ = rsa;
    }
}
