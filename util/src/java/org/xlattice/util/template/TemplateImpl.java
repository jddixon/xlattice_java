/* TemplateImpl.java */
package org.xlattice.util.template;

import org.xlattice.Template;

/**
 * @author Jim Dixon
 **/

public abstract class TemplateImpl implements Template {

    protected final int _type;

    // CONSTRUCTORS /////////////////////////////////////////////////
    protected TemplateImpl (int type) {
        _type = type;
    }
    // INTERFACE Template ///////////////////////////////////////////
    public final int getType() {
        return _type;
    }
}
