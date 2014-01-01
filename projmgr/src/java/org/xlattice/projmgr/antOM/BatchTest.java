/* BatchTest.java */
package org.xlattice.projmgr.antOM;

/**
 * BatchTest.
 *
 * This is copyrighted material made available under the terms of
 * the XLattice License, which is included in this distribution as
 * LICENSE.txt and is also available at
 *   http://xlattice.sourceforge.net/community/license.html
 *
 * @author Jim Dixon
 */
public class BatchTest implements JunitElm{

    // STATIC VARIABLES /////////////////////////////////////////////
    private String     _todir;
    private FileSet    _fileSet;

    // CONSTRUCTORS /////////////////////////////////////////////////
    public BatchTest () {
    }

    // ACCESS METHODS ///////////////////////////////////////////////
    public String getTodir() {
        return _todir;
    }
    public void setTodir(String value) {
        _todir = value;
    }
    public FileSet getFileSet() {
        return _fileSet;
    }
    public void setFileSet(FileSet value) {
        _fileSet = value;
    }
}
