/* TlsContext.java */
package org.xlattice.crypto.tls;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

/**
 * There will normally be one and only one TlsContext associated 
 * with an XLattice program invocation.  Usually it contains 
 * information used by all SSL/TLS connections and normally there
 * will be many such connections.
 * 
 * The TlsContext holds a String specifying the TLS/SSL protocol;
 * a SecureRandom instance; one or more private keys (zero or
 * more of which may be used by any session); and a KeyManagerFactory.
 * The TlsContext will also have at least one authentication level,
 * the interpretation of which is still not settled.  At least initially
 * this will be a 32-bit integer interpreted as a bit field.  Note that we
 * need to be able to specify an authorization level or client 
 * connections and another for server connections.
 *
 * The TlsSession holds zero or more peer public keys; a TrustManagerFactory;
 * an ephemeral JSSE SSLContext; and a JSSE SSLSession, which allows us
 * to resume the TlsSession.  It also has an authentication level member
 * characterizing the connection.
 *
 * An XLattice Node will have a single TlsContext and typically many
 * TlsSessions.
 *
 * @author Jim Dixon
 */
public class TlsContext implements TlsConst {

    /** SSL, TLS, etc */
    final String proto;
    
    /** authentication level */
    final int    level;
    
    /** private key store */
    final String myKSName;
    final KeyStore myKeyStore;
    final char[] myPassphrase;

    final SecureRandom rng;
    
    /** host name used for this end of the connection */
    final String hostHint;
    final int    portHint;
   
    /** decides how to respond to authentication requests */
    final KeyManager[] keyManagers;
    
    /**
     * Parameter checking is done by the TlsEngine.
     */
    public TlsContext(
            final String proto, final int level, 
            final String myKSName,  final char[] myPassphrase,
            SecureRandom rng, final String hostHint, final int portHint) 
                        throws IOException, GeneralSecurityException {

        this.level = level;
        if (proto == null || proto.equals(""))
            throw new IllegalArgumentException(
                                    "null or empty SSL/TSL protocol");
        // force a NoSuchAlgorithmException if appropriate
        SSLContext.getInstance(proto);
        this.proto = proto;
        
        if (myKSName == null || myKSName.equals(""))
            throw new IllegalArgumentException(
                                    "null or empty key store file name");
        this.myKSName = myKSName;
        
        if (myPassphrase == null || myPassphrase.length == 0)
            throw new IllegalArgumentException(
                                    "null or empty passphrase");
        this.myPassphrase = myPassphrase;
        
        if (rng == null) {
            rng = new SecureRandom();
            rng.nextInt();
        }
        this.rng = rng;

        if (hostHint == null || hostHint.equals(""))
            throw new IllegalArgumentException(
                                    "null or empty hostname hint");
        this.hostHint = hostHint;

        this.portHint = portHint;

        myKeyStore = KeyStore.getInstance("JKS");
        myKeyStore.load( new FileInputStream (myKSName), myPassphrase );
        
        if (level == ANONYMOUS_TLS) {
            keyManagers = null;
        } else {
            // may be ignored in TlsEngine, depends upon auth level
            KeyManagerFactory kmf 
                        = KeyManagerFactory.getInstance("NewSunX509");
            kmf.init(myKeyStore, myPassphrase);
            keyManagers = kmf.getKeyManagers();
        }
    }
    public KeyManager[] getKeyManagers() {
        return keyManagers;
    }
    // ACCESS TO PARAMETERS /////////////////////////////////////////
    /** authentication level */
    public int getLevel() {
        return level;
    }
    /** SSL, TLS, etc */
    public String getProtocol() {
        return proto;
    }
    /** private key store */
    public KeyStore getKeyStore() {
        return myKeyStore;
    }
    public SecureRandom getRNG() {
        return rng;
    }
    /** host name used for this end of the connection */
    public String getHostHint() {
        return hostHint;
    }
    public int getPortHint () {
        return portHint;
    }
}
