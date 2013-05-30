/* PutCallBack.java */
package org.xlattice.overlay;

/**
 * This is a callback interface.
 *
 * @author Jim Dixon
 */
public interface PutCallBack extends CallBack {

    /**
     * The put has completed.  If the status is zero, it was
     * successful.  Otherwise it was unsuccessful.
     *
     * @param status application-specific status code.
     */
    public void finishedPut (int status);
}
