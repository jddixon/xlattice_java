/* MockProvider.java */
package org.xlattice.transport.mockery;

import java.net.ProtocolFamily;
import java.nio.channels.*;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;

/**
 * @author Jim Dixon
 **/

public class MockProvider extends SelectorProvider {

    private final static MockProvider INSTANCE = new MockProvider();
    
    private MockProvider() {}

    // SelectorProvider INTERFACE ///////////////////////////////////
    public DatagramChannel openDatagramChannel() { 
        /* STUB */ 
        return null; 
    }
    public DatagramChannel openDatagramChannel(ProtocolFamily family) { 
        /* STUB */ 
        return null; 
    }
    public Pipe openPipe() { 
        /* STUB */ 
        return null; 
    }
    public AbstractSelector openSelector() { 
        /* STUB */ 
        return null; 
    }
    public ServerSocketChannel openServerSocketChannel() { 
        /* STUB */ 
        return null;
    }
    public SocketChannel openSocketChannel() { 
        return new MockSocketChannel();
    }
    // OTHER METHODS ////////////////////////////////////////////////
    public static MockProvider getInstance() {
        return INSTANCE;
    }
}
