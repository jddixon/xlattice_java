/* SchedulableAcceptor.java */
package org.xlattice.transport;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import org.xlattice.Acceptor;

/**
 * @author Jim Dixon
 */

public interface SchedulableAcceptor extends Acceptor {

    public SelectableChannel getChannel();

    public CnxListenerFactory getCnxListenerFactory();
    public SchedulableAcceptor setCnxListenerFactory(CnxListenerFactory f);

    public SelectionKey getKey();
    public SchedulableAcceptor setKey(SelectionKey key);

    public IOScheduler getReceiver();
    public SchedulableAcceptor setReceiver(IOScheduler receiver);
}
