/* Exec.java */
package org.xlattice.projmgr.antOM;

/**
 * Exec.
 *
 * This is copyrighted material made available under the terms of
 * the XLattice License, which is included in this distribution as
 * LICENSE.txt and is also available at
 *   http://xlattice.sourceforge.net/community/license.html
 *
 * @author Jim Dixon
 */
public class Exec implements TargetElm{

    // STATIC VARIABLES /////////////////////////////////////////////
    private String     _executable;
    private ExecElm    _execElm;

    // CONSTRUCTORS /////////////////////////////////////////////////
    public Exec () {
    }

    // ACCESS METHODS ///////////////////////////////////////////////
    public String getExecutable() {
        return _executable;
    }
    public void setExecutable(String value) {
        _executable = value;
    }
    public ExecElm getExecElm() {
        return _execElm;
    }
    public void setExecElm(ExecElm value) {
        _execElm = value;
    }
}
