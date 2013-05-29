/* MappedAddress.java */
package org.xlattice.protocol.stun;

import java.net.Inet4Address;

/**
 *
 * @author Jim Dixon
 */
public class MappedAddress extends AddrAttr {


    public MappedAddress (Inet4Address addr, int port) {
        super(MAPPED_ADDRESS, addr, port);
    }
}

