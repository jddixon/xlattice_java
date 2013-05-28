/* MockCnxListenerFactory.java */
package org.xlattice.transport.mockery;

import org.xlattice.transport.CnxListenerFactory;
import org.xlattice.transport.ConnectionListener;

/**
 * For testing; returns a mock ConnectionListener.
 *
 * @author Jim Dixon
 */
public class MockCnxListenerFactory implements CnxListenerFactory {

    public ConnectionListener getInstance () {
        return new MockConnListener();
    }
} 
