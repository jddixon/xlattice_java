/* VarTemplate.java */
package org.xlattice.util.template;

import org.xlattice.Context;
import org.xlattice.Template;

/**
 * Template whose value is a variable name which is resolved in the
 * Context.
 *
 * @author Jim Dixon
 */
public class VarTemplate extends TemplateImpl {

    private final String _name;
    
    // CONSTRUCTORS /////////////////////////////////////////////////
    protected VarTemplate (String name) {
        super(TPL_VAR);
        if (name == null || name.length() == 0)
            throw new IllegalArgumentException(
                    "null or empty variable name");
        _name = name;
    }
    // INTERFACE TEMPLATE ///////////////////////////////////////////
    public String toString(Context ctx) {
        if (ctx == null)
            throw new IllegalArgumentException("null context");
        Object value = ctx.lookup(_name);
        if (value != null)
            return value.toString();
        return null;
    }
    public byte[] getBytes(Context ctx) {
        if (ctx == null)
            throw new IllegalArgumentException("null context");
        Object value = ctx.lookup(_name);
        if (value == null)
            return null;
        if (value instanceof byte[])
            return (byte[])value;
        else if (value instanceof String)
            return ((String)value).getBytes();
        else
            throw new IllegalArgumentException("don't know what this is");
        
    }
}
