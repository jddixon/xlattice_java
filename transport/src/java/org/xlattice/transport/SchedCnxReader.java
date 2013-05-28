/* SchedCnxReader.java */
package org.xlattice.transport;

/**
 * That portion of the SchedulableConnection interface that a 
 * ConnectionListener may pass off to others.  This interface is
 * used to signal after an unpredictable delay that the using
 * thread is prepared to receive data.
 *
 * @deprecated
 *
 * @author Jim Dixon
 */
public interface SchedCnxReader {
    
    public void initiateReading();

}
