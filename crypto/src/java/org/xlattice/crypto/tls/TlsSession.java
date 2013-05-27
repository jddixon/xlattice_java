/* TlsSession.java */
package org.xlattice.crypto.tls;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
//import java.security.SecureRandom;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

/**
 * A TlsSession is associated with each SSL/TLS connection.  It is 
 * used in conjunction with a TlsContext.  The TlsContext holds 
 * common information used by many connections; the TlsSession 
 * contains information specific to a given connection, and indeed
 * sufficient information to allow the connection to be reopened 
 * after being closed.
 * 
 * The TlsSession holds zero or more peer public keys; a TrustManagerFactory;
 * an ephemeral JSSE SSLContext; and a JSSE SSLSession, which allows us
 * to resume the TlsSession.  It also has an authentication level member
 * characterizing the connection.
 *
 * An XLattice Node will normally have a single TlsContext and many
 * TlsSessions.
 *
 * XXX IS IT NECESSARY TO HAVE A SEPARATE KEYSTORE?
 *
 * @author Jim Dixon
 */
public class TlsSession                     implements TlsConst {

    final TlsContext context;
    
    /** the authentication level for the session */
    final   int      level;
    
    /** file name, key store for peer's public key */
    final   String   trustStoreName;
    final   KeyStore trustStore;
    final   char[]   passphrase;

    // XXX NEED TO SET THESE, then need a getter
    private String   peerHostHint;
    private int      peerPortHint;

    final TrustManager[] trustManagers;
    
    /** whether this end acts as a TLS client */
    public final boolean isClient;
    
    private TlsEngine  engine;
    private SSLSession jsseSession;
   
    public TlsSession(final TlsContext context, final int authLevel, 
            final String trustStoreName, final char[] passphrase, 
            final boolean isClient) 
                        throws IOException, GeneralSecurityException {
        if (context == null)
            throw new IllegalArgumentException ("null TlsContext");
        this.context = context;
        
        this.isClient = isClient;
        level = effectiveAuthLevel(context.level, authLevel);
        
        int myTrust;
        if (isClient) {
            myTrust = (level & SERVER_MASK) >> SERVER_SHIFT;
        } else /* server */ {
            myTrust = (level & CLIENT_MASK) >> CLIENT_SHIFT;
        }
        if (level == ANONYMOUS_TLS || myTrust == 0) {
            this.trustStoreName = null;
            this.trustStore     = null;
            this.passphrase     = null;
            trustManagers       = new TrustManager[] {TRUST_ANYONE};
        } else {
            if (trustStoreName == null || trustStoreName.equals(""))
                throw new IllegalArgumentException(
                        "null or empty KeyStore file name");
            this.trustStoreName = trustStoreName;
            
            if (passphrase == null || passphrase.equals(""))
                throw new IllegalArgumentException(
                        "null or empty passphrase");
            this.passphrase   = passphrase;
    
            trustStore = KeyStore.getInstance("JKS");
            trustStore.load( new FileInputStream (trustStoreName), passphrase );
            if ( (myTrust & ANY_CERT) != 0 ) {
                trustManagers = new TrustManager[] {TRUST_ANYONE};
            } else if ( (myTrust & CA_SIGNED_CERT) != 0 ) {
//              trustManagers = null;       // default is OK (??)
                TrustManagerFactory tmf 
                        = TrustManagerFactory.getInstance("SunX509");
                tmf.init(trustStore);
                trustManagers = tmf.getTrustManagers();
            } else { 
                // KNOWN_CERT
                throw new IllegalStateException(
                        "can't handle myTrust == " + myTrust);
            }
            // XXX LEARN_CERT NOT HANDLED
        }
        
    }
    /**
     * DETERMINE EFFECTIVE AUTHENTICATION LEVEL.  Context authentication 
     * level is interpreted as the minimum acceptable, except that if
     * LEARN then it is ORed in.  XXX This implementation doesn't 
     * quite make sense, but it should do for a while.
     *
     * @param ctxLevel  TlsContext authentication level
     * @param authLevel TlsSession parameter
     */
    public final int effectiveAuthLevel (int ctxLevel, int authLevel) {
        boolean learnClient = ((authLevel & LEARN_CLIENT_CERT) != 0) |
                              ((ctxLevel  & LEARN_CLIENT_CERT) != 0);
        boolean learnServer = ((authLevel & LEARN_SERVER_CERT) != 0) |
                              ((ctxLevel  & LEARN_SERVER_CERT) != 0);
       
        final int OUR_CLIENT_MASK = CLIENT_MASK & ~LEARN_CLIENT_CERT
                                    & ~0x80000000;  // clear high bit
        int ctxClientFlags = ctxLevel  & OUR_CLIENT_MASK;
        int ourClientFlags = authLevel & OUR_CLIENT_MASK;
        int effClientFlags = (ourClientFlags > ctxClientFlags ?
                                ourClientFlags : ctxClientFlags) |
                             (learnClient ? LEARN_CLIENT_CERT : 0);
    
        final int OUR_SERVER_MASK = SERVER_MASK & ~LEARN_SERVER_CERT
                                    & ~0x00008000;  // clear high bit
        int ctxServerFlags = ctxLevel  & OUR_SERVER_MASK;
        int ourServerFlags = authLevel & OUR_SERVER_MASK;
        int effServerFlags = (ourServerFlags > ctxServerFlags ?
                                ourServerFlags : ctxServerFlags) |
                             (learnServer ? LEARN_SERVER_CERT : 0);
        return effClientFlags | effServerFlags;
    } 
    
    public TrustManager[] getTrustManagers() {
        return trustManagers;
    }
    /**
     * XXX NEED TO ALLOW FOR SESSION BEING RESUMED.
     */
    public TlsEngine getEngine() 
                        throws IOException, GeneralSecurityException {
        if (engine == null) {
            if (isClient) 
                engine = new TlsClientEngine (context, this);
            else
                engine = new TlsServerEngine (context, this);
        } 
        jsseSession = engine.getJsseSession();
        return engine;
    }
    public SSLSession getJsseSession() {
        return jsseSession;
    }
}
