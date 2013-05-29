/* ChangeRequest.java */
package org.xlattice.protocol.stun;

/**
 *
 * @author Jim Dixon
 */
public class ChangeRequest                  extends ValueAttr {


    public ChangeRequest (int which) {
        super (CHANGE_REQUEST, new byte[4]);
        if ( (which & ~(CHANGE_IP | CHANGE_PORT)) != 0)
            throw new IllegalArgumentException(
                    "unrecognized change parameter " + which);
        value[3] = (byte) which;
    }

}
