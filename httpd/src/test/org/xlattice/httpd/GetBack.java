/* GetBack.java */
package org.xlattice.httpd;

import org.xlattice.overlay.GetCallBack;

/**
 * Simple test class.
 *
 * @author Jim Dixon
 */
public class GetBack implements GetCallBack {
    public int status = -1;
    public byte[] data;
    public void finishedGet (int status, byte[] data) {
        this.status = status;
        this.data   = data;
    }
    public int getStatus() {
        return status;
    }
}
