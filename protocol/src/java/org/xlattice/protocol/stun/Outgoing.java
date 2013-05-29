/* Outgoing.java */
package org.xlattice.protocol.stun;

import java.net.Inet4Address;
import java.net.InetAddress;

import org.xlattice.util.StringLib;

/**
 *
 * @author Jim Dixon
 */
public class Outgoing {

    public final Inet4Address addr;
    public final int          port;
    public final byte[]       msg;

    /**
     * @param a  address message is being sent to
     * @param p  port
     * @param m  the serialized StunMsg being sent
     */
    public Outgoing (Inet4Address a, int p, byte[]m) {
        if (a == null)
            throw new IllegalArgumentException("null address");
        addr = a;
        if (!Client.validPort(p))
            throw new IllegalArgumentException("port out of range: " + p);
        port = p;
        if (m == null)
            throw new IllegalArgumentException("null message");
        msg = m;
    }
    /**
     * XXX Preliminary format.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer("outgoing message to ")
                     .append(addr)
                     .append(':')
                     .append(port)
                     .append("\n  ")
                     .append(StringLib.byteArrayToHex(msg, 0, msg.length));
        return sb.toString();
    }
}
