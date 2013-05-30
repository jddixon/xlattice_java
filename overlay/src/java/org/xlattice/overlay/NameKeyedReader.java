/* NameKeyedReader.java */
package org.xlattice.overlay;

/**
 * @author Jim Dixon
 **/

public interface NameKeyedReader {
    public void get (final String key, GetCallBack listener);
}
