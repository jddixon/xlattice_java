/* ServerNameAttr.java */
package org.xlattice.protocol.stun;

/**
 *
 * @author Jim Dixon
 */
public class ServerNameAttr extends ValueAttr {

    public ServerNameAttr (byte[] name) {
        super (SERVER_NAME, name);
        if (name.length % 4 != 0)
            throw new IllegalArgumentException (
                "SERVER_NAME length must be a multiple of 4 but is "
                + name.length);
    }
    
    public String toString() {
        return new StringBuffer("ServerNameAttr:       ")
            .append(new String(value))
            .toString();
    }
}
