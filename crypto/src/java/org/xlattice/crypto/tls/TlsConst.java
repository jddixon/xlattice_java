/* TlsConst.java */
package org.xlattice.crypto.tls;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public interface TlsConst {

    public static enum EngineStates { 
        SETUP, INITIAL_HANDSHAKE, DATA_TRANSFER, 
                  RENEGOTIATING, SHUTTING_DOWN, CLOSED };

    // this trusts anything at all
    public static final TrustManager TRUST_ANYONE = new X509TrustManager() {
        public void checkClientTrusted(
                X509Certificate[] chain, String authType) 
                                        throws CertificateException {}
        public void checkServerTrusted(
                X509Certificate[] chain, String authType) 
                                        throws CertificateException {}
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }; 
    /** the only anonymous cipher supported if using TLS */
    public  static final String[] TLS_ANONYMOUS_CIPHERS
                    = new String[] {"TLS_DH_anon_WITH_AES_128_CBC_SHA"};
    
    /** neither side uses public key */
    public static final int ANONYMOUS_TLS               = 0x0000;

    public static final int ANY_CERT                    = 0x0001;
    public static final int CA_SIGNED_CERT              = 0x0002;
    public static final int KNOWN_CERT                  = 0x0004;
    public static final int LEARN_CERT                  = 0x4000;
    
    public static final int SERVER_SHIFT                =  0;
    public static final int CLIENT_SHIFT                = 16;   // bits
    
    /** low bytes contain client-related flags */
    public static final int SERVER_MASK = 0x0000ffff;
    /** accept any cert for the server */
    public static final int ANY_SERVER_CERT     
                                        = ANY_CERT      << SERVER_SHIFT;
    /** accept any cert signed by a known CA */
    public static final int CA_SIGNED_SERVER_CERT 
                                        = CA_SIGNED_CERT<< SERVER_SHIFT;
    /** accept any previously learned cert */
    public static final int KNOWN_SERVER_CERT   
                                        = KNOWN_CERT    << SERVER_SHIFT;
    /** if this cert is accepted, add to list of known server certs */
    public static final int LEARN_SERVER_CERT   
                                        = LEARN_CERT    << SERVER_SHIFT;
    
    /** high bytes contain server-related flags */
    public static final int CLIENT_MASK = 0xffff0000;
    /** accept any cert for the client */
    public static final int ANY_CLIENT_CERT     
                                        = ANY_CERT      << CLIENT_SHIFT;
    /** accept any cert signed by a known CA */
    public static final int CA_SIGNED_CLIENT_CERT
                                        = CA_SIGNED_CERT<< CLIENT_SHIFT;
    /** accept any previously learned cert */
    public static final int KNOWN_CLIENT_CERT   
                                        = KNOWN_CERT    << CLIENT_SHIFT;
    /** if this cert is accepted, add to list of known client certs */
    public static final int LEARN_CLIENT_CERT   
                                        = LEARN_CERT    << CLIENT_SHIFT;
}
