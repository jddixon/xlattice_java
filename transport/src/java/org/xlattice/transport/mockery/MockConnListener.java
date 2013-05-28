/* MockConnListener.java */
package org.xlattice.transport.mockery;

import java.nio.ByteBuffer;
import org.xlattice.transport.ConnectionListener;
import org.xlattice.transport.SchedulableConnection;

/**
 * @author Jim Dixon
 */

public class MockConnListener implements ConnectionListener {
    public void setConnection (SchedulableConnection cnx, ByteBuffer buffer) {
    }
    public void dataSent () {
    }
    public void dataReceived () {
    }
    public void reportDisconnect () {
    }
    public void reportException (Exception exc) {
    }
}
