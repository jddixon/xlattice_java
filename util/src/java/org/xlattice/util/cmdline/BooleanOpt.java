/* BooleanOpt.java */
package org.xlattice.util.cmdline;

/**
 * Binds a boolean field in an object to a command line argument.
 *
 * @author Jim Dixon
 */

public class BooleanOpt extends CmdLineOpt {

    // CONSTRUCTORS /////////////////////////////////////////////////
    /**
     * @param oName name of the command line option
     * @param name  name of the field the option is to be bound to
     * @param desc  description for use in help messages
     */
    public BooleanOpt(String oName, String name, String desc) {
        super (oName, name, desc);
    }

    public BooleanOpt(String oName, String name) {
        this (oName, name, null);
    }

    // IMPLEMENTATIONS OF ABSTRACT METHODS //////////////////////////
    protected void bindField(Class clazz) {
        super.bindField(clazz);
        Class type = fieldRef.getType();
        if (type != Boolean.class && type != Boolean.TYPE) {
            throw new OptionDescriptorException("field " + option + " in "
                + clazz.getName() + " is not of type boolean");
        }
    }

    /** XXX modify to flip sense */
    protected void setValue (Bindery bnd) {
        bnd.setField (fieldRef, Boolean.TRUE);
    }
}
