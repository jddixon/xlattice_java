/* OutputInfo.java */
package org.xlattice.httpd.sitemaker;

/**
 * @author Jim Dixon
 */

public class OutputInfo {

    private String _dir;

    public OutputInfo () {}

    public String getDir () {
        return _dir;
    }

    public void setDir(String s) {
        if (s == null || s.equals(""))
            throw new IllegalArgumentException(
                    "null or empty directory name");
        _dir = s;
    }
}
