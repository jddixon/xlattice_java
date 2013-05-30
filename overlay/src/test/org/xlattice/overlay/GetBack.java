/* GetBack.java */
package org.xlattice.overlay;

/**
 * Simple test class.
 *
 * @author Jim Dixon
 */
public class GetBack implements GetCallBack {
    public int status = -1;
    public byte[] data;
    public boolean done;
    
    public void finishedGet (int status, byte[] data) {
        this.status = status;
        this.data   = data;
        done        = true;
    }
    public int getStatus() {
        return status;
    }
}
