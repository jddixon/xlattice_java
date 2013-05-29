/* ChangedAddress.java */
package org.xlattice.protocol.stun;

import java.net.Inet4Address;

/**
 *
 * @author Jim Dixon
 */
public class ChangedAddress extends AddrAttr {


    public ChangedAddress (Inet4Address addr, int port) {
        super(CHANGED_ADDRESS, addr, port);
    }
}

