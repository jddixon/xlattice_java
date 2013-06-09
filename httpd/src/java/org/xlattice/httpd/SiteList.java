/* SiteList.java */
package org.xlattice.httpd;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.BufferedReader;

import org.xlattice.CryptoException;
import org.xlattice.SigVerifier;
import org.xlattice.crypto.RSAKey;
import org.xlattice.crypto.RSAPublicKey;
import org.xlattice.crypto.SignedList;
import org.xlattice.util.ArrayStack;
import org.xlattice.util.Base64Coder;

/**
 * Serialized, a site list is a list of Web site names.  The names
 * must end with a File.separator. Lines end with CRLF.
 *
 * XXX TODO: Change to sort entries, using this to eliminate
 * XXX any duplicates.
 *
 * XXX Also ADD PORT NUMBERS with default to 80.
 *
 * @author Jim Dixon
 */

public class SiteList extends SignedList {

    private ArrayStack contents;

    // CONSTRUCTORS AND SUCH ////////////////////////////////////////
    public SiteList (RSAPublicKey pubkey, String title)
                                            throws CryptoException {
        super (pubkey, title);
        contents = new ArrayStack();
    }
    public SiteList (Reader reader)
                                throws CryptoException, IOException {
        super (reader);
    }

    // SignedList METHODS ///////////////////////////////////////////
    /**
     * Read a series of content lines, each consisting of a simple
     * name.  This is an Internet domain name and so may contain no
     * spaces or other delimiters.  The name must end with the
     * file separator (File.separator).
     *
     * The text of the line, excluding the line terminator, is 
     * included in the digest.
     */
    protected void readContents (BufferedReader in)     
                                throws CryptoException, IOException {
        // this is called by super() while still in the constructor
        if (contents == null)
            contents = new ArrayStack();

        String line;
        /////////////////////////////////////////////////////////////
        // XXX SignedList no lnger has a getVerifier() method.  
        // There is however a boolean SignedList.verify().
        // 2011-08-23 uncommented SignedList.getVerifier()
        /////////////////////////////////////////////////////////////
        SigVerifier verifier = getVerifier();
    
        for (line = in.readLine(); line != null && line.length() > 0;
                                            line = in.readLine()) {
            if (line.equals("# END CONTENT #"))
                return;
            verifier.update(line.getBytes());
            line = line.trim();
            contents.push(line);
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
        return (String)contents.peek(n);
    }
    // SiteList-SPECIFIC METHODS ///////////////////////////////////
    /**
     * Add a content line to the SiteList.  The line is just a 
     * domain name terminated by a File.separator.
     *
     * @param name  file or path name of item
     * @return      reference to this SiteList, to ease chaining
     */
    public SiteList add (String name) {
        if (isSigned()) 
            throw new IllegalStateException ("cannot add to signed SiteList");
        if (name == null || name.length() == 0)
            throw new IllegalArgumentException ("null or empty file name");
        name = name.trim();
        contents.push (name);
        return this;
    }
}
