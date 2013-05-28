/* BlockingPacketPort.java */
package org.xlattice.transport;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.xlattice.Address;

/**
 *
 * @author Jim Dixon
 */

public interface BlockingPacketPort {
    
    /** unconnected forms */
    public Address receiveFrom(ByteBuffer dest) throws IOException ;
    public int sendTo (ByteBuffer src, Address target)
                                                throws IOException ;
    
    /** connected forms */
    public int receive (ByteBuffer dest)        throws IOException ;
    public int send (ByteBuffer src)            throws IOException ;
}
