/* MockSocketChannel.java */
package org.xlattice.transport.mockery;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Set;

/**
 * @author Jim Dixon
 **/

public class MockSocketChannel extends SocketChannel {

    private boolean connected;
    private boolean connectionPending;
    private Socket mySocket;

    private static SelectorProvider myProvider = MockProvider.getInstance();
    
    // CONSTRUCTORS /////////////////////////////////////////////////
    public MockSocketChannel () {
        super(myProvider);
        mySocket = new MockSocket();
    }
    // SPECIAL TEST METHODS /////////////////////////////////////////
    public int getState() {
        return ((MockSocket)mySocket).getState();
    }
    // SOCKET CHANNEL METHODS ///////////////////////////////////////
    // Input and output streams have to be added at the MockSocket
    // level.  Binding must also occur at that level
   
    public SocketChannel bind(SocketAddress local) throws IOException {
        // STUB
        return null;
    }
    public boolean connect (SocketAddress remote) {
        // STUB
        // We want the state to become PENDING
        return false;
    }
    public SocketAddress getLocalAddress() throws IOException {
        // STUB
        return null;
    }
    public <T>T getOption(SocketOption<T> name) throws IOException {
        // STUB
        return null;
    }
    public SocketAddress getRemoteAddress() throws IOException {
        // STUB
        return null;
    }
    public boolean finishConnect() {
        // STUB
        return false;
    }
    public boolean isConnected() {
        return connected;
    }
    public boolean isConnectionPending() {
        return connectionPending;
    }
    /**
     *
     * @throws NotYetConnectedException if channel not yet connected.
     */
    public int read (ByteBuffer dest) {
        // STUB
        return -1;      // signifies EOF
    }
    public long read (ByteBuffer[] dests, int offset, int length) {
        // STUB
        return -1;
    }
    public <T>SocketChannel setOption(SocketOption<T> name, T value) {
        // STUB
        return null;
    }
    public SocketChannel shutdownInput() throws IOException {
        // STUB
        return null;
    }
    public SocketChannel shutdownOutput() throws IOException {
        // STUB
        return null;
    }
    public Socket socket() {
        return mySocket;
    }
    public Set<SocketOption<?>> supportedOptions() {
        // STUB
        return null;
    }
    // public int validOps() is final
   
    /**
     *
     */
    public int write (ByteBuffer src) {
        // STUB
        return 0;
    }
    public long write (ByteBuffer[] srcs, int offset, int length) {
        // STUB
        return 0;
    }
    // ABSTRACT SELECTABLE CHANNEL METHODS //////////////////////////
    private boolean blocking;

    // public final Object blockingLock() 
    // public final SelectableChannel configureBlocking(boolean block)
    // protected final void implCloseChannel()
   
    protected void implCloseSelectableChannel() {
        // STUB
    }
    
    protected void implConfigureBlocking(boolean block) {
        // called by configureBlocking
        blocking = block;
    }
    // public final boolean isBlocking() 
    // public final isRegistered()
    // public final SelectionKey keyFor(Selector sel)
    // public final SelectorProvider provider()
    // public final SelectionKey register(Selector sel, int ops, Object att)

    // SELECTABLE CHANNEL METHODS ///////////////////////////////////
    // public final register(Selector sel, int ops)
    
    // ABSTRACT INTERRUPTIBLE CHANNEL METHODS ///////////////////////
    // protected final void begin()
    // protected final void end(boolean completed)
    // CHANNEL METHODS //////////////////////////////////////////////
    // public void close () 
    // public boolean isOpen() is final
}
