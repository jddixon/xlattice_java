/* MockSelectionKey.java */
package org.xlattice.transport.mockery;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

/**
 * @author Jim Dixon
 **/

public class MockSelectionKey extends SelectionKey {

    // this is a mock; no reason to hide these
    boolean cancelled = false;
    int interestSet;
    int readyOpSet;
    Selector mySelector;

    public MockSelectionKey() {}

    // attach() and attachment() are final

    public void cancel() {
        cancelled = true;
        // STUB
    }
    public SelectableChannel channel() {
        // STUB
        return null;
    }
    public int interestOps() {
        return interestSet;         // bit vector
    }
    public SelectionKey interestOps(int ops) {
        interestSet = ops;
        return this;
    }
    
    // is{Acceptable,Connectable,Readable,Writable}() are final */
    
    public boolean isValid() {
        return !cancelled;
    }
    public int readyOps() {
        return readyOpSet;
    }
    public Selector selector() {
        return mySelector;
    }
}
