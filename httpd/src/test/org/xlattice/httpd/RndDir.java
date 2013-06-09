/* RndDir.java */
package org.xlattice.httpd;

/**
 * @author Jim Dixon
 */

public interface RndDir {
    /**
     * Returns the separator-terminated directory name as a path
     * from the base directory.  Does NOT include the base directory
     * name.
     */
    public String dirName();

    /** 
     * Add a subdirectory below this one, prefixing a path.
     * @param s name of the subdirectory, excluding any path 
     */
    public SiteDir  addDir (String s)       throws Exception ;

    /** 
     * Add a data file below this directory, prefixing a path.
     * @param s name of the data file, excluding any path
     * @param x extension
     * @param b contents of the file
     */
    public SiteFile addFile (String s, String x, byte[] b)
                                            throws Exception ;

    public int dirCount ();
    public int fileCount ();
}
