/* SiteDir.java */
package org.xlattice.httpd;

import java.io.File;
import java.io.IOException;

/**
 * @author Jim Dixon
 */

import org.xlattice.util.ArrayStack;

public class SiteDir                        implements RndDir {

    public  final String name;
    /** name including path and separator */
    public  final String dirName;
    private final ArrayStack subDirs;
    private final ArrayStack files;
    
    public SiteDir (String dirName)             throws Exception {
        if (dirName == null || dirName.equals(""))
            throw new IllegalArgumentException (
                    "null or empty directory name");
        name    = dirName;
        if (name.endsWith(File.separator))
            this.dirName = name;
        else
            this.dirName = new StringBuffer(name)
                                .append(File.separator)
                                .toString();
        subDirs = new ArrayStack();
        files   = new ArrayStack();
    }
    /**
     * Create the physical directory below the given base directory,
     * a path relative to the default directory and guaranteed to be
     * separator-terminated.
     */
    public boolean mkdir (String base)          throws Exception {
        if (base == null || base.equals(""))
            throw new IllegalArgumentException (
                    "null or empty base directory name");
        String s = new StringBuffer (base)
                    .append(dirName).toString();
        File dir = new File(s);
        if (dir.exists())
            dir.delete();
        return dir.mkdir();
    }
    // INTERFACE RndDir /////////////////////////////////////////////
    /** @return name of directory, including path and separator */
    public String dirName() {
        return dirName;
    }
    public SiteDir  addDir  (String s)      throws Exception {
        SiteDir dir = new SiteDir ( new StringBuffer(dirName)
                                    .append(s).toString() );
        subDirs.push(dir);
        return dir;
    }
    /** 
     * Add a data file below this directory, prefixing a path.
     * @param s   name of the data file, excluding any path
     * @param ext extension excluding leading dot
     * @param b   contents of the file
     */
    public SiteFile addFile (String s, String ext, byte[] b) 
                                            throws Exception {
        String name = new StringBuffer(dirName).append(s).toString();
        SiteFile file = new SiteFile (name, ext, b);
        files.push(file);
        return file;
    }
    public int dirCount () {
        return subDirs.size();
    }
    public int fileCount () {
        return files.size();
    }
}
