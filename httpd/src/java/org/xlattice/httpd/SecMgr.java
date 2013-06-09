/* SecMgr.java */
package org.xlattice.httpd;

import java.io.File;
import java.io.FileDescriptor;

/**
 * A minimal security manager.  XXX We probably don't need one, given
 * that we so strictly restrict the interpretation of Strings from the
 * outside world.
 *
 * @author Jim Dixon
 */
class SecMgr extends SecurityManager { 
 
    // STUBS XXX ////////////////////////////////////////////////////
    public void checkAccept (String host, int port) { };
    public void checkAccess (Thread g) { };
    public void checkLink   (String lib) { };
    public void checkListen (int port) { };
    public void checkPropertyAccess(String key) { };
    public void checkRead   (FileDescriptor fd  ) {  };
    
    public void checkRead   (String s) { 
        if ( (s.indexOf("..") != -1) || new File(s).isAbsolute() ) {
            throw new SecurityException( "illegal path: " + s);
        }
    }
    public void checkWrite(FileDescriptor fd) { };
 
}
