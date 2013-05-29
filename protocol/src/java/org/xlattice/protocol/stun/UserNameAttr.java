/* UserNameAttr.java */
package org.xlattice.protocol.stun;

/**
 *
 * @author Jim Dixon
 */
public class UserNameAttr extends ValueAttr {

    public UserNameAttr (byte[] name) {
        super (USERNAME, name);
        if (name.length % 4 != 0)
            throw new IllegalArgumentException (
                "USERNAME length must be a multiple of 4 but is "
                + name.length);
                    
    }

    public String toString() {
        return new StringBuffer("UserNameAttr:       ")
            .append(new String(value))
            .toString();
    }
}
