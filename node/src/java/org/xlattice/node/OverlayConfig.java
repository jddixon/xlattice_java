/* OverlayConfig.java */
package org.xlattice.node;

import java.io.File;
import org.xlattice.Overlay;

/**
 * The Overlay specification from the Node's XML configuration 
 * file.  Currently this is limited to the names of the Overlay's
 * data directory and the Overlay's class.
 *
 * @author Jim Dixon
 */
public class OverlayConfig {
    private File  dir_;
    private Class clazz_;
    // temporary kludge
    private String className_;

    public OverlayConfig () {}

    
    // DIRECTORY ////////////////////////////////////////////////////
    // String forms ///////////////////////////////////////
    /** @return the name of the Overlay's data directory */
    public String getDir () {
        return dir_.getName();
    }
    /**
     * Set the name of the data directory; used by the data binding
     * software.
     * 
     * @todo  make sure name represents a directory 
     * @param dirName the directory name
     */
    public void setDir (String dirName) {
        if (dirName == null)
            throw new IllegalArgumentException("null overlay directory name");
        dir_ = new File(dirName);
    }
    // File forms /////////////////////////////////////////
    /** @return the data directory as a File */
    public File getDirFile() {
        return dir_;
    }
    /**
     * Set the data directory.
     * 
     * @param dir a reference to the File
     */
    public void setDirFile(File dir) {
        dir_ = dir;
    }
    // CLASS ////////////////////////////////////////////////////////
    // String forms ///////////////////////////////////////
    /** @return the name of the Overlay's class */
    public String getClassName() {
        //return clazz_.getName();
        return className_;
    }
    /**
     * Set the Overlay from its fully qualified class name.
     * 
     * @param className name in String form
     */
    public void setClassName(String className) throws ClassNotFoundException {
        if (className == null || className.equals(""))
            throw new IllegalArgumentException ("null or empty class name");
        //clazz_ = Class.forName(className);
        className_ = className;
    }
    // Class forms ////////////////////////////////////////
    /** @return a reference to the Overlay's class */
    public Class getClazz() {
        return clazz_;
    }
    /**
     * Set the Overlay's class.
     * @param clazz a reference to the class
     */
    public void setClazz (Class clazz) {
        if (clazz == null)
            throw new IllegalArgumentException ("null class");
        clazz_ = clazz;
    }
    // SERIALIZATION ////////////////////////////////////////////////
    /**
     * @return the Overlay specification in a particularly attractive form
     */
    public String toString () {
        return new StringBuffer()
            .append (getDir())
            .append (": ")
            .append (getClassName())
            .toString();
    }
}
