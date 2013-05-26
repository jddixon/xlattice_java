/* BinaryTemplate.java */
package org.xlattice.util.template;

import org.xlattice.Context;
import org.xlattice.Template;

/**
 * A Template which consists of an irreducible byte array.
 *
 * @author Jim Dixon
 */
public class BinaryTemplate extends TemplateImpl {

    private final byte[] _data;
    
    // CONSTRUCTORS /////////////////////////////////////////////////
    protected BinaryTemplate (byte[] data) {
        super(TPL_BINARY);
        if (data == null)
            throw new IllegalArgumentException(
                    "null byte array reference");
        _data = (byte[])data.clone();
    }
    // INTERFACE TEMPLATE ///////////////////////////////////////////
    /**
     * Return the byte array as a String.  This will often not make
     * any sense.
     */
    public String toString(Context ctx) {
        return new String (_data);
    }
    /**
     * For efficiency, returns a reference to the byte array.  
     * XXX Risky, of course.
     */
    public byte[] getBytes(Context ctx) {
        return _data;
    }
}
