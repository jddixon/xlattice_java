/* NattedAddress.java */
package org.xlattice.protocol.stun;

import java.net.InetAddress;
import java.net.Inet4Address;

// XXX so far, not used
import org.xlattice.Address;

/**
 * A node behind a NAT (network address translator) may have two
 * addresses: the address that it uses for communications and the
 * address that is seen by other machines on the Internet which are
 * beyond the NAT.  These differ because the NAT rewrites addresses
 * on packets as they pass through.
 *
 * For our purposes, NATs only deal with IPv4 addresses.  Supposedly
 * NATs are only a temporary measure which will be obsolete when 
 * IPv6 is universally deployed.
 */
public class NattedAddress                  implements Address {

    // INSTANCE VARIABLES ///////////////////////////////////////////
    private Inet4Address    localHost;
    private int             localPort;
    private Inet4Address    mappedHost;
    private int             mappedPort;
    private int             natType;
    
    // CONSTRUCTORS /////////////////////////////////////////////////
    /**
     * Create an address pair where only the local address is known.
     */
    public NattedAddress (Inet4Address host, int port) {
        if (host == null)
            throw new IllegalArgumentException("null host");
        if (port < 0 || port > 65535)
            throw new IllegalArgumentException("port out of range: "
                    + port);
        localHost = host;
        localPort = port;
        natType   = -1;
    }
    /**
     * Create an address pair where both local and mapped addresses 
     * are known.
     */
    public NattedAddress(Inet4Address localH,  int localP,
                         Inet4Address mappedH, int mappedP) {
        this(localH, localP);
        if (mappedH == null)
            throw new IllegalArgumentException("null mapped host");
        if (mappedP < 0 || mappedP > 65535)
            throw new IllegalArgumentException("port out of range: "
                    + mappedP);
        mappedHost = mappedH;
        mappedPort = mappedP;
    }
    // GETTERS //////////////////////////////////////////////////////
    public Inet4Address getLocalHost() {
        return localHost;
    }
    public int getLocalPort() {
        return localPort;
    }
    public Inet4Address getMappedHost() {
        return mappedHost;
    }
    public int getMappedPort() {
        return mappedPort;
    }
    public int getNatType() {
        return natType;
    }
    // SETTERS //////////////////////////////////////////////////////
    public void setLocalHost(Inet4Address host) {
        if (host == null)
            throw new IllegalArgumentException("null host");
        localHost = host;
    }
    public void setLocalPort(int port) {
        if (port < 0 || port > 65535)
            throw new IllegalArgumentException("port out of range: "
                    + port);
        localPort = port;
    }
    public void setMappedHost(Inet4Address mappedH) { 
        if (mappedH == null)
            throw new IllegalArgumentException("null mapped host");
        mappedHost = mappedH;
    }
    public void setMappedPort(int mappedP) {
        if (mappedP < 0 || mappedP > 65535)
            throw new IllegalArgumentException("port out of range: "
                    + mappedP);
        mappedPort = mappedP;
    }
    public void setNatType(int t) {
        if (t < Client.NOT_NATTED || t > Client.SYMMETRIC_NAT)
            throw new IllegalArgumentException("unknown NAT type " + t);
        natType = t;
    }
    // INTERFACE ADDRESS ////////////////////////////////////////////
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof NattedAddress))
            return false;
        NattedAddress other = (NattedAddress)o;
        if (other.localHost == null)
            return false;
        if ( (!localHost.equals(other.localHost)) 
                || (localPort != other.localPort))
            return false;
        if ( mappedHost == null ) 
            return other.mappedHost == null;
        if (other.mappedHost == null)
            return false;
        return mappedHost.equals(other.mappedHost) 
            && mappedPort == other.mappedPort;
    }
    public int hashCode() {
        int retval = localHost.hashCode() ^ localPort;
        if (mappedHost != null) 
            retval ^= mappedHost.hashCode() ^ mappedPort;
        return retval;
    }
    public String toString() {
        StringBuffer sb = new StringBuffer("natted: type ")
            .append(natType)
            .append(", ")
            .append(localHost.toString())
            .append(':')
            .append(localPort)
            .append(" => ");
        if (mappedHost != null) 
            sb.append(mappedHost.toString())
              .append(':')
              .append(mappedPort)
              .append(' ');
        return sb.toString();
    }
}
