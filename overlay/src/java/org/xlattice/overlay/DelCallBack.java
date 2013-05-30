/* DelCallBack.java */
package org.xlattice.overlay;

/**
 * This is a callback interface.
 *
 * @author Jim Dixon
 */
public interface DelCallBack extends CallBack {

    /**
     * If the delete operation succeeded, returns zero.  
     * Otherwise returns a non-zero application-specific status code.
     *
     * @param status application-specific status code
     */
    public void finishedDel (int status);
}
