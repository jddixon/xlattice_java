/* BindingResponse.java */
package org.xlattice.protocol.stun;

/**
 * A BindingResponse must have MappedAddress, SourceAddress, and
 * ChangedAddress attributes.  
 *
 * It might have ReflectedFrom and MessageIntegrity attributes.  It 
 * may NOT have ResponseAddress, ChangeRequest, UserName, Password, 
 * ErrorCode, or UnknownAttribute attributes.
 *
 * @author Jim Dixon
 */
public class BindingResponse extends StunMsg {

    public BindingResponse () {
        super (BINDING_RESPONSE);
    }
    public BindingResponse(byte[] msgID) {
        super (BINDING_RESPONSE, msgID);
    }


}
