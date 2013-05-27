/* U.java */
package org.xlattice.crypto.u;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xlattice.util.StringLib;
import org.xlattice.CryptoException;
import org.xlattice.crypto.RSAPublicKey;
import org.xlattice.crypto.SHA1Digest;
import org.xlattice.crypto.SignedList;
import static org.xlattice.crypto.u.UConst.*;

import org.xlattice.crypto.digest.SHA1;
import static org.xlattice.util.StringLib.*;

/**
 * A U store is a hierarchy of directories intended for storing
 * XLattice keyed data files.  Such files either have a content key
 * or a title key.  A content key is the SHA1 hash of a file's
 * contents.  A title key is the SHA1-based key of a SignedList,
 * which is derived from the RSA public key used to create the file
 * and its title.  Both title and content keys are 20-byte values.
 *
 * All U stores contain subdirectories <b>bad/</b> and <b>tmp/</b>.
 * The first of these is used for debugging.  The second is used
 * for collecting data for files which are candidates for inclusion
 * in the store.  That is, the candidate file is first built in
 * <b>tmp/</b>, and then if it is valid it is moved into <b>c/</b>
 * and/or <b>t/</b> as appropriate.  A content-keyed file is valid
 * if the content key of the file in <b>tmp/</b> is the same as
 * the key passed.  A title-keyed file is valid if its digital
 * signature validates.
 *
 * All U stores also contain subdirectories <b>c/</b> and <b>t/</b>.
 * The first of these contains files whose name is or can be derived
 * from the file's <b>content key</b>, the SHA1 hash of its contents.
 * The second contains serialized SignedLists whose name is or can be
 * derived from the list's 20-byte title key.
 *
 * <b>c/</b> and <b>t/</b> may be structured in at least three ways.
 * First, they may be FLAT, meaning that there are no further
 * subdirectorie and the name of the file is its key serialized as
 * a string of hex digits.
 *
 * Secondly, the directory may have a DIR256 structure, meaning that
 * there are 256 subdirectories, each of whose names consists of two
 * hexadecimal digits, one of <b>00/</b> through <b>ff/</b>.  Files
 * are put in the directory corresponding to the first byte, the first
 * two nibbles, of the file's key.  The file's name is its key, less
 * that first byte, in hexadecimal form.
 *
 * The third directory structure is DIR256x16, in which each of the
 * DIR256 subdirectories has a further sixteen subdirectores named
 * <b>0/</b> through <b>f/</b>.  In this case the first byte/two nibbles
 * of the file's key are used to direct one of the 256 mid-level
 * subdirectories, the next nibble is used to select one of the 16
 * surdirectories below that, and the file name is its hex key, less
 * the first three nibbles.  The high-order nibble of the seoond byte
 * is used to select the directory and the low-order nibble is the
 * first hex digit of the file name.
 *
 * This implementation is not thread safe.
 *
 * @author Jim Dixon
 */

abstract class U {

    // INSTANCE VARIABLES ///////////////////////////////////////////
    protected String uDirName;
    protected File   uDir;
    /** content-keyed directory structure type */
    protected int    dirStruc;
    protected boolean isLazy = false;
    protected boolean isOpen;

    public abstract int getDirStruc();

    public static final String U_SIG_FILE = ".u";

    public static void writeSig(U u)        throws IOException {
        String sigFilePath = new StringBuffer(u.uDirName)
                            .append(File.separator)
                            .append(U_SIG_FILE)
                            .toString();
        File sigFile = new File(sigFilePath);
        if(!sigFile.exists()) {
            String s = String.format("disStruc = %s\nlazy = %s\n", 
                                DIR_STRUC_NAMES[u.dirStruc], u.isLazy);
            BufferedWriter out = new BufferedWriter(new FileWriter(sigFile));
            out.write(s);
            out.close();
        } 
    }
    public static USig parseSig(U u)         throws IOException {
        String inLine;
        String sigFilePath = new StringBuffer(u.uDirName)
                            .append(File.separator)
                            .append(U_SIG_FILE)
                            .toString();
        File sigFile = new File(sigFilePath);
        if(sigFile.exists()) {
            Pattern dPat = Pattern.compile("dirStruc\\s*=\\s*(\\w+)");
            Pattern lPat = Pattern.compile("lazy\\s*=\\s*(true|false)");
            int dirStruc     = NO_DIR;
            boolean laziness = true;       // need 3-value logic :-(
            BufferedReader in = new BufferedReader(new FileReader(sigFile));
            String line;
            while ( (line = in.readLine()) != null) {
                Matcher dMatcher = dPat.matcher(line);
                if (dMatcher.matches()) {
                    String d = dMatcher.group();
                    // DEBUG
                    System.out.println("MATCH ON " + d);
                    // END
                    continue;
                }
                Matcher lMatcher = lPat.matcher(line);
                if (lMatcher.matches()) {
                    laziness = lMatcher.group().equals("true");
                    // DEBUG
                    System.out.println("MATCH ON " + lMatcher.group());
                    // END
                    continue;
                }
            }
            in.close();
            return new USig(dirStruc, laziness);
        } else {
            return null;
        }
    }
    // CONSTRUCTORS /////////////////////////////////////////////////
    public static U createU(String dirName, int dirStruc, boolean lazy)
                                                throws IOException {
        if (dirStruc == DEFAULT_DIR)
            dirStruc = DIR256;
        if (dirStruc < FLAT_DIR || DIR256x256 < dirStruc)
            throw new IllegalArgumentException(
                "unknown dirStruc value " + dirStruc);
        U u;
        if (dirStruc == FLAT_DIR)           u = new UFlat(dirName,    lazy);
        else if (dirStruc == DIR16)         u = new U16(dirName,      lazy);
        else if (dirStruc == DIR256)        u = new U256(dirName,     lazy);
        else if (dirStruc == DIR256x16)     u = new U256x16(dirName,  lazy);
        else if (dirStruc == DIR256x256)    u = new U256x256(dirName, lazy);
        else throw new IllegalStateException(
            "dirStruc type out of range: " + dirStruc);
        /* XXX CREATE SIGNATURE FILE */
        return u;
    }
    public static U createU (String dirName, int dirStruc)
                                            throws IOException {
        return createU(dirName, dirStruc, true);   // lazy by default
    }
    /**
     * If a store exists with the directory name passed, open it.
     * If no such store exists, create it with the default directory
     * structure.
     *
     * @param dirName path to the directory
     */
    public static U createU(String dirName)    throws IOException {
        return createU(dirName, DEFAULT_DIR, true);
    }
    /**
     * If a store exists with the directory name passed, open it.
     * If no such store exists, create it with the default directory
     * structure.  In this implementation, if the directory exists,
     * the directory structure codes will be changed to match the
     * actual directory structure.  If either code is DISCOVER, that
     * parameter will be set from the actual directory structure.  If
     * the directory does not exist and the code is DISCOVER, a default
     * value is set (DIR256).
     *
     * @param dirName   path to the directory
     * @param dirStruc  code for directory structure
     */
    U (String dirName, int dirStruc, boolean lazy)
                                                throws IOException {
        if (!validUDirName(dirName))
            throw new IllegalArgumentException(
                    "illegal directory name " + dirName);
        if (dirName.endsWith(File.separator)) {
            dirName = dirName.substring(0, dirName.length() - 1);
        }
        uDirName = dirName;

        uDir = new File(uDirName);  
        if (uDir.exists()) {
            // determine its structure
            /* XXX STUB - DISCOVERY LOGIC */
            this.dirStruc = dirStruc;
        } else {
            // create the directory structure
            if (dirStruc < DISCOVER || dirStruc > DIR256x256)
                throw new IllegalArgumentException(
                        "invalid directory structure code " + dirStruc);
            else if (dirStruc == DISCOVER)
                dirStruc = DIR256;
            this.dirStruc = dirStruc;

            // create the directory structure
            if (!isLazy) {
                switch (dirStruc) {
                    case FLAT_DIR:   mkFlat(uDirName, "");  break;
                    case DIR16:      mkDir16 ("");          break;
                    case DIR256:     mkDir256(  0);     break;
                    case DIR256x16:  mkDir256( 16);     break;
                    case DIR256x256: mkDir256(256);     break;
                    default:
                        throw new IllegalStateException(
                            "unknown directory structure code " + dirStruc);
            }
            }
            mkFlat(uDirName, "bad");
            mkFlat(uDirName, "tmp");

        }
        isLazy = lazy;
        isOpen = true;;
    }
    // PROPERTIES ///////////////////////////////////////////////////
    /**
     * Remove all contained files but leave the directory structure
     * intact.
     */
    public abstract void clear() throws IOException;

    /**
     * After calling this method, no further I/O operations may be
     * performed on the U store.
     */
    public void close()                         throws IOException {
        /* STUB */
        isOpen = false;
    }

    final static void cantRemoveFile(String s, String reason) {
        throw new IllegalStateException("can't remove " + s
                + " -- aborting recursiveRemove");
    }
    /**
     * Recursively remove a directory and all of its contained files,
     * including subdirectories.  Using code must ensure that the
     * parameter contains no illegal substrings, such as "/../",
     * and is otherwise a valid name.
     */
    final static boolean recursiveRemove(String subDirName)
                                                throws IOException {
        File subDir = new File(subDirName);
        // this is a dangerous method, so if anything is doubtful,
        // do nothing
        if (! subDir.exists() )
            cantRemoveFile (subDirName, "no such directory");
        if (! subDir.isDirectory() )
            cantRemoveFile (subDirName, "not a directory");

        String [] files = subDir.list();
        for (int i = 0; i < files.length; i++) {
            String name = files[i];
            if (name.equals(".") || name.equals(".."))
                continue;
            StringBuffer sb = new StringBuffer(subDirName)
                                .append('/')
                                .append(files[i]);
            name = sb.toString();
            File file = new File(name);
            if (! file.exists() )
                cantRemoveFile(name, "no such file");
            if ( file.isDirectory() ) {
                recursiveRemove(name);
            } else {
                if (! file.delete() )
                    cantRemoveFile(name, "file delete failed");
            }
        }
        return  subDir.delete();
    }
    /**
     * Remove the U store, including all subdirectories and contained
     * files, and the root directory for the store.
     */
    public boolean delete()                     throws IOException {
        // this is a dangerous operation, so we require that the
        // U store be in an appropriate state
        if (isOpen)
            return false;
        recursiveRemove(uDirName);
        return true;
    }
    public boolean isOpen () {
        return isOpen;
    }
    // STORE-STORE OPERATIONS ///////////////////////////////////////
    /**
     * Copy keyed files from another store, checking the integrity
     * of each file as it is copied.  If an integrity check fails,
     * the file will not be added to this store.  In particular if
     * a SignedList fails its integrity checks, it will not be
     * added to this store either by content key or by title key.
     */
    public void copyFrom(U src) {
        /* STUB */
    }
    /**
     * Copy keyed files from another store, checking the integrity
     * of each file as it is copied, and then removing it from the
     * store it is being moved from.  If an integrity check fails,
     * the file will not be moved to this store, it will remain in
     * the store files are being moved from.
     */
    public void moveFrom (U src) {
        /* STUB */
    }
    // OTHER METHODS ////////////////////////////////////////////////
    /**
     * Given a string representing the path to
     * U and a hex String representing the content key of value to be stored,
     * append the substring, if any, required for U's directory structure.
     * A U256, for example, would add a slash followed by two hex digits,
     * whereas a U256x256 would add a slash, two hex digits, a slash, and 
     * two more hex digits.  The hex digits must be lower case.
     * 
     * @param sb   path to the base U directory, terminated by a slash
     * @param key  the SHA1 hex content key of the file being accessed
     * @returns    the path to the file being accessed
     */
    public abstract StringBuffer chkDirFixPath(StringBuffer sb, String key);

    /**
     * Returns whether a file with this content key <b>or</b> title
     * key is present in the store.
     *
     * @return whether a file with this key is present in the store
     */
    public boolean contains(byte[] key)         throws IOException {
        // STUB
        return false;
    }
    /**
     * Calculates a title key from the RSA public key and title
     * and returns whether the list is present in the store.
     *
     * @param pubkey public part of the RSA key
     * @param title  the title of the SignedList
     * @return whether a list with the implied title key is present
     */
    public boolean contains(RSAPublicKey pubkey, String title)
                                                throws IOException {
        // STUB
        return false;
    }
    // GETS /////////////////////////////////////////////////////////
    /**
     * If a file with the given SHA1 content key is present in the store, 
     * return its contents as a byte array.  
     */
    public byte[] getData(byte[] key)           throws IOException {
        String keyStr = byteArrayToHex(key);
        // it might save time to clone a public final String  
        StringBuffer sb = new StringBuffer(uDirName).append('/');
        sb = chkDirFixPath( sb, keyStr );
        String pathToFile = sb.append('/').append(keyStr).toString();
        File f = new File(pathToFile);
        if (f.exists()) {
            // XXX CAN EASILY BLOW UP; MUST ALLOW FOR BIG FILES
            long len = f.length(); 
            byte[] inBuf = new byte[(int)len];  // XXX HACK
            FileInputStream in = new FileInputStream(f); 
            // XXX NEEDS TO BE IN A LOOP
            in.read(inBuf, 0, (int)len);
            in.close();
            return inBuf;
        } else {
            return null;                        // file not found
        }
    }
    /**
     * If a file with the given key is present in the store, write
     * its contents to the file named.  The file returned might have
     * binary content whose content key is the value passed, or it
     * might be a serialized SignedList whose title key is the value
     * passed.
     */
    public void get(String fileName, byte[] key)
                                                throws IOException {
        // STUB
    }
    /**
     * If a SignedList with the title key calculated from the RSA
     * public key and title is present in the store, returns it.
     *
     * @param pubkey public part of the RSA key
     * @param title  the title of the SignedList
     * @return the SignedList or null
     */
    public SignedList get(RSAPublicKey pubkey, String title)
                                throws CryptoException, IOException {
        // STUB
        return null;
    }
    // PUTS ////////////////////////////////////////////////////////

    /** add the block of data to the store and return its content key */
    public byte[] putData (byte[] data) throws IOException {
        SHA1 sha1 = new SHA1();
        byte[] key = sha1.digest(data);
        String keyStr = byteArrayToHex(key);
        StringBuffer sb = new StringBuffer(uDirName);
        sb = chkDirFixPath(sb, keyStr);
        String dirName = sb.toString(); 
        File dir = new File(dirName);
        if (isLazy && !dir.exists()) 
            dir.mkdirs();
        // DEBUG XXX
        if (!dir.exists()) {
            System.out.println("*** " + (isLazy ? "LAZY AND " : "NOT LAZY BUT ")
                + "DIRECTORY DOES NOT EXIST: " + dirName);
            if (dirName.endsWith("/")) {
                String foo = dirName.substring(0, dirName.length()-1);
                File fooFile = new File(foo);
                if (!fooFile.exists()) {
                    System.out.println ("*** " + foo + " DOESN'T EXIST EITHER");
                    fooFile.mkdirs();
                    System.out.println("    - SO I MADE IT");
                }
            }
        }
        //
        sb.append('/').append(keyStr);
        String fileName = sb.toString();
        FileOutputStream o = new FileOutputStream(fileName); 
        o.write(data);
        o.close();
        return key;
    }
    /**
     * Write a block of data into the store, validating it against
     * its content key.
     *
     * IMPLEMENTATION DETAIL: the data passed is written to a
     * temporary file under <b>tmp/</b>.  It is then validated
     * against the key passed.  If validation is successful, it
     * is then moved into <b>c/</b> unldess the data is already
     * present.  If validation fails, the temporary file is either
     * deleted or moved to <b>bad/</b>.
     *
     * @param key  its SHA1 content key
     * @param data the data to be stored
     * @return whether the data was already present.
     */
    public boolean put (byte[] key, byte[] data)
                                                throws IOException {
        // STUB
        return false;
    }
    /**
     * Write the contents of a file into the store, validating it
     * against its content key.
     *
     * @param key      its SHA1 content key
     * @param fileName name of the file the data is found in
     * @return whether the data was already present
     */
    public boolean put (byte[] key, String fileName)
                                                throws IOException {
        return put (key, new File(fileName));
    }
    /**
     * Write the contents of a file into the store, validating it
     * against its content key.
     *
     * @param key  its SHA1 content key
     * @param file contains the data to be written
     * @return whether the data was already present
     */
    public boolean put (byte[] key, File file)  throws IOException {
        // STUB
        return false;
    }
    /**
     * Write a serialized SignedList into the store, after validating
     * its digital signature and verifying that it is more recent than
     * any pre-existing SignedList with the same title key.  After a
     * successful write, the list will be retrievable either by content
     * key or by title key and the method returns <b>true</b>.  If the
     * same SignedList is already present or a more recent SignedList
     * with the same title key is present, the method returns <b>false</b>.
     * In the latter case the content key will have changed.
     *
     * IMPLEMENTATION DETAIL: under Linux/UNIX, if validation is
     * successful and if the SignedList is more recent than any
     * with the same title key, the serialized list is written to
     * <b>c/</b> under its content key and then a link is created
     * to the content key file from the title key under <b>t/</b>.
     *
     * @param list  the SignedList to be stored
     * @return whether the operation succeeded
     */
    public boolean put (byte[] key, SignedList list)
                                                throws IOException {
        // STUB
        return false;
    }
    // IMPLEMENTATION METHODS ///////////////////////////////////////
    protected final boolean badSubString(String s) {
        for (int i = 0; i < BAD_SUBSTRINGS.length; i++)
            if (s.indexOf(BAD_SUBSTRINGS[i]) != -1)
                return true;
        return false;
    }

    protected final boolean validUDirName(String s) {
        if (s == null || s.equals(""))
            return false;
        for (int i = 0; i < BAD_FILENAMES.length; i++)
            if (s.equals(BAD_FILENAMES[i]))
                return false;
        return ! badSubString(s);
    }
    protected final String mkFlat (String baseName, String subDirName)
                                                throws IOException {
        StringBuffer sb = new StringBuffer(baseName);
        if (!("".equals(subDirName)))
            sb.append(File.separatorChar)
              .append(subDirName);
        String name = sb.toString();
        File dir = new File(name);
        if(! dir.mkdirs() )
            throw new IOException("can't create '" + name + "'");
        return name;
    }
    /**
     * Create a DIR16 subdirectory and its 16 subdirectories,
     * <b>0/</b> through <b>f/<b>.  <b>uDir</b> must already exist.
     *
     * If the second parameter is true, 16 sub-subdirectories are
     * created below each of the 256 subdirectories, <b>0/0/</b>
     * through <b>f/f/</b>.
     *
     * @param subDirName name of subdirectory; may be ""
     */
    protected final void mkDir16(String subDirName)
                                                throws IOException {
        String relPath = mkFlat(uDirName, subDirName);
        for (int i = 0; i < 16; i++) {
            String subSubDirName = mkFlat(relPath, DIR16NAMES[i]);
        }
    }
    /**
     * Populate a DIR256 directory with its 256 subdirectories,
     * <b>00/</b> through <b>ff/<b>.  <b>uDir</b> must already exist.
     *
     * If the second parameter is non-zero, create that many 
     * sub-subdirectories within each subdirectory.
     *
     * @param subDirName name of subdirectory, c/ or t/
     * @param make16too  whether to create 16 subdirectories below that
     */

    protected final void mkDir256(int width)
                                                throws IOException {
        for (int i = 0; i < 256; i++) {
            String subDirName = mkFlat(uDirName, DIR256NAMES[i]);
            if (width == 16) 
                for (int j = 0; j < 16; j++) 
                    mkFlat(subDirName, DIR16NAMES[j]);
            else if (width == 256)
                for (int j = 0; j < 256; j++) 
                    mkFlat(subDirName, DIR256NAMES[j]);
            else if (width!=0)
                throw new IllegalStateException(
                    "mkDir256 but width is " + width);
        }
    }
    // ACCESSOR METHODS /////////////////////////////////////////////
    public boolean isLazy() {
        return isLazy;
    }
}
