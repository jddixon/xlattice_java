/* SignedList.java */
package org.xlattice.crypto;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.Reader;

/**
 * @author Jim Dixon
 **/

import org.xlattice.CryptoException;
import org.xlattice.DigSigner;
import org.xlattice.SigVerifier;
import org.xlattice.util.Base64Coder;
import org.xlattice.util.Timestamp;

/**
 * In its serialized form a SignedList consists of a public key line,
 * a title line, a timestamp line, a number of content lines, and a
 * digital signature.  Each of the lines ends with a CR-LF sequence.
 * A blank line follows the last content line.  The timestamp (in
 * CCYY-MM-DD HH:MM:SS form) represents the time at which the list
 * was signed using the RSA private key corresponding to the key in
 * the public key line.  The public key itself is base-64 encoded.  
 *
 * The SHA1withRSA digital signature is on the entire SignedList excluding 
 * the digital signature line.  All line endings are converted to 
 * CRLF before taking the digital signature.
 * 
 * The SignedList itself has a 20-byte extended hash, the 20-byte SHA1 
 * digest of a function of the public key and the title.  This means
 * that the owner of the RSA key can create any number of documents
 * with the same hash but different timestamps with the intention 
 * being that users can choose to regard the document with the most
 * recent timestamp as authentic.
 *
 * What the content line contains varies between subclasses.
 *
 * @author <A HREF="mailto:jddixon@users.sourceforge.net">Jim Dixon</A>
 */
public abstract class SignedList {

    public static final int BUF_SIZE = 4096;

    private Base64Coder coder = new Base64Coder();

    private RSAPublicKey pubkey;
    /** cache the encoded form */
    private String encodedKey       = "* INVALID PUBLIC KEY *\r\n"; 
    
    private String title;
    /** set when signed */
    private Timestamp timestamp;
    /** cache the String form */
    private String encodedTimestamp = "* LIST HAS NOT BEEN SIGNED *";
    
    private byte[] digSig;

    private final SigVerifier verifier;

    private SHA1Digest sha1 = new SHA1Digest();

    public final static String CRLF = "\r\n";

    // CONSTRUCTORS /////////////////////////////////////////////////
    private SignedList ()                   throws CryptoException {
        verifier = new SHA1withRSAVerifier();
    }

    protected SignedList (RSAPublicKey publicKey, String title)
                                            throws CryptoException {
        this();
        if (publicKey == null)
            throw new IllegalArgumentException("key must not be null");
        pubkey = new RSAPublicKey (
                        publicKey.getModulus(), publicKey.getExponent());
        this.title  = title;
    }
    protected SignedList (Reader reader)        
                                throws CryptoException, IOException {
        this();
        if (reader == null)
            throw new IllegalArgumentException ("null reader");
        BufferedReader in = new BufferedReader (reader);
        
        // READ HEADER ////////////////////////////////////
        // RSA public key and title
        String line;
        encodedKey = readFoldedLine (in, false);
        pubkey     = Key64Coder.decodeRSAPublicKey(encodedKey);
        verifier.init(pubkey);              // 2005-03-04
        title      = in.readLine();
        encodedTimestamp = in.readLine();
        try {
            timestamp = new Timestamp (encodedTimestamp);
        } catch (java.text.ParseException pe) {
            throw new CryptoException ("cannot decode timestamp: "
                    + encodedTimestamp + " - " + pe);
        }
       
        // READ CONTENTS //////////////////////////////////
        // read the content (handled by subclass)
        line = in.readLine();
        if (!line.equals("# BEGIN CONTENT #"))
            throw new CryptoException("missing BEGIN CONTENT line");
        readContents (in);

        // READ FOOTER ////////////////////////////////////
        // XXX Modify to use readFoldedLine()
        digSig = Base64Coder.decode(in.readLine());
    }
    // PROPERTIES ///////////////////////////////////////////////////
    /** @return a clone of the public key */
    public final RSAPublicKey getPublicKey() {
        return new RSAPublicKey(pubkey);
    }
    public final String getTitle() {
        return title;
    }

    // algorithm changed in line with Sourceforge tracker bug 1472471;
    // subclasses no longer have access to the verifier
    // 2011-08-23 FIX/HACK uncommented this method
    protected SigVerifier getVerifier() {
        return verifier;
    }

    public final boolean isSigned () {
        return digSig != null;
    }
    // UTILITY METHODS //////////////////////////////////////////////
    /**
     * Read lines until one is found that does not begin with a space.
     * The lines beginning with a space are added to the StringBuffer.
     * The first line found not beginning with a space is returned.
     * 
     * XXX This should find a better home.
     *
     * @param in     open BufferedReader
     * @param unfold unfold the line if true
     * @return       the collected line
     */
    public static String readFoldedLine(
            BufferedReader in, boolean unfold) throws IOException {

        String line = in.readLine(); 
        if (line.charAt(0) == ' ')
            throw new IllegalStateException (
                    "first line begins with space");
        in.mark(BUF_SIZE);
        StringBuffer sb = new StringBuffer(line);

        for (line = in.readLine(); line != null; line = in.readLine()) {
            if ( line.charAt(0) == ' ' ) {
                if (unfold)
                    sb.append(line.substring(1));
                else
                    sb.append(CRLF).append(line);
            }
            else {
                in.reset();
                break;
            }
            in.mark(BUF_SIZE);
        }
        return sb.toString();
    } 
    // OTHER METHODS ////////////////////////////////////////////////
    /** 
     * Return this SignedList's hash, a byte array 20 bytes
     * long.  
     *
     * The exact algorithm used is critical.  It should not be
     * changed except perhaps at major version number changes.
     *
     * @return the hash of the SignedList
     */
    public byte[] getHash() {
        byte[] b = pubkey.getModulus().toByteArray();
        sha1.reset();
        byte[] t = sha1.digest(title.getBytes());
        byte[] x;
        int len;
        if (b.length < t.length) {
            x = t;          // longer array
            len = b.length; // length of shorter
        } else {
            x = b;
            len = t.length;
        }
        for (int i = 0; i < len; i++)
            x[i] = (byte)(b[i] ^ t[i]);
        sha1.reset();
        byte[] hash  = sha1.digest(x);
        return hash;
    }
    
    /**
     * Subclasses must read in content lines, stripping off line 
     * endings
     * do a verifier.update(line), where line excludes any terminating
     * CRLF.
     *
     * @throws CryptoException if error in content lines
     */
    protected abstract void readContents (BufferedReader in)
                                throws CryptoException, IOException ;

    /**
     * Set a timestamp and calculate a digital signature.  First
     * calculate the SHA1 hash of the pubkey, title, timestamp,
     * and content lines, excluding the terminating CRLF in each
     * case, then encrypt that using the RSA private key supplied.
     *
     * @param key RSAKey whose secret materials are used to sign
     */
    public final void sign (RSAKey key)  throws CryptoException {
        if (digSig != null)
            throw new CryptoException ("SignedList has already been signed_");
        if (key == null)
            throw new IllegalArgumentException("null RSA key");
        DigSigner signer = key.getSigner("sha1");

        /////////////////////////////////////////////////////////////
        // algorithm changed in line with Sourceforge tracker bug 1472471
        /////////////////////////////////////////////////////////////

        // add RSA public key and title
        try {
            encodedKey = Key64Coder.encodeRSAPublicKey (pubkey);
        } catch (CryptoException e) { /* otherwise ignored */ }
//      signer.update(encodedKey.getBytes())
//            .update(title.getBytes());
        
        // generate the timestamp
        timestamp = new Timestamp();
        encodedTimestamp = timestamp.toString();
        
//      signer.update(encodedTimestamp.getBytes());

//      // add content lines, one by one
//      for (int i = 0; i < size(); i++) 
//          signer.update (toString(i).getBytes());

        signer.update( withoutDigSig().toString().getBytes() );
        digSig = signer.sign();
    }

    /**
     * The number of items in the list, excluding the header lines
     * (public key, title, timestamp) and the footer lines (blank
     * line, digital signature).
     *
     * @return the number of content items
     */
    public abstract int size ();

    /**
     * Verify that the SignedList agrees with its digital signature.
     *
     * @return       true if the digital signature agrees with the
     *                  public key
     */
    public boolean verify()                 throws CryptoException {
        if (digSig == null) {
            return false;
        }
        verifier.init (pubkey);
        
        /////////////////////////////////////////////////////////////
        // algorithm changed in line with Sourceforge tracker bug 1472471
        /////////////////////////////////////////////////////////////

//      // XXX THIS ENCODEDKEY IS FOLDED
//      verifier.update(encodedKey.getBytes());
//      verifier.update(title.getBytes())
//              .update(encodedTimestamp.getBytes());
//      for (int i = 0; i < size(); i++)
//          verifier.update(toString(i).getBytes());

        verifier.update( withoutDigSig().toString().getBytes() );
        return verifier.verify(digSig);
    }
    // SERIALIZATION ////////////////////////////////////////////////
    protected final StringBuffer withoutDigSig() {
        StringBuffer sb = new StringBuffer();
        
        // HEADER
        sb.append(encodedKey)         .append(CRLF)
          .append(title)              .append(CRLF)
          .append(encodedTimestamp)   .append(CRLF)
          .append("# BEGIN CONTENT #").append(CRLF);
        
        // CONTENTS
        for (int i = 0; i < size(); i++)
            sb.append(toString(i))  .append(CRLF);
        
        sb.append("# END CONTENT #").append(CRLF);
        return sb;
    }
    /**
     * Serialize the entire document.  All lines are CRLF-terminated.
     * Subclasses are responsible for formatting their content lines,
     * without any termination.
     */
    public final String toString () {
        return withoutDigSig()
            // FOOTER XXX dig sig needs to be folded
            .append (Base64Coder.encode(digSig))
            .append(CRLF)
            .toString();
        
    }
    /**
     * Nth content item in String form, without any terminating
     * CRLF.
     *
     * @param n index of content item to be serialized
     * @return  serialized content item
     */
    public abstract String toString (int n);

}
