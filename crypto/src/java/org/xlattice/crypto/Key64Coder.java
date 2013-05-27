/* Key64Coder.java */
package org.xlattice.crypto;

import java.math.BigInteger;

/**
 * @author Jim Dixon
 **/

import org.xlattice.CryptoException;
import org.xlattice.util.Base64Coder;

/**
 * Methods for base64 encoding and decoding public key values.  
 *
 * @author < A HREF="mailto:jddixon@users.sourceforge.net">Jim Dixon</A>
 */
public class Key64Coder {

    public final static int LINE_LENGTH = 76;
    public final static int MAX_PREFIX  = LINE_LENGTH - 1;
    public final static String CRLF = "\r\n";

    // CONSTRUCTORS /////////////////////////////////////////////////
    public Key64Coder() {}

    // OTHER METHODS ////////////////////////////////////////////////
    /**
     * Construct a line consisting of a prefix, followed by a space,
     * followed by a base64-encoded binary value, followed by a space,
     * followed by a positive integer value.  If the resulting 
     * String is longer than LINE_LENGTH characters, a line break is
     * inserted and the next line is indented by a single space.
     *
     * The integer value must not exceed 1 million, a completely 
     * arbitrary restriction.
     *
     * XXX This implementation is not efficient -- AND IT'S TOO FIDDLY;
     * XXX MODIFY TO NOT TREAT EXPONENT DIFFERENTLY.  That is, just 
     * XXX wrap at 76 characters, disregarding exact location of e.
     *
     * @param prefix alphanumeric value (algorithm name)
     * @param b      binary value to be encoded
     * @param i      positive integer value, less than one million
     */
    public static String prefixEncodeAndFold (
                                        String prefix, byte[] b, int i) {
        if (prefix == null || prefix.equals(""))
            throw new IllegalArgumentException ("null or empty prefix");
        if (prefix.length() > MAX_PREFIX)
            throw new IllegalArgumentException (
                    "prefix length is " + prefix.length() 
                    + " but may not exceed " + MAX_PREFIX);
        if (b == null)
            throw new IllegalArgumentException ("null byte array");
        if (i < 1)
            throw new IllegalArgumentException ("int param must be positive");
        if (i >= 1000000)
            throw new IllegalArgumentException (
                                "int param must be less than 1 million");

        int iLen = (i <      10 ? 1 : 
                   (i <     100 ? 2 :
                   (i <    1000 ? 3 :
                   (i <   10000 ? 4 :
                   (i <  100000 ? 5 :
                   (i < 1000000 ? 6 : 7))))));

        // begin with prefix ...
        StringBuffer sb = new StringBuffer (prefix).append(" ");
        int curLength   = sb.length();
        String encoded  = Base64Coder.encode(b);
      
        // do the folding
        for (int offset = 0; offset < encoded.length();  ) {
            int subLen = encoded.substring(offset).length();
            if (curLength + subLen <= LINE_LENGTH){
                sb.append(encoded.substring(offset));
                offset += LINE_LENGTH;      // gets us out of loop
                curLength += subLen;
            } else {
                sb.append( encoded.substring(offset, 
                                        offset + LINE_LENGTH - curLength));
                offset += (LINE_LENGTH - curLength);
                sb.append(CRLF).append(" ");
                curLength = 1;
            }
        }
        // don't break up the int value
        if (curLength + 1 + iLen > LINE_LENGTH)
            sb.append(CRLF).append(" ");
        sb.append(" ").append(i);
       
        return sb.toString();
    }
    /**
     * Base64-encode an RSAPublicKey.  The representation of
     * the modulus is big-endian.  The public exponent is silently
     * converted to an int, a narrowing conversion which possibly
     * loses precision and changes the sign of the exponent.
     * 
     * The String returned looks like "rsa NNN...N E" where
     * NNN..N represents the base64-encoded modulus and E the 
     * exponent in decimal form.  Spaces separate "rsa" and the
     * two numeric values.  If the number of printing characters 
     * in any line would exceed LINE_LENGTH, a CR-LF-SPACE sequence 
     * is inserted.
     */
    public static String encodeRSAPublicKey (RSAPublicKey k)
                                            throws CryptoException {
        if (k == null) 
            throw new IllegalArgumentException ("null public key");
        BigInteger n = k.getModulus();
        int e = k.getExponent().intValue();
        return prefixEncodeAndFold("rsa", n.toByteArray(), e);
    }
    /**
     * Decode a base64-encoded RSAPublicKey.  The key has been
     * encoded as described above, "rsa NNN...N E", with line
     * lengths kept to LINE_LENGTH by inserting CR-LF-SPACE 
     * sequences and the entire String CR-LF terminated.
     */
    public static RSAPublicKey decodeRSAPublicKey(String s) 
                                            throws CryptoException {
        if (s == null)
            throw new IllegalArgumentException (
                    "encoded key is null or empty");
        
        // XXX MOVE THIS UNFOLDING ELSEWHERE
        s = s.trim()                    // gets rid of trailing CRLF
             .replaceAll("\r\n ","");   // ... and CR-LF-SPACEs
        if (s.length() == 0)
            throw new IllegalArgumentException("empty public key");
        
        if (!s.startsWith("rsa "))
            throw new CryptoException("public key does not have rsa prefix");
        int eOffset = s.lastIndexOf(" ");
        if (eOffset == -1)
            throw new CryptoException("missing exponent");
        BigInteger e;
        try {
            e = new BigInteger (s.substring(eOffset + 1));
        } catch (NumberFormatException nfe) {
            throw new CryptoException (
                    "public key exponent is not a number: <" 
                    + s.substring(eOffset + 1) + ">");
        }
        String stripped = s.substring(4, eOffset);
        byte[] b = Base64Coder.decode(stripped);
        return new RSAPublicKey (new BigInteger (b), e);
    }
}
