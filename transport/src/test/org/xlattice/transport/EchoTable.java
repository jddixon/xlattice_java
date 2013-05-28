/* EchoTable.java */
package org.xlattice.transport;

import org.xlattice.util.NonBlockingLog;
import org.xlattice.util.Timestamp;

/**
 * @author Jim Dixon
 **/

public class EchoTable {

    // DEBUG
    private static NonBlockingLog debugLog 
                        = NonBlockingLog.getInstance("debug.log");
    private void DEBUG_MSG(String s) {
        debugLog.message("EchoTable" + s);
    }
    // END
    private final int _size;
    private int count;
    private boolean finished;
    
    byte[][] dataSent;
    byte[][] dataRcvd;
    Timestamp[] timeSent;
    Timestamp[] timeRcvd;

    public EchoTable (int n) {
        _size    = n;
        dataSent = new byte[n][];
        dataRcvd = new byte[n][];
        timeSent = new Timestamp[n];
        timeRcvd = new Timestamp[n];
    }
    public void recordSend (int which, byte[] dataOut) {
        DEBUG_MSG(".recordSend(" + which + ", dataOut)");
        timeSent[which] = new Timestamp();
        synchronized (dataSent) {
            dataSent[which] = dataOut;
        }
    }
    public void recordReceive (int which, byte[] dataIn) {
        DEBUG_MSG(".recordReceive(" + which + ", dataIn)");
        timeRcvd[which] = new Timestamp();
        synchronized (dataRcvd) {
            dataRcvd[which] = dataIn;
            count++;
        }
        DEBUG_MSG(".recordReceive, count is " + count);
        if (count >= _size) {
            DEBUG_MSG("    all table rows complete");
            synchronized (this) {
                finished = true;
                notifyAll();
            }
        }
    }
    public boolean isFinished() {
        return finished;
    }
    public int seenSoFar() {
        return count;
    }
    public int size() {
        return _size;
    }
}
