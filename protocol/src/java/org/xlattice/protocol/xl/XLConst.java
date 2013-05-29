/* XLConst.java */
package org.xlattice.protocol.xl;

/** 
 * All constant values are big-endian.
 * 
 * XXX These values are interim: they are subject to change at
 * any time.
 *
 * @author Jim Dixon
 */
public interface XLConst {

    public static final int HEADER_LENGTH = 20;
    public static final int MSG_ID_LENGTH = 12;

    // labels ///////////////////////////////////////////////////////
    public static final int LABEL = 0x8000;
    
    /** these fields are not shifted */
    public static final int EXP_MASK     =  7;
    public static final int BOTTOM_MASK  = 0x100;
    public static final int TTL_MASK     = 0x000000ff;
    
    // message types ////////////////////////////////////////////////
    public static final int PING         = 0x8001;
    public static final int PONG         = 0x8002;
    
    // attributes ///////////////////////////////////////////////////
    public static final int SOURCE       = 0x0001;
    public static final int DESTINATION  = 0x0002;

}
