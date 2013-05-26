/* EndPoint.java */
package org.xlattice;

/**
 * An EndPoint is specified by a transport and an Address, including
 * the local part.  If the transport is TCP/IP, for example, the 
 * Address includes the IP address and the port number.
 *
 * @author Jim Dixon
 */
public class EndPoint {

    private final Transport transport;
    private final Address   addr_;

    public EndPoint (Transport t, Address addr) {
        transport = t;
        addr_     = addr;
    }
    // PROPERTIES ///////////////////////////////////////////////////
    public Address getAddress() {
        return addr_;
    }
    public Transport getTransport() {
        return transport;
    }

//  /**
//   * XXX reference to transport is just copied 
//   *
//   * 2006-02-28 dropped this method.
//   */
//  public Object clone () {
//      return new EndPoint (transport, (Address)addr_.clone());
//  }
    // SERIALIZATION ////////////////////////////////////////////////
    public String toString () {
        return new StringBuffer(transport.name())
                            .append(" ")
                            .append(addr_.toString())
                            .toString();
    }
}
