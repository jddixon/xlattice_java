/* FileLib.java */
package org.xlattice.util;

import java.io.File;
import java.io.IOException;

/**
 * @author Jim Dixon
 **/

public class FileLib {

    private FileLib() {}

    /**
     * Creates a subdirectory below a given parent directory if
     * it does not already exist.  The name of the parent must
     * end with a File.separator.  Returns the name of the child
     * directory, separator-terminated.
     *
     * The parent directory must already exist.
     * 
     * @param parent directory name, terminated by a File.separator
     * @param child  directory name, optionally so terminated
     * @return subdirectory name, terminated by separator
     * @throws IOException if the directory cannot be created
     */
    public static String mkSubDir (String parent, String child) 
                                                throws IOException {
        StringBuffer sb = new StringBuffer(parent).append(child);
        if (!child.endsWith(File.separator))
            sb.append(File.separator);
        String subDirName = sb.toString();
        File subDir = new File(subDirName);
        if (!subDir.exists() && !subDir.mkdir())
            throw new IOException ("can't create " + subDirName);
        return subDirName;
    }
    /** 
     * Deletes x and, if it is a directory, all files contained in
     * it, including subdirectories, recursively.  
     *
     * Is silent if x does not exist (and so nothing is deleted).
     *
     * @param pathToX name of the file relative to the base directory
     * @return the number of files that could NOT be deleted
     */
    public static int recursingDelete(String pathToX)  
                                                throws IOException {
        int couldntDelete = 0;
        File x = new File(pathToX);
        if (x.exists()) {
            if (x.isDirectory()) {
                if ( !pathToX.endsWith(File.separator) )
                    pathToX = new StringBuffer(pathToX)
                                .append(File.separator)
                                .toString();
                String[] files = x.list();
                for (int i = 0; i < files.length; i++) {
                    String fName = pathToX + files[i];
                    File f       = new File(fName);
                    if (f.isDirectory()) {
                        recursingDelete (fName);
                    } else {
                        if (! f.delete() )
                            couldntDelete ++;
                    }
                }
            }
            x.delete();
        }
        return couldntDelete;
    }
}
