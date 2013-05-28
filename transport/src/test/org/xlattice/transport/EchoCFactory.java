/* EchoCFactory.java */
package org.xlattice.transport;

public class EchoCFactory implements CnxListenerFactory {

    public final int N;
    public final EchoTable TABLE;
    
    public EchoCFactory (int n, EchoTable table) {
        N     = n;
        TABLE = table;
    }
    public ConnectionListener getInstance() {
        return new EchoTableCListener(N, TABLE);
    }
}
