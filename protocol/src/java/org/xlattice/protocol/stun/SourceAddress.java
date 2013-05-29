/* SourceAddress.java */
package org.xlattice.protocol.stun;

import java.net.Inet4Address;

/**
 *
 * @author Jim Dixon
 */
public class SourceAddress extends AddrAttr {


    public SourceAddress (Inet4Address addr, int port) {
        super(SOURCE_ADDRESS, addr, port);
    }
}

