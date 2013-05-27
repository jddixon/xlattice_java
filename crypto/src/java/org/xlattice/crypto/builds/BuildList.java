/* BuildList.java */
package org.xlattice.crypto.builds;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.BufferedReader;
import java.util.ArrayList;

import org.xlattice.CryptoException;
import org.xlattice.SigVerifier;
import org.xlattice.crypto.RSAKey;
import org.xlattice.crypto.RSAPublicKey;
import org.xlattice.crypto.SHA1Digest;
import org.xlattice.crypto.SignedList;
import org.xlattice.util.Base64Coder;

/**
 * Serialized, a build list is a list of files and their extended hashes.
 * Each content line starts with base64-encoded extended hash which is
 * followed by a single space and then the file name, including the
 * path.  Lines end with CRLF.
 *
 * The hash for a serialized BuildList, its title key, is the 20-byte 
 * SignedList hash, an SHA1-based function of the BuildList's title and 
 * RSA public key.
 *
 * The digital signature in the last line is calculated from the
 * SHA1 digest of the header lines (public key, title, and timestamp
 * lines, each CRLF-terminated) and the content lines.
 *
 * @author Jim Dixon
 */

public class BuildList extends SignedList {

    public final static String separator   = "/";
    public final static char separatorChar = '/';

    private ArrayList contents;

    /**
     * Items in the build list: the hash of a file (a content hash
     * or the hash of a SignedList) and the path of the file,
     * including its name.
     */
    private class Item {
        final byte[] ehash;
        final String path;
        Item (byte[] hash, String name) {
            if (hash == null || hash.length == 0)
                throw new IllegalArgumentException("null or empty hash");
            if (name == null || name.length() == 0)
                throw new IllegalArgumentException("null or empty name");
            ehash = hash;
            path  = name;
        }
        public String toString() {
            return new StringBuffer()
                .append(org.xlattice.util.Base64Coder.encode(ehash))
                .append(" ")
                .append(path)
                .toString();
        }
    }
    // CONSTRUCTORS AND SUCH ////////////////////////////////////////
    public BuildList (RSAPublicKey pubkey, String title)
                                            throws CryptoException {
        super (pubkey, title);
        contents = new ArrayList();
    }
    public BuildList (Reader reader)
                                throws CryptoException, IOException {
        super (reader);
    }

    // SignedList ABSTRACT METHODS //////////////////////////////////
    /**
     * Read a series of content lines, each consisting of a hash
     * followed by a space followed by a file name.  The hash is
     * base-64 encoded.  
     *
     * The text of the line, excluding the line terminator, is 
     * included in the digest.
     */
    protected void readContents (BufferedReader in)     
                                throws CryptoException, IOException {
        // this is called by super() while still in the constructor
        if (contents == null)
            contents = new ArrayList();

        String line;
        // algorithm changed in line with Sourceforge bug tracker 1472471
//      SigVerifier verifier = getVerifier();
    
        for (line = in.readLine(); line != null && line.length() > 0;
                                            line = in.readLine()) {
            if (line.equals("# END CONTENT #"))
                return;
            // algorithm changed in line with Sourceforge bug tracker 1472471
//          verifier.update(line.getBytes());
            int spaceAt = line.indexOf(' ');
            if (spaceAt < 0)
                throw new CryptoException("bad content line in BuildList: "
                        + line);
            // XXX ??? XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXxXXXXX
            // XXX NEED BETTER PARSING, WITH MSG FLAGGING THE CONTENT
            // XXX LINE IN ERROR
            // XXX ??? XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXxXXXXX
            Item next = new Item (
                    Base64Coder.decode(line.substring(0, spaceAt)),
                    line.substring(spaceAt + 1));
            contents.add(next);
        }
    }
    /** @return the number of content lines */
    public int size () {
        return contents.size();
    }
    /**
     * @param n zero-based content line index
     * @return the Nth content line in String form 
     */
    public String toString (int n) {
        return ((Item)contents.get(n)).toString();
    }
    // BuildList-SPECIFIC METHODS ///////////////////////////////////
    /**
     * Add a content line to the BuildList.  In string form, the 
     * content line begins with the extended hash of the item
     * (the content hash if it is a data file) followed by a space
     * followed by the name of the item.  If the name is a path,
     * the separator character is a UNIX/Linux-style forward slash,
     * BuildList.separator.
     *
     * XXX MODIFY TO replaceAll(File.separator with BuildList.separator.
     *
     * @param hash  extended hash of item, its file key
     * @param name  file or path name of item
     * @return      reference to this BuildList, to ease chaining
     */
    public BuildList add (byte[] hash, String name) {
        if (hash == null || hash.length == 0)
            throw new IllegalArgumentException ("null or empty hash value");
        if (name == null || name.length() == 0)
            throw new IllegalArgumentException ("null or empty file name");
        if (isSigned()) 
            throw new IllegalStateException ("cannot add to signed BuildList");
        String nameUsed;
        if ( separatorChar == File.separatorChar ) 
            nameUsed = name;
        else 
            nameUsed = name.replaceAll(File.separator, separator);
        contents.add (new Item(hash, nameUsed));
        return this;
    }
    /** @return the SHA1 hash for the Nth item */
    public byte[] getHash(int n) {
        return (byte[])((Item)contents.get(n)).ehash.clone();
    }
    /** 
     * Returns the path + fileName for the Nth content line, in
     * a form usable with the operating system.  That is, the 
     * separator is File.separator instead of BuildList.separator,
     * if there is a difference.
     * 
     * @param n content line 
     * @return the path + file name for the Nth item 
     */
    public String getPath(int n) {
        String path = ((Item)contents.get(n)).path;
        if (separatorChar == File.separatorChar)
            return path;
        else
            return path.replaceAll(separator, File.separator);
    }
}
