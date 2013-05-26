/* IntOpt.java */

/**
 * @author Jim Dixon
 **/

package org.xlattice.util.cmdline;

/**
 * Bind a command line argument to an int field in an object,
 * optionally specifiying minimum and maximum value.
 *
 * @author <a href="mailto:jddixon@users.sourceforge.net">Jim Dixon</a>
 */

public class IntOpt extends CmdLineOpt {

    private final int minVal;

    private final int maxVal;

    // CONSTRUCTORS /////////////////////////////////////////////////
    /**
     * Create a command line argument-int field binding.
     * 
     * @param oName option name
     * @param fName field name
     * @param desc  description for use in help messages
     * @param min   minimum permissible value
     * @param max   maximum permissible value
     */
    public IntOpt(String oName, String fName, String desc, int min, int max) {
        super(oName, fName, desc);
        minVal = min;
        maxVal = max;
    }

    public IntOpt(String oName, String fName, int min, int max) {
        this (oName, fName, null, min, max);
    }

    public IntOpt(String oName, String fName, String desc) {
        this (oName, fName, desc, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public IntOpt(String oName, String fName) {
        this (oName, fName, null);
    }

    public String optionForHelp() {
        return "-" + option + "NNN";
    }

    protected void bindField(Class clazz) {
        super.bindField(clazz);
        Class type = fieldRef.getType();
        if (type != Integer.class && type != Integer.TYPE) {
            throw new OptionDescriptorException("field " + option + " in " +
                clazz.getName() + " is not of type int");
        }
    }

    protected void setValue (Bindery bnd) {
        ArgCursor cursor  = bnd.cursor();
        if (!cursor.hasNext()) {
            throw new CmdLineException("missing integer argument value");
        }
        String arg = cursor.next();
        // throws NumberFormatException()
        int value;
        try {
            value = Integer.parseInt(arg);
        } catch (NumberFormatException nfe) {
            throw new CmdLineException( "option -" + option 
                + " has bad numeric value: " + arg);
        } 
        // MODIFY TO ACCEPT HEX
        if (value < minVal || value > maxVal) {
            throw new CmdLineException( "option -" + option 
                + " has value out of range: " + arg);
        } else {
            bnd.setField( fieldRef, new Integer(value) );
        }
    }
}
