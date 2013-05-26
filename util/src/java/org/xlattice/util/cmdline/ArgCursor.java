/* ArgCursor.java */

/**
 * @author Jim Dixon
 **/

package org.xlattice.util.cmdline;

/**
 * Cursor over an array of command-line arguments.  The argument list
 * is sanitized before being copied here.
 *
 * @author <a href="mailto:jddixon@users.sourceforge.net">Jim Dixon</a>
 */
class ArgCursor {

    /** array of command line arguments */
    private final String[] args;

    /** index into the argument array, updated as used */
    private int curArg;

    /**
     * Create a cursor over a copy of an array of command line arguments.
     * The copied arguments have leading and trailing whitespace trimmed
     * off.
     * @param source the original array of arguments
     */
    protected ArgCursor (String[] source) {
        args     = new String[source.length];
        for (int i = 0; i < source.length; i++) {
            String s = source[i];
            if (s == null ) {
                // an internal error
                throw new IllegalArgumentException("null argument");
            }
            args[i] = s.trim();
            if (args[i].equals("")) {
                // another internal error
                throw new IllegalArgumentException("empty argument");
            }
        }
        curArg   = 0;
    }
    /**
     * @return the current position in the arg array
     */
    public int index() {
        return curArg;
    }
    /**
     * @return the number of arguments
     */
    public int length() {
        return args.length;
    }
    /**
     * Returns next argument without advancing the cursor.
     * @return reference to next arg
     */
    public String peek() {
        return args[curArg];
    }
    // ITERATOR EMULATION ///////////////////////////////////////////
    /**
     * @return Whether there is another argument.
     */
    public boolean hasNext() {
        return curArg < args.length;
    }
    /**
     * Returns next arg, advancing the cursor.
     * @return reference to next argument
     * @throws ArrayIndexOutOfBoundsException
     */
    public String next() {
        return args[curArg++];
    }
}
