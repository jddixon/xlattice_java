/* NameKeyedWriter.java */
package org.xlattice.overlay;

/**
 *
 *
 * @author Jim Dixon
 */
public interface NameKeyedWriter {
    public void delete (final String key, DelCallBack listener);
    public void put (final String key, byte[] buffer, PutCallBack listener);
}
