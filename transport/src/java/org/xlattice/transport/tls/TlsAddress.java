/* TlsAddress.java */
package org.xlattice.transport.tls;

import java.security.KeyStore;

import org.xlattice.transport.IPAddress;

/**
 * XXX The authentication level is actually for connections 
 * XXX involving this address.  
 * 
 * @author Jim Dixon
 */
public class TlsAddress extends IPAddress {

    protected final int      level;
    protected final char[]   passphrase;
    private         KeyStore keys;
    /** XXX dunno if this is of any value */
    protected final boolean  isPrivate;
   
    /**
     * @param ipAddr     XLattice IP address (host plus port)
     * @param authLevel  authentication level
     * @param keyStore   holds keys to be used with this address
     * @param passwd     keyStore passphrase (why??)
     * @param isPrivate  whether the keyStore contains private keys
     */
    public TlsAddress (IPAddress ipAddr, int authLevel, 
            KeyStore keyStore, char[] passwd, boolean isPrivate) {
        super(ipAddr.getHost(), ipAddr.getPort());
        level = authLevel;
        if (keyStore == null) {
            try {
                keys = KeyStore.getInstance("JKS");
            } catch (java.security.KeyStoreException kse) {
                /* STUB */
            }
        } else {
            keys = keyStore;
        }
        passphrase = passwd;
        this.isPrivate = isPrivate;
    }
    // PROPERTIES ///////////////////////////////////////////////////
    public KeyStore getKeyStore() {
        return keys;
    }
    // SERIALIZATION ////////////////////////////////////////////////
    public String toString() {
        return new StringBuffer("tls:")
            .append ( host.toString() )
            .append (':')
            .append (getPort())
            .toString();
    }
}
