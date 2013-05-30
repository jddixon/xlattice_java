/* DelBack.java */
package org.xlattice.overlay;

/**
 * Simple test class, a mock.
 *
 * @author Jim Dixon
 */
public class DelBack implements DelCallBack {
    public int status = -1;
    public void finishedDel (int status) {
        this.status = status;
    }
    public int getStatus() {
        return status;
    }
}
