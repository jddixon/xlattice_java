/* SecondaryAddress.java */
package org.xlattice.protocol.stun;

import java.net.Inet4Address;

/**
 *
 * @author Jim Dixon
 */
public class SecondaryAddress extends AddrAttr {


    public SecondaryAddress (Inet4Address addr, int port) {
        super(SECONDARY_ADDRESS, addr, port);
    }
}

