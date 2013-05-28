/* EchoSFactory.java */
package org.xlattice.transport;

public class EchoSFactory implements CnxListenerFactory {

    public ConnectionListener getInstance() {
        return new EchoSListener();
    }
}
