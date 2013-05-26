/* DoubleOpt.java */

/**
 * @author Jim Dixon
 **/

package org.xlattice.util.cmdline;

/**
 * Binds a command line argument to a double field in an object.
 *
 * @author <a href="mailto:jddixon@users.sourceforge.net">Jim Dixon</a>
 */

public class DoubleOpt extends CmdLineOpt {

    /** Minimum allowed argument value. */
    private final double minVal;

    /** Maximum allowed argument value. */
    private final double maxVal;

    /**
     * Create the binding between the option and the field,
     * optionally specifying maximum and minimum values.
     *
     * @param oName option name
     * @param fName field name
     * @param desc  description in help message
     * @param min   minimum value permitted
     * @param max   maximum value permitted
     */
    public DoubleOpt (String oName, String fName, String desc,
                                            double min, double max) {
        super(oName, fName, desc);
        minVal = min;
        maxVal = max;
    }

    public DoubleOpt (String oName, String fName, double min, double max) {
        this (oName, fName, null, min, max);
    }

    public DoubleOpt (String oName, String fName, String desc) {
        this (oName, fName, desc, Double.MIN_VALUE, Double.MAX_VALUE);
    }

    public DoubleOpt (String oName, String fName) {
        this (oName, fName, null);
    }

    /** @return the message displayed in a help message */
    public String optionForHelp() {
        return "-" + option + "N.NN";
    }

    protected void bindField(Class clazz) {
        super.bindField(clazz);
        Class type = fieldRef.getType();
        if (type != Double.class && type != Double.TYPE) {
            throw new OptionDescriptorException("field " + option + " in " +
                clazz.getName() + " is not of type double");
        }
    }

    protected void setValue (Bindery bnd) {
        ArgCursor cursor = bnd.cursor();
        if (!cursor.hasNext()) {
            throw new CmdLineException("missing value, option " + option);
        }
        String arg = cursor.next();
        double value;
        try {
            value = Double.parseDouble(arg);
        } catch (NumberFormatException ex) {
            throw new CmdLineException("option " + option 
                    + " has badly formatted value: " + arg);
        }
        if (value < minVal || value > maxVal) {
            throw new CmdLineException("option " + option 
                    + " has value out of range: " + arg);
        } else {
            bnd.setField( fieldRef, new Double(value) );
        }
    }
}
