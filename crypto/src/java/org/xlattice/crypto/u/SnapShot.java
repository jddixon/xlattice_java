/* SnapShot.java */
package org.xlattice.crypto.u;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.BufferedReader;
import java.util.ArrayList;

import org.xlattice.CryptoException;
import org.xlattice.crypto.RSAPublicKey;
import org.xlattice.crypto.SignedList;
import org.xlattice.util.Base64Coder;

/**
 * Serialized, a SnapShot is a list of files, their attributes, and their 
 * content hashes.
 * 
 * Each content line starts with a file's base64-encoded content hash.  
 * This is followed by its UNIX/Linus permissions, ownership, a timestamp, 
 * and then the file name, including the path relative to the current
 * directory from which the snapshot was taken.  Items are separated
 * by a single space.  Lines end with CRLF.
 *
 * The content hash is a sequence of 28 characters representing the 
 * 20-byte SHA1 content hash for the data in the file.
 * 
 * Permissions are represented by a octal number.  This will normally
 * be 3 digits, representing in order owner, group, and world permissions.
 * As is conventional, permissions are added up, with 1 representing
 * execute, 2 write, and 4 read, so for example 6 represents permission
 * to read and write but not execute, and 124 would mean that the owner
 * could execute, group members could write, and others could read.
 *
 * Ownership is in "id1.id2" form, where the first id is the user 
 * name (login) and the second the group name.  A common example is
 * <b>root.wheel</b>, the superuser and the "wheel" group.
 * 
 * The timestamp is an integer value, a 64-bit long representing
 * the number of milliseconds from the epoch, 1970-01-01 00:00:00 UTC.
 * 
 * The file name may neither begin nor end with a file separator
 * (the forward slash as under UNIX), nor may it contain spaces or other
 * non-printing characters.  These may be HTTP-escaped, so that for
 * example a space in a file name is represented by %20.  (HTTP escape
 * codes are NOT YET SUPPORTED in this implementation.)
 * 
 * A typical line then would look like
 *   <b>abcdef123456ghijkl789012mnop 644 www.www 1095357343 foo/bar</b>
 * 
 * The hash for a serialized SnapShot, its title key, is the 20-byte 
 * SignedList hash, an SHA1 digest of the SnapShot's RSA public key 
 * and title, both seen as byte arrays.
 *
 * The digital signature in the last line is calculated from the
 * SHA1 digest of the header lines (public key, title, and timestamp
 * lines) and the content lines, excluding the line endings.  The
 * canonical line ending is a two character CR-LF sequence and 
 * XLattice software will always write the line ending in this form.
 *
 * @author Jim Dixon
 */

public class SnapShot extends SignedList {

    public final static String separator   = "/";
    public final static char separatorChar = '/';

    private ArrayList contents;

    /** 
     * This is Java, so we have no way of setting or reading these
     * without resorting to JNLI.  For now, just use the same values
     * on all files.  0644 is owner read/write, everybody else read 
     * only, no one gets to execute.
     */
    private int permissions = 0644;    
    private String owner;
    private String group;
    
    /**
     * Items in the build list: the hash of a file (a content hash
     * or the hash of a SignedList) and the path of the file,
     * including its name.
     *
     * @param ehash       SHA1 digest, content key of the file
     * @param permissions 
     * @param owner
     * @param group
     * @param timestamp   modification date, ms since the epoch
     * @param path        file name, including path from 'here'
     */
    private class Item {
        final byte[] ehash;         // BAD NAME
        final int permissions;      // NEW
        final String owner;         // NEW
        final String group;         // NEW
        final long timestamp;       // NEW
        final String path;
        Item (byte[] hash, 
                int permissions, String owner, String group,
                long timestamp, String name) {
            if (hash == null || hash.length == 0)
                throw new IllegalArgumentException("null or empty hash");
            ehash = hash;
            
            if (permissions == -1)
                permissions = 0644;
            this.permissions = permissions;
            
            if (owner == null || owner.length() == 0)
                throw new IllegalArgumentException("null or empty owner");
            this.owner = owner;
            
            if (group == null || group.length() == 0)
                throw new IllegalArgumentException("null or empty group");
            this.group = group;
            
            this.timestamp = timestamp;
            
            if (name == null || name.length() == 0)
                throw new IllegalArgumentException("null or empty name");
            if (name.startsWith(separator) || name.endsWith(separator))
                throw new IllegalArgumentException(
                        "illegal path to file: " + name);
            path  = name;
        }
        public String toString() {
            return new StringBuffer()
                .append(org.xlattice.util.Base64Coder.encode(ehash))
                .append(' ')
                .append( Integer.toString(permissions, 8) )
                .append(' ')
                .append(owner)  .append('.')    .append(group)
                .append(' ')
                .append( Long.toString(timestamp) )
                .append(' ')
                .append(path)
                .toString();
        }
    }
    // CONSTRUCTORS AND SUCH ////////////////////////////////////////
    public SnapShot (RSAPublicKey pubkey, String title)
                                            throws CryptoException {
        super (pubkey, title);
        contents = new ArrayList();
    }
    public SnapShot (Reader reader)
                                throws CryptoException, IOException {
        super (reader);
    }
    // PROPERTIES ///////////////////////////////////////////////////
    /**
     * Sets default permissions, owner, and group for all files.
     * If the permssions parameter is -1, it defaults to 0644.
     */
    public void setDefaults (int permissions, String owner, String group) {
        // XXX validate permissions
        if (permissions == -1)
            permissions = 0644;
        this.permissions = permissions;
        
        if (owner == null || owner.length() == 0)
            throw new IllegalArgumentException("null or empty owner");
        // XXX NEED BETTER CHECKS
        this.owner = owner;
        
        if (group == null || group.length() == 0)
            throw new IllegalArgumentException("null or empty group");
        // XXX NEED BETTER CHECKS
        this.group = group;
    }
    /**
     * Sets the owner and group to the same value, leaves default
     * permissions unchanged.
     */
    public void setDefaults (String owner) {
        if (owner == null || owner.length() == 0)
            throw new IllegalArgumentException("null or empty owner");
        this.owner = owner;
        group      = owner;
    }
    // SignedList ABSTRACT METHODS /////////////////////////////////
    private int nextSpace(String line, int from, String afterWhat) 
                                                throws CryptoException {
        int nextSpaceAt = line.indexOf(' ', from);
        if (nextSpaceAt == -1)
            throw new CryptoException("no space after " + afterWhat 
                    + " in line:\n\t" + line);
        return nextSpaceAt;
    }
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
    
        for (line = in.readLine(); line != null && line.length() > 0;
                                            line = in.readLine()) {
            if (line.equals("# END CONTENT #"))
                return;

            byte[] digest;
            int    permission;
            String owner;
            String group;
            long   modTime;
            String relPath;

            // content hash ///////////////////////////////
            int spaceAt = nextSpace(line, 0, "content hash");
            digest = Base64Coder.decode(line.substring(0, spaceAt));
            // permissions ////////////////////////////////
            int nextOne = nextSpace(line, ++spaceAt, "permissions");
            permission = Integer.parseInt(line.substring(spaceAt, nextOne), 8);
            spaceAt = nextOne;
            // ownership //////////////////////////////////
            nextOne = nextSpace(line, ++spaceAt, "ownership");
            String ownership = line.substring(spaceAt, nextOne);
            int dotAt = ownership.indexOf('.');
            if (dotAt == -1)
                throw new CryptoException("missing dot in ownership: "
                        + ownership);
            // XXX various pathological possibilities 
            owner = ownership.substring(0, dotAt);
            group = ownership.substring(++dotAt);
            spaceAt = nextOne;
            // timestamp //////////////////////////////////
            nextOne = nextSpace(line, ++spaceAt, "timestamp");
            modTime = Long.parseLong(line.substring(spaceAt, nextOne));
            spaceAt = nextOne;
            // path to file ///////////////////////////////
            relPath = line.substring(++spaceAt);
            Item next = new Item (
                    digest,
                    permission, owner, group, modTime, relPath
                    );
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
    // SnapShot-SPECIFIC METHODS ///////////////////////////////////
    /**
     * Add a content line to the SnapShot.  In string form, the 
     * content line begins with the extended hash of the item
     * (the content hash if it is a data file) followed by a space
     * followed by the name of the item.  If the name is a path,
     * the separator character is a UNIX/Linux-style forward slash,
     * SnapShot.separator.
     *
     * XXX MODIFY TO replaceAll(File.separator with SnapShot.separator.
     *
     * @param hash  content hash of item, its SHA1 content key
     * @param name  file or path name of item
     * @return      reference to this SnapShot, to ease chaining
     */
    public SnapShot add (byte[] hash, int permission, 
            String owner, String group, long timestamp, String name) {
        if (isSigned()) 
            throw new IllegalStateException ("cannot add to signed SnapShot");
        
        if (hash == null || hash.length == 0)
            throw new IllegalArgumentException ("null or empty hash value");
        if (name == null || name.length() == 0)
            throw new IllegalArgumentException ("null or empty file name");
        String nameUsed;
        if ( separatorChar == File.separatorChar ) 
            nameUsed = name;
        else 
            nameUsed = name.replaceAll(File.separator, separator);
        contents.add (new Item(hash, permission, owner, group,
                                                 timestamp, nameUsed));
        return this;
    }
    /** @return the SHA1 hash for the Nth item */
    public byte[] getHash(int n) {
        return (byte[])((Item)contents.get(n)).ehash.clone();
    }
    /** 
     * Returns the path + fileName for the Nth content line, in
     * a form usable with the operating system.  That is, the 
     * separator is File.separator instead of SnapShot.separator,
     * if there is a difference.
     * 
     * @param n  content line 
     * @return the path + file name for the Nth item 
     */
    public String getPath(int n) {
        String path = ((Item)contents.get(n)).path;
        if (separatorChar == File.separatorChar)
            return path;
        else
            return path.replaceAll(separator, File.separator);
    }
    // OTHER IMPLEMENTATION METHODS /////////////////////////////////
    public String encodePermissions(int n) {
        /* STUB */
        return "644";
    }
    public int decodePermissions(String s) {
        /* STUB */
        return 0644;
    }
    public boolean validOwner (String owner) {
        /* STUB */
        return false;
    }
    public boolean validGroup (String group) {
        return validOwner(group);
    }
    
}
