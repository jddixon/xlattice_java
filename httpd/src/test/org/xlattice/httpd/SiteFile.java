/* SiteFile.java */
package org.xlattice.httpd;

import org.xlattice.util.ArrayStack;

/**
 * @author Jim Dixon
 */

public class SiteFile {

    public final String name;
    public final String ext;
    public final String fullName;   // yes, lazy
    public final byte[] data;

    public SiteFile (String fileName, String extension, 
                                    byte[] b)   throws Exception {
        if (fileName == null || fileName.equals(""))
            throw new IllegalArgumentException (
                    "null or empty directory name");
        name = fileName;
        if (extension == null || extension.equals(""))
            throw new IllegalArgumentException (
                    "null or empty extension");
        ext  = extension;
        if (b == null)
            throw new IllegalArgumentException("null data array");
        data = b;
        fullName = new StringBuffer (name)
                            .append(".")
                            .append(extension)
                            .toString();
    }
}
