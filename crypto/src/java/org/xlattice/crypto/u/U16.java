/* U16.java */
package org.xlattice.crypto.u;

import java.io.File;
import java.io.IOException;

import org.xlattice.CryptoException;
import org.xlattice.crypto.RSAPublicKey;
import org.xlattice.crypto.SignedList;
import static org.xlattice.crypto.u.UConst.*;

import org.xlattice.crypto.digest.SHA1;
import static org.xlattice.util.StringLib.*;

/**
 * 
 * @author Jim Dixon
 */
public class U16 extends U {

    // ACCESS METHODS ///////////////////////////////////////////////
    public int getDirStruc() { return DIR16;   }

    // CONSTRUCTORS /////////////////////////////////////////////////
    /** 
     * If a store exists with the directory name passed, open it.
     * If no such store exists, create it with the default directory
     * structure.
     *
     * @param dirName path to the directory
     */
    protected U16 (String dirName, boolean lazy)    throws IOException {
        super(dirName, DIR16, lazy);

        isOpen = true;;
    }
    // INTERFACE U ////////////////////////////////////////////////// 
    /**
     * After calling this method, no further I/O operations may be
     * performed on the U.
     */
    public void clear()                         throws IOException {
        /* STUB */
    }
    
    // STORE-STORE OPERATIONS ///////////////////////////////////////
    /**
     * Copy keyed files from another U, checking the integrity
     * of each file as it is copied.  If an integrity check fails,
     * the file will not be added to this store.  In particular if
     * a SignedList fails its integrity checks, it will not be 
     * added to this U either by content key or by title key.
     */
    public void copyFrom(U src) {
        /* STUB */
    }
    /**
     * Copy keyed files from another U, checking the integrity
     * of each file as it is copied, and then removing it from the
     * store it is being moved from.  If an integrity check fails,
     * the file will not be moved to this store, it will remain in
     * the U files are being moved from.
     */
    public void moveFrom (U src) {
        /* STUB */
    }
    // OTHER METHODS ////////////////////////////////////////////////
    public StringBuffer chkDirFixPath(StringBuffer sb, String key) {
        sb.append('/')
          .append(key.charAt(0));
        return sb;
    }
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
    /**
     * If a file with the given key is present in the store, return
     * its contents as a byte array.  The file returned might be a
     * byte array with the value passed as a content key, or it might
     * be a serialized SignedList.
     */
    public byte[] get(byte[] key)               throws IOException {
        // STUB
        return null;
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
    // PUTS /////////////////////////////////////////////////////////
    /** add the block of data to the store and return its content key */
    public byte[] put (byte[] data)             throws IOException {
        SHA1 sha1 = new SHA1();
        byte[] key = sha1.digest(data);
        /* STUB - write the data to disk */
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
}
