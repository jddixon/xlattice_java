/* IMemCache.java */
package org.xlattice.overlay.datakeyed;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.xlattice.CryptoException;
import org.xlattice.NodeID;
import org.xlattice.crypto.SHA1Digest;
import org.xlattice.crypto.SignedList;
import org.xlattice.overlay.CallBack;
import org.xlattice.overlay.DataKeyed;
import org.xlattice.overlay.GetCallBack;
import org.xlattice.overlay.DelCallBack;
import org.xlattice.overlay.PutCallBack;
import org.xlattice.util.NonBlockingLog;
import org.xlattice.util.StringLib;         // DEBUG

/**
 * This class is intended to be thread safe.
 *
 * @author Jim Dixon
 */
public interface IMemCache extends DataKeyed {
   
    // Cannot be part of the interface because 'final static'
    // public final static MemCache getInstance(); 
    // public final static MemCache getInstance(String s); 
    
    // LOGGING //////////////////////////////////////////////////////
    /** Subclasses should override.  */
    public void DEBUG_MSG(String msg);
    public void ERROR_MSG(String msg);
    
    // PROPERTIES ///////////////////////////////////////////////////
    public void add (NodeID id, byte[] b);
    public long byteCount ();
    public void clear();
    public int itemCount ();
    public String getPathToXLattice ();
}
