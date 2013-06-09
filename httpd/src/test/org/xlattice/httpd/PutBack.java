/* PutBack.java */
package org.xlattice.httpd;

import org.xlattice.overlay.PutCallBack;

/**
 * Simple test class, a mock.
 *
 * @author Jim Dixon
 */
public class PutBack implements PutCallBack {
    public int status;
    public void finishedPut (int status) {
        this.status = status;
    }
    public int getStatus() {
        return status;
    }
}
