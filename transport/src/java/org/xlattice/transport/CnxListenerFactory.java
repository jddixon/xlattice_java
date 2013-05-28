/* CnxListenerFactory.java */
package org.xlattice.transport;

/**
 * Returns an instance of a ConnectionListener on each getInstance()
 * invocation.  Typically the factory will have been configured with
 * whatever state the listener needs.
 */
public interface CnxListenerFactory {

    ConnectionListener getInstance();

}
