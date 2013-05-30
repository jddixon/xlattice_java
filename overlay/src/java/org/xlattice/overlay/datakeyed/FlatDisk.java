/* FlatDisk.java */
package org.xlattice.overlay.datakeyed;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

/**
 * @author Jim Dixon
 **/

import org.xlattice.CryptoException;
import org.xlattice.NodeID;
import org.xlattice.crypto.SignedList;
import org.xlattice.overlay.CallBack;
import org.xlattice.overlay.DataKeyed;
import org.xlattice.overlay.DelCallBack;
import org.xlattice.overlay.GetCallBack;
import org.xlattice.overlay.PutCallBack;
import org.xlattice.util.NonBlockingLog;
import org.xlattice.util.StringLib;

public class FlatDisk extends AbstractDisk {

    // CONSTRUCTORS AND SUCH ////////////////////////////////////////
    public FlatDisk () {
        super();                
        DEBUG_MSG(" no-path constructor");
    }
    public FlatDisk(String fromHereToXLattice) {
        super(fromHereToXLattice);
        DEBUG_MSG(" constructor, path is " + fromHereToXLattice);
    }
    // LOGGING //////////////////////////////////////////////////////
    /**
     * Subclasses override.
     */
    protected void DEBUG_MSG(String msg) {
        if (debugLog != null)
            debugLog.message("FlatDisk" + msg);
    }
    protected void ERROR_MSG(String msg) {
        if (errorLog != null)
            errorLog.message("FlatDisk" + msg);
    }
    // AbstractDisk METHODS /////////////////////////////////////////
    /**
     * Remove all files stored on the logical disk.
     *
     * This method must be thread-safe.
     */
    public void clear () {
        synchronized (diskLock) {
            // list() returns null if store/ does not exist
            String fileNames[] = store.list();  
            if (fileNames != null) {
                for (int i = 0; i < fileNames.length; i++) {
                    String path = new StringBuffer(fullPathToStore)
                                    .append(fileNames[i]).toString();
                    File file = new File(path);
                    // XXX SHOULD WRITE TO ERROR LOG XXX
                    if(!file.isDirectory() && !file.delete())
                        ERROR_MSG("can't delete " + fileNames[i]);
                }
                super.clear();
            }
        }
    }
    /**
     * Remove the directory structure below store/ as well as any
     * files contained.  For FlatDisk, there should be subdirectories
     * below store, so this is the same as clear().
     */
    public void clearAll() {
        clear();
    }
    // INTERFACE DataKeyedReader /////////////////////////////////////
    /** 
     * Retrieve data by content key (content hash).  
     *
     * @return the contents of the file or null if file not found
     */
    public void get (NodeID nodeID, GetCallBack listener) {
        int    status;
        byte[] data = null;
        DEBUG_MSG(".get()");

        if (nodeID == null)
            status = CallBack.BAD_ARGS;
        else try {
            String name = fileName(nodeID); 
            DEBUG_MSG(".get from\n    " + name);
            File file = new File(name);
            if (file.exists()) {
                if (file.isDirectory())
                    status = CallBack.IS_DIRECTORY;
                else {
                    FileInputStream fins = new FileInputStream(file);
                    long len = file.length();
                    if (len > Integer.MAX_VALUE)
                        status = CallBack.TOO_BIG;
                    else {
                        data = new byte[(int)len];
                        int count = fins.read(data);
                        fins.close();
                        if (count == (int)len) 
                            status = CallBack.OK;
                        else 
                            status = CallBack.IO_EXCEPTION;
                    }
                }
            } else 
                status = CallBack.NOT_FOUND;
        } catch (IOException ioe) {
            status = CallBack.IO_EXCEPTION;
        }
        listener.finishedGet(status, data);
    }
    /**
     * Retrieve a serialized SignedList, given its key, calculated
     * from the RSA public key and title of the list.
     *
     * @param nodeID whose value is the key
     */
    public void getSigned (NodeID nodeID, GetCallBack listener) {
        byte [] data = null;
        // STUB
        listener.finishedGet(CallBack.NOT_IMPLEMENTED, data);
    }

    // INTERFACE DataKeyedWriter ////////////////////////////////////
    /**
     * Delete the stored item corresponding to the hash which is the
     * value of nodeID.
     *
     * @return whether the operation succeeded
     * @throws an IOException if the name corresponds to a directory
     */
    public void delete (NodeID nodeID, DelCallBack listener) {
        String name = fileName(nodeID); 
        File file   = new File(name);
        int status;
        if (file.exists()) {
            if (file.isDirectory())
                status = CallBack.IS_DIRECTORY;
            else if(file.delete())
                status = CallBack.OK;
            else
                status = CallBack.IO_EXCEPTION;
        } else
            status = CallBack.NOT_FOUND;
        listener.finishedDel (status);
    }
   
    /**
     * If there is a file whose content key is the same as the value 
     * of the nodeID, return false.  Otherwise, if the content hash of 
     * the data in the buffer is the same as the value of the nodeID,
     * then store it and return true.
     * 
     * @param nodeID whose value is the SHA1 content hash of the buffer
     * @param b      data being stored
     */
    public void put (NodeID nodeID, byte[] b, PutCallBack listener) {
        int status; 
        DEBUG_MSG(".put()");        // XXX
        if (nodeID == null) 
            status = CallBack.BAD_ARGS;
        else try { 
            String name = fileName(nodeID); 
            DEBUG_MSG(".put to " + name);
            File file = new File(name);
            if (file.exists()) {
                if (file.isDirectory()) {
                    status = CallBack.IS_DIRECTORY;
                } else 
                    status = CallBack.EXISTS;
            } else {
                FileOutputStream fouts = new FileOutputStream(file);
                fouts.write(b);
                fouts.flush();
                fouts.close();
                status = CallBack.OK;
            }
        } catch (IOException ioe) {
            status = CallBack.IO_EXCEPTION;
        }
        listener.finishedPut(status);
    }
    /**
     * Store a SignedList.  The value of the NodeID is the key
     * calculated from the RSA public key and title of the list.
     * 
     * @param nodeID whose value is the key of the SignedList
     * @param list   serialized SignedList
     * @return if successful, a reference to the File, otherwise null
     */
    public void putSigned (NodeID nodeID, SignedList list,
                                          PutCallBack listener) {

        // STUB
        listener.finishedPut(CallBack.NOT_IMPLEMENTED);
    }
    // IMPLEMENTATION ///////////////////////////////////////////////
    protected final String fileName(NodeID nodeID) {
        return  new StringBuffer(fullPathToStore)
                            .append(StringLib.byteArrayToHex(nodeID.value()))
                            .toString();
    }
}
