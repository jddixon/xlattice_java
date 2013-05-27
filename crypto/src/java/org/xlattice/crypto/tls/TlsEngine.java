/* TlsEngine.java */
package org.xlattice.crypto.tls;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;

/**
 */
public abstract class TlsEngine         implements TlsConst {

    // INSTANCE VARIABLES ///////////////////////////////////////////
    protected       EngineStates state = EngineStates.SETUP;

    final SSLEngine    engine;
    final SSLSession   jsseSession;
    
    // CONSTRUCTORS /////////////////////////////////////////////////
    /**
     */
    protected TlsEngine (TlsContext context, TlsSession session )
                            throws GeneralSecurityException, IOException {
       
        if (context == null)
            throw new IllegalArgumentException ("null TLS context");
        if (session == null)
            throw new IllegalArgumentException ("null TLS session");
        
        SSLContext jsseContext = SSLContext.getInstance(context.proto);
        KeyManager[]   kMgrs = context.getKeyManagers();
        TrustManager[] tMgrs = session.getTrustManagers();
        jsseContext.init(kMgrs, tMgrs, context.rng);
        
        String hostHint = context.hostHint;
        if (hostHint == null || hostHint == "") {
            engine = jsseContext.createSSLEngine();
        } else {
            engine = jsseContext.createSSLEngine(
                                        hostHint, context.portHint);
        }
        jsseSession = engine.getSession();
    }
    // SETUP, PROPERTIES ////////////////////////////////////////////
    public EngineStates getState() {
        return state;
    }
    protected void setState(EngineStates newState) {
        state = newState;
    }
    protected SSLSession getJsseSession() {
        return jsseSession;
    }
    /** should not be called until ciphersuite set */
    public int getApplicationBufferSize() {
        return jsseSession.getApplicationBufferSize();
    }
    public int getPacketBufferSize() {
        return jsseSession.getPacketBufferSize();
    }
    public String[] getEnabledCipherSuites() { 
        return engine.getEnabledCipherSuites();
    }
    public void setEnabledCipherSuites( String[] p ) {
        engine.setEnabledCipherSuites(p);
    }
    
    /** XXX value? */
    public String[] getEnabledProtocols() { 
        return engine.getEnabledProtocols();
    }
    public void setEnabledProtocols( String[] p ) {
        engine.setEnabledProtocols(p);
    }
    // HANDSHAKING //////////////////////////////////////////////////
    /**
     * XXX "To force a complete SSL/TLS session renegotiation, the 
     * current session should be invalidated prior to calling this
     * method."  Translation: must call Session.invalidate();
     */
    void beginHandshake()                       throws IOException {
        if (state == EngineStates.SETUP)
            state = EngineStates.INITIAL_HANDSHAKE;
        else if (state == EngineStates.DATA_TRANSFER)
            state = EngineStates.RENEGOTIATING;
        else 
            throw new IOException("cannot beginHandshake in state "
                    + state);
        // throws SSLException which is an IOException
        engine.beginHandshake();
    }
    /**
     * XXX Urgently need a less ugly interface.
     */
    public SSLEngineResult.HandshakeStatus getHandshakeStatus() {
        return engine.getHandshakeStatus();
    }
    public Runnable getDelegatedTask() {
        return engine.getDelegatedTask();
    }
    // ENCRYPT/DECRYPT //////////////////////////////////////////////
    // XXX ADD OTHER VARIANTS LATER XXX
    public SSLEngineResult unwrap (ByteBuffer src, ByteBuffer dest) 
                                                throws IOException {
        return engine.unwrap (src, dest);
    }
    public SSLEngineResult wrap (ByteBuffer src, ByteBuffer dest) 
                                                throws IOException {
        return engine.wrap(src, dest);   
    }
    
    // TEARDOWN /////////////////////////////////////////////////////
    public void closeInbound()                  throws IOException {
        /* possible state transition */
        engine.closeInbound();
    }
    public void closeOutbound()                 throws IOException {
        /* possible state transition */
        engine.closeOutbound();
    }
    public boolean isInboundDone() {
        return engine.isInboundDone();
    }
    public boolean isOutboundDone() {
        return engine.isOutboundDone();
    }
    // OTHER METHODS ////////////////////////////////////////////////
    
    // SERIALIZATION ////////////////////////////////////////////////


}
