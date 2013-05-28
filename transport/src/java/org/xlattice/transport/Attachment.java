/* Attachment.java */
package org.xlattice.transport;

/**
 * @author Jim Dixon
 */
class Attachment {
    
    // DEBUG
    // STATICS //////////////////////////////////////////////////////
    static int nextIndex = 0;
    static Object indexLock = new Object();
    // END

   
    // CONSTANTS ////////////////////////////////////////////////////
    public final static int NIX_A = 0;
    public final static int ACC_A = 1;      // Acceptor
    public final static int CNX_A = 2;      // Connection
    public final static int CTR_A = 3;      // Connector
    public final static int PKT_A = 4;      // PacketPort
    
    // INSTANCE VARIABLES ///////////////////////////////////////////
    int    type;
    Object obj;
    // DEBUG
    final int index;
    // END
    
    // CONSTRUCTORS /////////////////////////////////////////////////
    protected Attachment (int t, Object o) {
        // DEBUG
        synchronized (indexLock) {
            index = nextIndex++;
        }
        // END
        if (t < ACC_A || t > PKT_A)
            throw new IllegalArgumentException("unrecognized type " + t);
        if (o == null)
            throw new IllegalArgumentException("object may not be null");
        type = t;
        obj  = o;
    }
}

