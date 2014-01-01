/* Copy.java */
package org.xlattice.projmgr.antOM;

/**
 * Copy.
 *
 * This is copyrighted material made available under the terms of
 * the XLattice License, which is included in this distribution as
 * LICENSE.txt and is also available at
 *   http://xlattice.sourceforge.net/community/license.html
 *
 * @author Jim Dixon
 */
public class Copy implements TargetElm{

    // STATIC VARIABLES /////////////////////////////////////////////
    private String     _file;
    private String     _todir;
    private String     _tofile;
    private FileSet    _fileSet;

    // CONSTRUCTORS /////////////////////////////////////////////////
    public Copy () {
    }

    // ACCESS METHODS ///////////////////////////////////////////////
    public String getFile() {
        return _file;
    }
    public void setFile(String value) {
        _file = value;
    }
    public String getTodir() {
        return _todir;
    }
    public void setTodir(String value) {
        _todir = value;
    }
    public String getTofile() {
        return _tofile;
    }
    public void setTofile(String value) {
        _tofile = value;
    }
    public FileSet getFileSet() {
        return _fileSet;
    }
    public void setFileSet(FileSet value) {
        _fileSet = value;
    }
}
