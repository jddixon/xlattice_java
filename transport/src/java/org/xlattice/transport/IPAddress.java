/* IPAddress.java */
package org.xlattice.transport;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import org.xlattice.Address;

/**
 * The name of this class is confusing and so unsatisfactory.
 *
 * Sourceforge tracker bug fix 1489026 2006-05-16
 * 
 * @author Jim Dixon
 */

public class IPAddress implements Address {

    protected final InetAddress host;
    private         int         port;

    // CONSTRUCTORS /////////////////////////////////////////////////
    public IPAddress (InetSocketAddress addr) {
        if (addr == null)
            throw new IllegalArgumentException ("address is null");
        host = addr.getAddress();
        port = addr.getPort();
    }
    /**
     * This constructor accepts a null host address, interpreting it
     * as the wildcard address 0.0.0.0/0.
     * 
     * @throws IllegalArgumentException if the port number is out of range
     */
    public IPAddress (InetAddress h, int p) {
        host = h;
        checkPort(p);
        port = p;
    }
    // UTILITY //////////////////////////////////////////////////////
    public static InetAddress copyInetAddr( InetAddress orig ) {
        byte[] ipAddr = (byte[]) orig.getAddress().clone();
        InetAddress addrCopy = null;
        try {
            addrCopy = InetAddress.getByAddress( ipAddr );
        } catch (java.net.UnknownHostException uhe) { 
            /* should be impossible */
        }
        return addrCopy;
    }
    // INTERFACE Address ////////////////////////////////////////////
    /** 
     * This method is no longer part of the Address API.
     *
     * XXX Deprecate?
     * 
     * @return a deep copy of the IPAddress 
     */
    public Object clone() {
        return new IPAddress (host, port);
    }
    public boolean equals(Object o) {
        if (o == null || !(o instanceof IPAddress))
            return false;
        IPAddress other = (IPAddress)o;
        if (other == this)
            return true;
        if (getPort() != other.getPort())
            return false;
        return getHost().equals(other.getHost());
    }
    /**
     * This is a hash on both the IP address and the port number.
     */
    public int hashCode() {
        return host.hashCode() ^ port;
    }

    public String toString() {
        return new StringBuffer("IPAddress: ")
            .append(host.toString())
            .append(':')
            .append(port)
            .toString();
    }
    // PROPERTIES ///////////////////////////////////////////////////
    public InetSocketAddress getSocketAddress() {
        return new InetSocketAddress(host, port);
    }
    public InetAddress getHost() {
        return host;
    }
    public int getPort() {
        return port;
    }
    protected void checkPort(int p) {
        if (p < 0 || p > 65535)
            throw new IllegalArgumentException("port number out of range: " 
                    + p);
    }
    public void setPort(int p) {
        checkPort(p);
        port = p;
    }
}
