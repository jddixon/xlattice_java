/* Target.java */
package org.xlattice.projmgr.antOM;

import java.util.ArrayList;

/**
 * Target.
 *
 * This is copyrighted material made available under the terms of
 * the XLattice License, which is included in this distribution as
 * LICENSE.txt and is also available at
 *   http://xlattice.sourceforge.net/community/license.html
 *
 * @author Jim Dixon
 */
public class Target implements ProjectElm{

    // STATIC VARIABLES /////////////////////////////////////////////
    private String     _description;
    private String     _if;
    private String     _name;
    private String     _unless;
    private ArrayList<String> _dependses = new ArrayList<String>();
    private ArrayList<TargetElm> _targetElms = new ArrayList<TargetElm>();

    // CONSTRUCTORS /////////////////////////////////////////////////
    public Target () {
    }

    // ACCESS METHODS ///////////////////////////////////////////////
    public String getDescription() {
        return _description;
    }
    public void setDescription(String value) {
        _description = value;
    }
    public String getIf() {
        return _if;
    }
    public void setIf(String value) {
        _if = value;
    }
    public String getName() {
        return _name;
    }
    public void setName(String value) {
        _name = value;
    }
    public String getUnless() {
        return _unless;
    }
    public void setUnless(String value) {
        _unless = value;
    }
    public void addDepends(String value) {
        _dependses.add(value);
    }
    public String getDepends(int n) {
        return _dependses.get(n);
    }
    public int sizeDependses() {
        return _dependses.size();
    }
    public void addTargetElm(TargetElm value) {
        _targetElms.add(value);
    }
    public TargetElm getTargetElm(int n) {
        return _targetElms.get(n);
    }
    public int sizeTargetElms() {
        return _targetElms.size();
    }
}
