/* PutBack.java */
package org.xlattice.overlay;

/**
 * Simple test class.
 *
 * @author Jim Dixon
 */
public class PutBack implements PutCallBack {
    public int status = -1;
    public void finishedPut (int status) {
        this.status = status;
    }
    public int getStatus () {
        return status;
    }
}
