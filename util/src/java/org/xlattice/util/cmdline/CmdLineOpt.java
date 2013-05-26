/* CmdLineOpt.java */

/**
 * @author Jim Dixon
 **/

package org.xlattice.util.cmdline;

import java.lang.reflect.Field;

/**
 * Superclass for bindings between command line arguments and
 * object fields.  Command line options are expected to be 
 * preceded by a single dash (minus sign), as for example "-h"
 * and followed by a value.  The subclasses of this class convert
 * the option value to an appropriate type.
 *
 * @author <a href="mailto:jddixon@users.sourceforge.net">Jim Dixon</a>
 */

public abstract class CmdLineOpt {
    protected final String option;      // XXX SHOULD BE char
    protected final String fieldName;
    protected Field fieldRef;
    protected final String description;

    /**
     * Creates descriptor for an individual option
     *
     * @param f option character (for example, 'h' if "-h")
     * @param n name for field in class
     * @param d discription text for parameter
     */

    protected CmdLineOpt(String f, String n, String d) {
        if ( f == null || n == null) {
            throw new OptionDescriptorException(
                    "option name or field name is null");
        }
        if ( f.equals("") || n.equals("")) {
            throw new OptionDescriptorException(
                    "option name or field name not specified");
        }
        option      = f;
        fieldName   = n;
        if (d == null) {
            d = "";
        }
        description = d;
    }


    /**
     * Bind parameter to target class field. Must be invoked by subclasses.
     *
     * @param clazz target class for saving parameter values
     */

    protected void bindField(Class clazz) {
        try {
            fieldRef = clazz.getDeclaredField(fieldName);
            fieldRef.setAccessible(true);
        } catch (NoSuchFieldException ex) {
            throw new OptionDescriptorException("field " + fieldName +
                " not found in " + clazz.getName());
        }
    }

    /**
     * Set the value in the object field bound to this option.
     * Needs to be implemented for each primitive type accepted 
     * on the command line.
     *
     * @param bnd command line option bindery invoking setValue
     */
    protected abstract void setValue(Bindery bnd);

    /**
     * Overridden if something more elaborate than '-h' is needed.
     *
     * @return option as seen in help messages
     */
    protected String optionForHelp() {
        return "-" + option;
    } 
    
    // ACCESS METHODS
    /** @return the single-character option name */
    public String getOptionName() {
        return option;
    }
    /** @return the name of the object field */
    public String getFieldName() {
        return fieldName;
    }
    /** @return the option description for help messages */
    public String getDescription() {
        return description;
    }
}
