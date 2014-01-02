/* Delete.java */
package org.xlattice.projmgr.antOM;

/**
 * Delete.
 *
 * This is copyrighted material made available under the terms of
 * the XLattice License, which is included in this distribution as
 * LICENSE.txt and is also available at
 *   http://xlattice.sourceforge.net/community/license.html
 *
 * @author Jim Dixon
 */
public class Delete implements TargetElm{

    // STATIC VARIABLES /////////////////////////////////////////////
    private String     _dir;
    private String     _file;
    private FileSet    _fileSet;

    // CONSTRUCTORS /////////////////////////////////////////////////
    public Delete () {
    }

    // ACCESS METHODS ///////////////////////////////////////////////
    public String getDir() {
        return _dir;
    }
    public void setDir(String value) {
        _dir = value;
    }
    public String getFile() {
        return _file;
    }
    public void setFile(String value) {
        _file = value;
    }
    public FileSet getFileSet() {
        return _fileSet;
    }
    public void setFileSet(FileSet value) {
        _fileSet = value;
    }
}
