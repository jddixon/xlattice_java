/* StringOpt.java */

/**
 * @author Jim Dixon
 **/

package org.xlattice.util.cmdline;

/**
 * Bind a command line option value to a String field.
 *
 * @author <a href="mailto:jddixon@users.sourceforge.net">Jim Dixon</a>
 */

public class StringOpt extends CmdLineOpt {

    // CONSTRUCTORS /////////////////////////////////////////////////
    /**
     * @param option option name
     * @param fName  name of field being bound to
     * @param desc   description for help messages 
     */
    public StringOpt(String option, String fName, String desc) {
        super (option, fName, desc);
    }

    public StringOpt(String option, String fName) {
        this (option, fName, null);
    }

    // IMPLEMENTATIONS OF ABSTRACT METHODS //////////////////////////
    protected void bindField(Class clazz) {
        super.bindField(clazz);
        Class type = fieldRef.getType();
        if (type != String.class) {
            throw new OptionDescriptorException("field " + fieldName
                    + " in " + clazz.getName() + " is not of type String");
        }
    }

    protected void setValue(Bindery bnd) {
        ArgCursor cursor = bnd.cursor();
        if (cursor.hasNext()) {
            String arg = cursor.next();
            if (arg.length() > 0 && arg.charAt(0) == '-') {
                throw new CmdLineException (
                    "option -" + option + " value starts with '-'");
            } else {
                bnd.setField (fieldRef, arg);
            }
        } else {
            throw new CmdLineException ( "option -" + option 
                                                    + " value missing");
        }
    }
}
