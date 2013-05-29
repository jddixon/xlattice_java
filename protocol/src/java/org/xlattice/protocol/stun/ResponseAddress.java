/* ResponseAddress.java */
package org.xlattice.protocol.stun;

import java.net.Inet4Address;

/**
 *
 * @author Jim Dixon
 */
public class ResponseAddress extends AddrAttr {


    public ResponseAddress (Inet4Address addr, int port) {
        super(RESPONSE_ADDRESS, addr, port);
    }
}

