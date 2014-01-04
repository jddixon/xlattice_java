/* Junit.java */
package org.xlattice.projmgr.antOM;

/**
 * Junit.
 *
 * This is copyrighted material made available under the terms of
 * the XLattice License, which is included in this distribution as
 * LICENSE.txt and is also available at
 *   http://xlattice.sourceforge.net/community/license.html
 *
 * @author Jim Dixon
 */
public class Junit implements TargetElm{

    // STATIC VARIABLES /////////////////////////////////////////////
    private String     _dir;
    private String     _errorproperty;
    private String     _failureproperty;
    private boolean    _fork;
    private String     _haltonerror;
    private String     _haltonfailure;
    private String     _printsummary;
    private JunitElm   _junitElm;

    // CONSTRUCTORS /////////////////////////////////////////////////
    public Junit () {
    }

    // ACCESS METHODS ///////////////////////////////////////////////
    public String getDir() {
        return _dir;
    }
    public void setDir(String value) {
        _dir = value;
    }
    public String getErrorproperty() {
        return _errorproperty;
    }
    public void setErrorproperty(String value) {
        _errorproperty = value;
    }
    public String getFailureproperty() {
        return _failureproperty;
    }
    public void setFailureproperty(String value) {
        _failureproperty = value;
    }
    public boolean getFork() {
        return _fork;
    }
    public void setFork(boolean value) {
        _fork = value;
    }
    public String getHaltonerror() {
        return _haltonerror;
    }
    public void setHaltonerror(String value) {
        _haltonerror = value;
    }
    public String getHaltonfailure() {
        return _haltonfailure;
    }
    public void setHaltonfailure(String value) {
        _haltonfailure = value;
    }
    public String getPrintsummary() {
        return _printsummary;
    }
    public void setPrintsummary(String value) {
        _printsummary = value;
    }
    public JunitElm getJunitElm() {
        return _junitElm;
    }
    public void setJunitElm(JunitElm value) {
        _junitElm = value;
    }
}
