/* LongOpt.java */

/**
 * @author Jim Dixon
 **/

package org.xlattice.util.cmdline;

/**
 * Binding between a command line option and a long field in an 
 * object, with optional minimum and maximum values.
 */

public class LongOpt extends CmdLineOpt {

    private final long minVal;

    private final long maxVal;

    /**
     * Create command line option and long field binding, optionally
     * specifying a minimum and maximum option value.
     *
     * @param oName command line option name
     * @param fName field name
     * @param desc  description for use in help messages
     * @param min   minimum permissible value
     * @param max   maximum permissible value for option 
     */
    public LongOpt(String oName, String fName, String desc, 
                                                    long min, long max) {
        super(oName, fName, desc);
        minVal = min;
        maxVal = max;
    }

    public LongOpt(String oName, String fName,      long min, long max) {
        this (oName, fName, null, min, max);
    }

    /**
     * Constructor defaulting option values to MIN_VALUE and MAX_VALUE.
     */
    public LongOpt(String oName, String fName, String desc) {
        this (oName, fName, desc, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    public LongOpt(String oName, String fName) {
        this (oName, fName, null);
    }

    public String optionForHelp() {
        return "-" + option + "NNN";
    }

    protected void bindField(Class clazz) {
        super.bindField(clazz);
        Class type = fieldRef.getType();
        if (type != Long.class && type != Long.TYPE) {
            throw new OptionDescriptorException("field " + option + " in " +
                clazz.getName() + " is not of type long");
        }
    }

    protected void setValue(Bindery bnd) {
        ArgCursor cursor  = bnd.cursor();
        if (!cursor.hasNext()) {
            throw new CmdLineException("missing integer argument value");
        }
        String arg = cursor.next();
        long value;
        try {
            value = Long.parseLong(arg);
        } catch (NumberFormatException nfe) {
            // MODIFY TO ACCEPT HEX
            throw new CmdLineException( "option -" + option 
                + " has bad numeric value: " + arg);
        } 
        if (value < minVal || value > maxVal) {
            throw new CmdLineException( "option -" + option 
                + " has value out of range: " + arg);
        } else {
            bnd.setField( fieldRef, new Long(value) );
        }
    }
}
