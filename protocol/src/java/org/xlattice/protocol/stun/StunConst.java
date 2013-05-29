/* StunConst.java */
package org.xlattice.protocol.stun;


/**
 *
 * @author Jim Dixon
 */

public interface StunConst  {

    // StunMsg constants ////////////////////////////////////////////
    public final static int BINDING_REQUEST              = 0x0001;
    public final static int BINDING_RESPONSE             = 0x0101;
    public final static int BINDING_ERROR_RESPONSE       = 0x0111;
    public final static int SHARED_SECRET_REQUEST        = 0x0002;
    public final static int SHARED_SECRET_RESPONSE       = 0x0102;
    public final static int SHARED_SECRET_ERROR_RESPONSE = 0x0112;

    public final static int MSG_ID_LENGTH = 16;    // bytes, so 128 bits
    public final static int HEADER_LENGTH = 4 + MSG_ID_LENGTH;

    public final static int STUN_SERVER_PORT = 3478;

    // StunAttr constants ///////////////////////////////////////////
    public static final int MAPPED_ADDRESS      = 0x0001;
    public static final int RESPONSE_ADDRESS    = 0x0002;
    public static final int CHANGE_REQUEST      = 0x0003;
    public static final int SOURCE_ADDRESS      = 0x0004;
    public static final int CHANGED_ADDRESS     = 0x0005;
    public static final int USERNAME            = 0x0006;
    public static final int PASSWORD            = 0x0007;
    public static final int MESSAGE_INTEGRITY   = 0x0008;
    public static final int ERROR_CODE          = 0x0009;
    public static final int UNKNOWN_ATTRIBUTES  = 0x000a;
    public static final int REFLECTED_FROM      = 0x000b;


    // *some* of the new RFC3489 bis extensions /////////////////////
    public static final int XOR_ONLY            = 0x0021;
    public static final int XOR_MAPPED_ADDRESS  = 0x8020;
    public static final int SERVER_NAME         = 0x8022;
    public static final int SECONDARY_ADDRESS   = 0x8050;
    
    /** change request parameters; get ORed together */
    public static final int CHANGE_IP           = 4;
    public static final int CHANGE_PORT         = 2;
}
