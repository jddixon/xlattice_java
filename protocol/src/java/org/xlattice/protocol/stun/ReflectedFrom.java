/* ReflectedFrom.java */
package org.xlattice.protocol.stun;

import java.net.Inet4Address;

/**
 *
 * @author Jim Dixon
 */
public class ReflectedFrom extends AddrAttr {


    public ReflectedFrom (Inet4Address addr, int port) {
        super(REFLECTED_FROM, addr, port);
    }
}

