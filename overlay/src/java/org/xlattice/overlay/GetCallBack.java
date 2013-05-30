/* GetCallBack.java */
package org.xlattice.overlay;

/**
 * This is a callback interface.
 *
 * @author Jim Dixon
 */
public interface GetCallBack extends CallBack {

    /**
     * If whatever was requested was found, it is returned as the
     * value of the byte array and the status code is zero; otherwise 
     * the byte array is null and the status code is non-zero.
     *
     * @param status application-specific status code
     * @param data   requested value as byte array
     */
    public void finishedGet (int status, byte[] data);
}
