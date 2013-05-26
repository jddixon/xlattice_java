/* OptionDescriptorException.java */

/**
 * @author Jim Dixon
 **/

package org.xlattice.util.cmdline;

/**
 * Exception thrown when there is an error in the command line
 * specification.  That is, this marks a programmer error rather
 * than a user error.
 *
 * @author <a href="mailto:jddixon@users.sourceforge.net">Jim Dixon</a>
 */

public class OptionDescriptorException extends RuntimeException {

    public OptionDescriptorException(String msg) {
        super(msg);
    }
}
