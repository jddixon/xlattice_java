/* SchedCnxWriter.java */
package org.xlattice.transport;

import java.nio.ByteBuffer;

/** 
 * The limited view of a SchedulableConnection that it is 
 * appropriate for a ConnectionListener to pass on to others.
 *
 * @deprecated
 *
 * @author Jim Dixon
 */
public interface SchedCnxWriter {
    /**
     * Initiate the sending of data.
     *
     * @param buffer holds data to be transmitted
     */
    public void sendData (ByteBuffer buffer);

    /**
     * Our quaint way to signal that there is a problem.
     */
    public void close();
}
