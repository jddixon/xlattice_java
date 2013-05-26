/* Overlay.java */
package org.xlattice;

/**
 * A Overlay is characterized by an address space, a transport protocol,
 * and possibly a set of rules for navigating the address space using
 * the protocol.
 * 
 * A Overlay may either be system-supported, like TCP/IP will
 * normally be, or it may explicitly depend upon an underlying
 * Overlay, in the way that HTTP, for example, is generally 
 * implemented over TCP/IP.
 *
 * If the Overlay is system-supported, traffic will be routed and
 * neighbors will be reached by making calls to operating system 
 * primitives such as sockets.
 * 
 * In some Overlays there is a method which, given an Address, returns 
 * another Address, a gateway, which can be used to route messages to 
 * the first.
 * 
 * XXX 'Overlay' is drifting towards a synonym for EndPoint.
 *
 * @author Jim Dixon
 */

public interface Overlay {

    public String transport();
    public String protocol();
    public Address address();
    
    public boolean equals(Object o);
    public int hashCode();
}
