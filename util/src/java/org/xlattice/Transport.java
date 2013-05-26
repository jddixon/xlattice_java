/* Transport.java */
package org.xlattice;

/**
 * Abstraction of the transport protocol used over a communications 
 * channel.
 *
 * XXX As it has been interpreted so far, this is unsatisfactory.  
 * For example, if there are several nodes on a host, each will have its 
 * own keystore.  If the Transport is Tls, any Acceptor created through 
 * this interface would need to have access to that keystore.  Therefore
 * what's needed is an instance of a Transport provider/factory associated 
 * with the node, with a reference to the keystore either in the 
 * constructor or passed in a setter.  
 * 
 * Dichotomies are: blocking versus non-blocking, reliable vs unreliable.
 *
 * @author Jim Dixon
 */
public interface Transport {

    // XXX v0.3.8 distinguishes connection-oriented transports (tls, tcp) 
    // XXX from connectionless (udp)
//  /**
//   * Create an Acceptor with the local address specified.  The
//   * Acceptor listens for attempts to establish connections on
//   * that address and then creates connections to the contacting
//   * node (client) possibly on the same local address, possibly
//   * on another, according to the transport protocol.
//   *
//   * XXX Need to be able to specify the protocol for the 
//   * XXX connection created and whether that connection is blocking;
//   * XXX need an AcceptorListener (default should be same protocol,
//   * XXX and non-blocking).
//   *
//   * @param near     local address on which the Acceptor listens
//   * @param blocking whether the Acceptor itself blocks
//   */
//  public Acceptor getAcceptor   (Address near, boolean blocking)
//                                              throws IOException;
 
//  // XXX THIS SHOULD BE DROPPED ENTIRELY
//  public Connection getConnection (Address near, Address far,
//                          boolean blocking)   throws IOException;
//  /**
//   * Get a Connector for use in setting up connections to a 
//   * remote host.  The Connector itself may be blocking or non-blocking;
//   * that is, Connector.connect() may or may not block.  That is 
//   * specified here.  Whether the new Connection is itself blocking
//   * is determined by a Connector.connect() parameter.
//   * 
//   * @param far      address of the remote Acceptor
//   * @param blocking whether this Connector is itself blocking
//   */
//  public Connector  getConnector  (Address far, boolean blocking)
//                                              throws IOException;

    /** @return a name for this transport protocol, useful in debugging */
    public String name();
}
