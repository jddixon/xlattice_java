/* RSAInfo.java */
package org.xlattice.node;

import java.math.BigInteger;
import org.xlattice.crypto.RSAKey;
import org.xlattice.util.Base64Coder;

/**
 * Helper class for collecting RSA key information from an XML file
 * and returning it.  Convenience functions are provided to convert
 * between base64 encoding and the BigInteger forms of the values.
 * 
 * An RSA key is fully specified by three values n, e, and d, but
 * more conveniently by four, replacing n with its prime factors p
 * and q.  These four values (p, q, e, and d) are stored on disk 
 * in base64-encoded form, but the RSAKey class uses BigInteger
 * values, so we provide methods using both representations.
 * 
 * @deprecated This functionality has been moved to org.xlattice.crypto.
 *
 * @author Jim Dixon
 */
public class RSAInfo {

    private static Base64Coder coder = new Base64Coder();
    
    private String p_;
    private String q_;
    private String e_;
    private String d_;

    // CONSTRUCTORS /////////////////////////////////////////////////
    public RSAInfo () {}

    public RSAInfo (RSAKey key) {
        if (key == null)
            throw new IllegalArgumentException("null RSA key");
        setBigP (key.getP());
        setBigQ (key.getQ());
        setBigE (key.getE());
        setBigD (key.getD());
    }

    public String getP() {
        return p_;
    }
    public void setP(String p) {
        p_ = p;
    }
    /** 
     * Get the value in BigInteger format, decoding the base64 String.
     */
    public BigInteger getBigP() {
        return new BigInteger ( coder.decode(p_) );
    }
    public void setBigP(BigInteger bigP) {
        p_ = coder.encode ( bigP.toByteArray() );
    }
    
    public String getQ() {
        return q_;
    }
    public void setQ(String q) {
        q_ = q;
    }
    /** 
     * Get the value in BigInteger format, decoding the base64 String.
     */
    public BigInteger getBigQ() {
        return new BigInteger ( coder.decode(q_) );
    }
    public void setBigQ(BigInteger bigQ) {
        q_ = coder.encode ( bigQ.toByteArray() );
    }
    
    public String getE() {
        return e_;
    }
    public void setE(String e) {
        e_ = e;
    }
    /** 
     * Get the value in BigInteger format, decoding the base64 String.
     */
    public BigInteger getBigE() {
        return new BigInteger ( coder.decode(e_) );
    }
    public void setBigE(BigInteger bigE) {
        e_ = coder.encode ( bigE.toByteArray() );
    }
  

    public String getD() {
        return d_;
    }
    public void setD(String d) {
        d_ = d;
    }
    /** 
     * Get the value in BigInteger format, decoding the base64 String.
     */
    public BigInteger getBigD() {
        return new BigInteger ( coder.decode(d_) );
    }
    public void setBigD(BigInteger bigD) {
        d_ = coder.encode ( bigD.toByteArray() );
    }

    /**
     * Quick and dirty implementation.
     */
    public String toString() {
        return new StringBuffer("RSAInfo\n")
            .append("  P: ").append(p_).append('\n')
            .append("  Q: ").append(q_).append('\n')
            .append("  E: ").append(e_).append('\n')
            .append("  D: ").append(d_)
            .toString();
    }
}
