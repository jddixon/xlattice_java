/* AbstractDisk.java */
package org.xlattice.overlay.datakeyed;

import java.io.IOException;
import java.io.File;
import java.nio.ByteBuffer;

/**
 * @author Jim Dixon
 **/

import org.xlattice.CryptoException;
import org.xlattice.NodeID;
import org.xlattice.crypto.SignedList;
import org.xlattice.overlay.DataKeyed;
import org.xlattice.util.NonBlockingLog;

public abstract class AbstractDisk implements DataKeyed {

    /** standard location for XLattice store */
    public final static String PATH_TO_STORE = "xlattice/overlays/store/";
    
    // PRIVATE MEMBERS //////////////////////////////////////////////
    protected final String fullPathToStore;
    protected final File store;
    
    private long bytesStored = 0;
    private int  fileCount   = 0;
    
    protected final Object diskLock  = new Object();
    
    protected final static NonBlockingLog debugLog 
        = NonBlockingLog.getInstance("debug.log");
    protected final static NonBlockingLog errorLog 
        = NonBlockingLog.getInstance("error.log");
    
    // CONSTRUCTORS /////////////////////////////////////////////////
    /**
     * Looks for a directory at xlattice/overlays/store relative to
     * the default directory.  If the directory is absent, it and 
     * any necessary intermediate directories should be created.
     * Otherwise the subclass should size the store and set fileCount
     * and bytesStored.
     */

    public AbstractDisk () {
        this (null);
    }
    public AbstractDisk (String fromHereToXlattice) {
        if (fromHereToXlattice == null || fromHereToXlattice.equals(""))
            fullPathToStore = PATH_TO_STORE;
        else {
            StringBuffer sb = new StringBuffer(fromHereToXlattice);
            if (!fromHereToXlattice.endsWith(File.separator))
                sb.append(File.separator);
            fullPathToStore = sb.append(PATH_TO_STORE).toString();
        }
        store = new File(fullPathToStore);
        if (store.exists()) {
            if(!store.isDirectory()) {
                throw new IllegalStateException(fullPathToStore
                    + " exists, but is not a directory");
            }
            // OK, it's a directory, get it sized
            // XXX STUB XXX
           
        } else {
            if(!store.mkdirs())
                throw new IllegalStateException(
                    "couldn't create " + fullPathToStore);
            // DEBUG
            System.out.println("created " + fullPathToStore);
            // END
            bytesStored = 0;
            fileCount   = 0;
        }
    }
    // LOGGING //////////////////////////////////////////////////////
//  /** 
//   * Set the name of the file debug messages are logged to.  If
//   * this is null, no logging is done.
//   *
//   * @param name path to log file from current directory
//   */
//  protected final void setDebugLog (String name) {
//      if (debugLog != null)
//          throw new IllegalStateException("can't change debug log name");
//      if (name != null)
//          debugLog   = NonBlockingLog.getInstance(name);
//  }
//  /** 
//   * Set the name of the file error messages are logged to.  If
//   * this is null, no logging is done.
//   *
//   * @param name path to log file from current directory
//   */
//  protected final void setErrorLog (String name) {
//      if (errorLog != null)
//          throw new IllegalStateException("can't change error log name");
//      if (name != null)
//          errorLog   = NonBlockingLog.getInstance(name);
//  } // GEEP
    
    /**
     * Subclasses should override.
     */
    protected void DEBUG_MSG(String msg) {
        if (debugLog != null)
            debugLog.message("AbstractDisk" + msg);
    }
    protected void ERROR_MSG(String msg) {
        if (errorLog != null)
            errorLog.message("AbstractDisk" + msg);
    }
    // PROPERTIES ///////////////////////////////////////////////////
    public long bytesStored () {
        return bytesStored;
    }
    public int  fileCount() {
        return fileCount;
    }
    // AbstractDisk METHODS /////////////////////////////////////////
    /**
     * Remove all files stored on the abstract disk.  Subclasses must
     * actually do this, possibly using bulk operating system methods,
     * then call this method to reset the counts.
     *
     * XXX CONCURRENCY PROBLEMS
     */
    protected void clear () {
        bytesStored = 0;
        fileCount   = 0;
    }
//  // INTERFACE DataKeyedReader ////////////////////////////////////
//  /** 
//   * Retrieve data by content key (content hash).
//   */
//  public abstract byte[] get (NodeID nodeID)  
//                              throws CryptoException, IOException;
//  /**
//   * Retrieve a serialized SignedList, given its key, calculated
//   * from the RSA public key and title of the list.
//   *
//   * @param nodeID whose value is the key
//   */
//  public abstract byte[] getSigned (NodeID nodeID)   
//                              throws CryptoException, IOException ;

//  // INTERFACE DataKeyedWriter ////////////////////////////////////
//  
//  public abstract boolean delete (NodeID nodeID)
//                                              throws IOException;
// 
//  /**
//   * If there is a file whose content key is the same as the value 
//   * of the nodeID, return false.  Otherwise, if the content hash of 
//   * the data in the buffer is the same as the value of the nodeID,
//   * then store it and return true.
//   * 
//   * @param nodeID whose value is the SHA1 content hash of the buffer
//   * @param b      data being stored
//   * @return whether anything was written to store
//   */
//  public abstract boolean put (NodeID nodeID, byte[] b)
//                              throws CryptoException, IOException ;
//  /**
//   * Store a SignedList.  The value of the NodeID is the key
//   * calculated from the RSA public key and title of the list.
//   * 
//   * @param nodeID whose value is the key of the SignedList
//   * @param buffer serialized SignedList
//   * @return if successful, a reference to the File, otherwise null
//   */
//  public abstract boolean putSigned (NodeID nodeID, SignedList list)
//                              throws CryptoException, IOException ;
}
