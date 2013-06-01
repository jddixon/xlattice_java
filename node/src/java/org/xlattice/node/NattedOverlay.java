/* NattedOverlay.java */
package org.xlattice.node;

import org.xlattice.Address;
import org.xlattice.Overlay;
import org.xlattice.Protocol;
import org.xlattice.protocol.stun.NattedAddress;

/**
 * An overlay which uses UDP and a NATted address.  The protocol
 * will typically be the XLattice messaging protocol as defined
 * in org.xlattice.protocol.xl.
 */
public class NattedOverlay implements Overlay {
    protected final Protocol protocol;
    protected final NattedAddress addr;

    public NattedOverlay (Protocol p, NattedAddress a) {
        if (p == null)
            throw new IllegalArgumentException("null Protocol");
        protocol = p;
        if (a == null)
            throw new IllegalArgumentException("null NattedAddress");
        addr = a;
    }
    // INTERFACE Overlay ////////////////////////////////////////////
    public String transport() {
        return "udp";
    }
    public String protocol() {
        return protocol.name();
    }
    public Address address() {
        return addr;
    }
    public boolean equals(Object o) {
        if (this == o)
            return true;
        NattedOverlay other = (NattedOverlay)o;
        return  
            addr.getLocalHost() .equals(other.addr.getLocalHost())  &&
            addr.getLocalPort()  == other.addr.getLocalPort()       &&
            addr.getMappedHost().equals(other.addr.getMappedHost()) &&
            addr.getMappedPort() == other.addr.getMappedPort();
    }
    public int hashCode() {
        return 
            addr.getLocalHost() .hashCode() ^
            addr.getLocalPort()             ^
            addr.getMappedHost().hashCode() ^
            addr.getMappedPort();
    }
}
