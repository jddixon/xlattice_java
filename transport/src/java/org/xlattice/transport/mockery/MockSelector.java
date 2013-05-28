/* MockSelector.java */
package org.xlattice.transport.mockery;

import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;

/**
 * @author Jim Dixon
 **/

import java.util.Set;

public class MockSelector extends Selector {

    boolean closed;
    
    public MockSelector () {}

    public void close() {
        closed = true;
    }
    public boolean isOpen() {
        return !closed;
    }
    public Set keys() {
        // STUB
        return null;
    }
    public SelectorProvider provider() {
        // STUB
        return null;
    }
    public int select () {
        // STUB
        return 0;
    }
    public int select (long timeout) {
        // STUB
        return 0;
    }
    public Set selectedKeys() {
        // STUB
        return null;
    }
    public int selectNow () {
        // STUB
        return 0;
    }
    public Selector wakeup() {
        // STUB
        return this;
    }
}
