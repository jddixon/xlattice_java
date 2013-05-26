/* StringTemplate.java */
package org.xlattice.util.template;

import org.xlattice.Context;
import org.xlattice.Template;

/**
 * A template which consists of an irreducible String, one which
 * will not be examined for any embedded templates.
 *
 * @author Jim Dixon
 */
public class StringTemplate extends TemplateImpl {

    private final String _text;
    
    // CONSTRUCTORS /////////////////////////////////////////////////
    protected StringTemplate (String text) {
        super(TPL_STRING);
        if (text == null)
            throw new IllegalArgumentException("null String reference");
        _text = text;
    }
    // INTERFACE TEMPLATE ///////////////////////////////////////////
    public String toString(Context ctx) {
        return _text;
    }
    public byte[] getBytes(Context ctx) {
        return _text.getBytes();
    }
}
