/* HttpSCallBacks.java */
package org.xlattice.httpd;

import org.xlattice.overlay.GetCallBack;
import org.xlattice.overlay.PutCallBack;

/**
 * CallBacks that must be implemented.  This will be extended
 * further.  Methods GET, HEAD, and POST use GetCallBacks.
 *
 *
 * @author Jim Dixon
 */
public interface HttpSCallBacks extends GetCallBack
                    // , PutCallBack 
                                                        {
}
