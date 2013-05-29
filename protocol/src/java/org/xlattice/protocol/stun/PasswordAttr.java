/* PasswordAttr.java */
package org.xlattice.protocol.stun;

/**
 *
 * @author Jim Dixon
 */
public class PasswordAttr extends ValueAttr {

    public PasswordAttr (byte[] password) {
        super (PASSWORD, password);
        if (password.length % 4 != 0)
            throw new IllegalArgumentException (
                "PASSWORD length must be a multiple of 4 but is "
                + password.length);
    }

}
