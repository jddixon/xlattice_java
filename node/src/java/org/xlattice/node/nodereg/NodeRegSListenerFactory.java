/* NodeRegSListenerFactory.java */
package org.xlattice.node.nodereg;

import org.xlattice.transport.ConnectionListener;
import org.xlattice.transport.CnxListenerFactory;

public class NodeRegSListenerFactory implements CnxListenerFactory {

    public ConnectionListener getInstance() {
        return new NodeRegSListener();
    }
}
