/* StunMsg.java */
package org.xlattice.protocol.stun;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Vector;

import org.xlattice.protocol.TLV16;
import org.xlattice.util.StringLib;
import org.xlattice.util.UIntLib;

/**
 *
 * @author Jim Dixon
 */

public abstract class StunMsg               implements StunConst {

    /** any use must be synchronized */
    protected final static SecureRandom rng = new SecureRandom();

    // INSTANCE VARIABLES ///////////////////////////////////////////
    public final int type;
    private      int length;                    // initially zero
    private      byte[] msgID;

    private final Vector attributes;
   
    protected StunMsg (int type) {
        this(type, null);
    }
    protected StunMsg (int type, byte[] msgID) {
        // XXX check type ?
        this.type = type;

        if (msgID == null) {
            msgID = new byte[MSG_ID_LENGTH];
            synchronized (rng) {
                rng.nextBytes(msgID);
            }
        } else {
            if (msgID.length != MSG_ID_LENGTH)
                throw new IllegalArgumentException(
                        "bad message ID length: " + msgID.length);
        }
        this.msgID = msgID;
        attributes = new Vector (2, 2);
    }
    // ACCESS METHODS ///////////////////////////////////////////////
    /**
     * The total length of any attributes, including the type/length
     * fields.
     */
    public int length () {
        return length;
    }
    public byte[] getMsgID() {
        return msgID;
    }
    /**
     * Add a TLV16 attribute to the message, updating the header's
     * length field.
     */
    protected void add (StunAttr attr) {
        if (attr == null) 
            throw new IllegalArgumentException("null attribute");
        attributes.add(attr);
        // allow 4 bytes for type and attr value length
        length += 4 + attr.length();
    }
    protected StunAttr get (int n) {
        return (StunAttr)attributes.get(n);
    }
    /** @return number of attributes */
    public int size() {
        return attributes.size();
    }
    /** @return byte length of serialized message */
    public int wireLength() {
        return HEADER_LENGTH + length;
    }
    // SERIALIZATION ////////////////////////////////////////////////
    public void encode (byte[] outBuf) {
        if (outBuf == null)
            throw new IllegalArgumentException("null output buffer");
        if (outBuf.length < wireLength())
            throw new IllegalStateException("output buffer of length " 
                    + outBuf.length + " cannot hold message of length "
                    + wireLength());
        if (msgID == null)
            throw new IllegalStateException("null message ID");
        
        outBuf[0] = (byte) (0xff & (type >> 8));
        outBuf[1] = (byte) type;
        outBuf[2] = (byte) (0xff & (length >> 8));
        outBuf[3] = (byte) length;
        System.arraycopy( msgID, 0, outBuf, 4, MSG_ID_LENGTH);
        
        int offset = HEADER_LENGTH;
        int count  = size();
        for (int i = 0; i < count; i++) {
            StunAttr attr = (StunAttr)attributes.get(i);
            attr.encode (outBuf, offset);
            offset += 4 + attr.length();
        }
    }
    // DESERIALIZATION //////////////////////////////////////////////
    /**
     * Read and attach attributes to previously deserialized header.
     *
     * @param msg    StunMsg being created
     * @param inBuf  buffer containing serialized message
     * @param msgLen total number of bytes in attributes
     */
    protected static void decodeAttr (StunMsg msg, byte[] inBuf, int msgLen) 
                                                throws IOException {
        int offset = HEADER_LENGTH;
        while (offset < HEADER_LENGTH + msgLen) {
            StunAttr attr = (StunAttr) StunAttr.decode (inBuf, offset);
            msg.add(attr);
            offset += 4 + attr.length();
        }
    }
    public static StunMsg decode (byte[] inBuf) 
                throws IllegalArgumentException, IllegalStateException, 
                                                        IOException {
        if (inBuf == null)
            throw new IllegalArgumentException("null in buffer");
        if (inBuf.length < HEADER_LENGTH)
            throw new IllegalStateException("in buffer too short: "
                    + inBuf.length);
        int _type   = UIntLib.decodeUInt16(inBuf, 0);
        int _length = UIntLib.decodeUInt16(inBuf, 2);
        byte[] _msgID = new byte[ MSG_ID_LENGTH];
        System.arraycopy (inBuf, 4, _msgID, 0, MSG_ID_LENGTH);
        StunMsg msg = null;
        
        switch (_type) {
            case BINDING_REQUEST:
                BindingRequest req  = new BindingRequest(_msgID);
                msg = req;
                break;
                
            case BINDING_RESPONSE:
                BindingResponse resp = new BindingResponse(_msgID);
                msg = resp;
                break;

            case BINDING_ERROR_RESPONSE:
                BindingErrorResponse bError 
                                    = new BindingErrorResponse(_msgID);
                msg = bError;
                break;

            case SHARED_SECRET_REQUEST:
                SharedSecretRequest ssReq 
                                    = new SharedSecretRequest(_msgID);
                msg = ssReq;
                break;

            case SHARED_SECRET_RESPONSE:
                SharedSecretResponse ssResp 
                                    = new SharedSecretResponse(_msgID);
                msg = ssResp;
                break;

            case SHARED_SECRET_ERROR_RESPONSE:
                SharedSecretErrorResponse ssErr 
                                    = new SharedSecretErrorResponse(_msgID);
                msg = ssErr;
                break;

            default:
                throw new IllegalStateException(
                        "unrecognized StunMsg type " + _type);
        }
        if (_length > 0)
            decodeAttr (msg, inBuf, _length);
        return msg;
    } 
}
