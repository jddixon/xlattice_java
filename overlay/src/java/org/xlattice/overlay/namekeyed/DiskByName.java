/* DiskByName.java */
package org.xlattice.overlay.namekeyed;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;

/**
 * @author Jim Dixon
 **/

import org.xlattice.overlay.CallBack;
import org.xlattice.overlay.DelCallBack;
import org.xlattice.overlay.GetCallBack;
import org.xlattice.overlay.PutCallBack;
import org.xlattice.overlay.NameKeyed;

/**
 * Provides a blocking ByteBuffer get/put interface to a set of 
 * named files.  All file names are relative to a base directory.  
 */
public class DiskByName implements NameKeyed {

    private static final HashMap dirs = new HashMap();

    // INSTANCE DATA ////////////////////////////////////////////////
    private final String absPath;
    private final File   baseDir;
    
    // CONSTRUCTORS AND SUCH ////////////////////////////////////////
    /**
     * @param absolutePath name of an existing directory 
     */
    private DiskByName (String absolutePath) {
        if (!absolutePath.endsWith(File.separator)) {
            absolutePath = new StringBuffer(absolutePath) 
                        .append(File.separator)
                        .toString();
        }
        absPath = absolutePath;
        baseDir = new File (absPath);
        dirs.put(absPath, baseDir);
    }
    public static final DiskByName getInstance (String name) {
        if (name == null)
            throw new IllegalArgumentException ("null directory name");
        File dir = new File(name);
        if (!dir.exists())          // we might want it to be created
            throw new IllegalArgumentException ("directory does not exist");
        if (!dir.isDirectory())
            throw new IllegalArgumentException("not a directory: " + name);
        String absPath = dir.getAbsolutePath();
        DiskByName thisDisk = (DiskByName) dirs.get(absPath);
        if (thisDisk == null) {
            thisDisk =  new DiskByName(absPath);
            dirs.put(absPath, thisDisk);
            return thisDisk;
        } else { 
            return thisDisk;
        }
    }
    public static final DiskByName getInstance (File f) {
        if (f == null)
            throw new IllegalArgumentException("null file");
        return getInstance(f.getName());
    }
    // INTERFACE NameKeyedReader ////////////////////////////////////
    /**
     * Given the name of a file (expressed as a path relative to the
     * base directory), return its contents wrapped in a ByteBuffer.
     * If there is any problem accessing the file, returns null.
     * 
     * @param  key the pathname of a file
     * @return null or the entire contents of the file in a ByteBuffer
     */
    public void get (String key, GetCallBack listener) {
        int status;
        byte[] data = null;
        long len;
        if (isBadName(key))
            status = CallBack.BAD_ARGS;
        else try {
            String absName = new StringBuffer(absPath).append(key).toString();
            File f = new File(absName);
            if (!f.exists())
                status = CallBack.NOT_FOUND;
            else {
                len = f.length();
                // XXX something of a hack, files limited to 2GB
                if (len > Integer.MAX_VALUE) 
                    status = CallBack.TOO_BIG;
                else { 
                    int intLen = (int) len;
                    data = new byte[intLen]; 
                    FileInputStream ins = new FileInputStream (f);
                    int count = ins.read(data);
                    if (count == intLen)
                        status = CallBack.OK;
                    else 
                        status = CallBack.IO_EXCEPTION;
                }
            }
        } catch (IOException ioe) {
            status = CallBack.IO_EXCEPTION;
        }
        listener.finishedGet(status, data);
    }
    // INTERFACE NameKeyedWriter ////////////////////////////////////
    public void delete (String key, DelCallBack listener) {
        int status;
        if (isBadName(key))
            status = CallBack.BAD_ARGS;
        else {
            String absName = new StringBuffer(absPath)
                                            .append(key).toString();
            File file = new File(absName);
            if(file.delete())
                status = CallBack.OK;
            else
                status = CallBack.NOT_FOUND;
        } 
        listener.finishedDel(status);
    }
    /**
     *
     * @param key   name of the file to be written
     * @param data  data to be written
     */
    public void put (String key, byte[] data, PutCallBack listener) {
        int status; 
        if (isBadName(key))
            status = CallBack.BAD_ARGS;
        else try {
            String absName = new StringBuffer(absPath).append(key).toString();
            File f = new File(absName);
            FileOutputStream outs = new FileOutputStream (f);
            outs.write(data);
            outs.flush();
            outs.close();
            status = CallBack.OK;
        } catch (IOException ioe) {
            status = CallBack.IO_EXCEPTION;
        }
        listener.finishedPut(status);
    }
    // UTILITY METHODS //////////////////////////////////////////////
    /**
     * Performs limited checks on file/path names.
     * @return whether name is inacceptable
     */
    private boolean isBadName(String name) {
        if (name == null || name.length() == 0)
            return true;
        char char0 = name.charAt(0);
        if (char0 == '/' || char0 == '\\')
            return true;
        if (name.indexOf("..") != -1)
            return true;
        return false;
    }
    // PROPERTIES ///////////////////////////////////////////////////
    
}
