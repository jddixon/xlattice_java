/* IPv4Address.java */
package org.xlattice.transport;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.xlattice.Address;
import org.xlattice.AddressException;

/**
 * An IPv4 address + port number combination capable of acting as
 * a Connection endpoint.
 * 
 * XXX WHY doesn't this just extend IPAddress? XXX
 *
 * @author Jim Dixon
 */
public final class IPv4Address implements Address {

    private final InetSocketAddress saddr;
    
    // CONSTRUCTORS /////////////////////////////////////////////////
    public IPv4Address (InetSocketAddress sockAddr) {
        saddr = sockAddr;
    }
    /**
     * @throws IllegalArgumentException if port is out of range
     */
    public IPv4Address (InetAddress host, int port) {
        saddr = new InetSocketAddress (host, port);
    }
        
    /**
     * @throws IllegalArgumentException if port is out of range
     */
    public IPv4Address (byte[] b, int port)     throws AddressException {
        if ( !isValidAddress(b) )
            throw new AddressException (
                    "not legal IPv4 address: " + b);
        try {
            saddr = new InetSocketAddress(InetAddress.getByAddress(b), port);
        } catch (UnknownHostException e) {
            /* thrown only if address length is wrong, but we have
             * caught that already */
            throw new IllegalArgumentException( 
                    "should never get here " + e );
        }
    }
    /**
     * Create a valid address/port number pair.  If the port number
     * passed is zero, an ephemeral port will be bound.  
     * 
     * @param host the host name
     * @param port the port number, construed as an unsigned short
     * @throws IllegalArgumentException if port is out of range
     */
    public IPv4Address (String host, int port)  throws AddressException {
        InetAddress iAddr;
        try {
            iAddr = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            throw new AddressException ("bad host address: " + e);
        }
        saddr = new InetSocketAddress (iAddr , port);
    }
    /**
     * Create an IPv4 address from a formatted string.  This must 
     * be a dotted quad followed by a colon followed by a port 
     * number (A.B.C.D:n).  The port number must be present,
     * it has no default.
     *
     * @throws IllegalArgumentException if port is out of range
     */
    public IPv4Address (String addr)            throws AddressException {
        int colonAt = addr.indexOf(":");
        if (colonAt == -1)
            throw new AddressException("missing port number");
        String ipAddr = addr.substring(0, colonAt);
        
        try {
            InetAddress iAddr = InetAddress.getByName(ipAddr);
            int port = Integer.parseInt(addr.substring(colonAt + 1)); 
            saddr = new InetSocketAddress(iAddr, port);
        } catch (UnknownHostException uhe) {
            throw new AddressException ("can't interpret " + ipAddr);
        }
    }
    // PROPERTIES ///////////////////////////////////////////////////
    /** @return the IP part of the address as a Java InetAddress */
    public InetAddress getInetAddress () {
        return saddr.getAddress();
    }
    /**
     * Return a byte array representing the IP address, with the
     * high order part in byte 0.  A conventional "dotted quad"
     * IP address maps exactly into this byte array; if the 
     * address is A.B.C.D, then byte 0 will be A, byte 1 will be
     * B, and so forth.
     *
     * @return the IP address as a byte array
     */
    public byte[] getIPAddress() {
        return saddr.getAddress().getAddress();
    }
    public int getPort() {
        return saddr.getPort();
    }
    // OTHER METHODS ////////////////////////////////////////////////
    /** 
     * This method is no longer part of the Address API.
     *
     * XXX Deprecate?
     *
     * @return a deep copy of the IPAddress 
     */
    public Object clone() {
        byte[] ipAddr_ = saddr.getAddress().getAddress();
        int port       = saddr.getPort();
        InetAddress copiedAddr = null;
        try {
            copiedAddr = InetAddress
                            .getByAddress( (byte[]) ipAddr_.clone() );
        } catch (java.net.UnknownHostException uhe) { 
            /* should be impossible */
        }
        return new IPv4Address (copiedAddr, port);
    }
    public boolean equals (Object o) {
        if (o == null || !(o instanceof IPv4Address))
            return false;
        IPv4Address other = (IPv4Address) o;
        if (saddr.getPort() != other.getPort())
            return false;
        InetAddress iad = other.getInetAddress();
        return iad.equals(saddr.getAddress());
    }
    public int hashCode() {
        return saddr.hashCode();
    }
    public static boolean isValidAddress (byte [] b) {
        return b != null && b.length == 4;
    }
    public static boolean isPrivate (byte [] b) {
        return isValidAddress(b) &&  (
            (b[0] == 10)  ||                // big endian 10/8
            (b[0] == -128 && b[1] == 0) ||  // 128.0/16
            (b[0] == - 84 &&                // 172.16/12
                b[1] >= 16 && b[1] <= 31 )
        );
    }
    /**
     * This method _allows_ blocks which are reserved by IANA but 
     * subject to allocation.
     *
     * It does not currently permit use of the loopback interface,
     * 127.0.0.1.
     * 
     * @return whether a remote address is routable 
     */
    public static boolean isRFC3330notPrivate (byte[] b) {
        return isValidAddress(b) && (
            (b[0] == 0)  ||                 // "this", 0/8
            (b[0] == 14) ||                 // 14/8, public data network
            (b[0] == 24) ||                 // 24/8, cable TV
            (b[0] == 127)||                 // 127/8, loopback
            (b[0] == -87 && b[1] == -2) ||  // 169.254/16, link local
            (b[0] == -64 && (
                (b[1] == 0 && b[2] == 2) || // 192.0.2.0/24, test net
                (b[1] == 88 && b[2] == 99)  // 192.88.99.0/24, 6to4relay anycast
                )) ||
            (b[0] == -58 &&
                (b[1] == 18 || b[1] == 19)  // 198.18.0.0/15, benchmark testing
                ) ||
            (-32 <= b[0] && b[0] < 0)       // 224/4, multicast
                                            // 240/4, reserved for future use
            
        );
    }
    /**
     * @return whether a remote address is routable
     */
    public static boolean isRFC3330 (byte [] b) {
        return isPrivate(b) || isRFC3330notPrivate(b);
    }
    public String toString () {
        // Java 1.4 prepends slash 
        // return saddr.toString();
        return saddr.toString().substring(1);
    }
    
}
