/* XorMappedAddress.java */
package org.xlattice.protocol.stun;

import java.net.Inet4Address;

/**
 * XXX The fact that this is going to be difficult to handle means
 * that we need to replace the Inet4Address with a byte[4].
 *
 * @author Jim Dixon
 */
public class XorMappedAddress extends AddrAttr {


    public XorMappedAddress (Inet4Address addr, int port) {
        super(XOR_MAPPED_ADDRESS, addr, port);
    }
}

