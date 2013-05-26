/* CmdLineException.java */

/**
 * @author Jim Dixon
 **/

package org.xlattice.util.cmdline;

/**
 * Exception thrown when there is an error in the command line.
 * This is a user error rather than a programmer error.
 *
 * @author <a href="mailto:jddixon@users.sourceforge.net">Jim Dixon</a>
 */

public class CmdLineException extends RuntimeException {

    public CmdLineException(String msg) {
        super(msg);
    }
}
