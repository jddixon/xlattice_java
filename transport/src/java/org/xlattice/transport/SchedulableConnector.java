/* SchedulableConnector.java */
package org.xlattice.transport;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import org.xlattice.Connector;

/**
 * @author Jim Dixon
 **/

public interface SchedulableConnector extends Connector {

    /** An experiment. */
    public SchedulableConnection connection()   throws IOException;
    
    public SelectableChannel getChannel();

    public CnxListenerFactory getCnxListenerFactory();
    public SchedulableConnector setCnxListenerFactory(CnxListenerFactory f);

    public SelectionKey getKey();
    public SchedulableConnector setKey(SelectionKey key);

    public IOScheduler getReceiver();
    public SchedulableConnector setReceiver(IOScheduler receiver);
}
