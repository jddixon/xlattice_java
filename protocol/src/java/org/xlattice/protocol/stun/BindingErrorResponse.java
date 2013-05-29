/* BindingErrorResponse.java */
package org.xlattice.protocol.stun;

/**
 *
 * @author Jim Dixon
 */
public class BindingErrorResponse extends StunMsg {

    public BindingErrorResponse () {
        super (BINDING_ERROR_RESPONSE);
    }
    public BindingErrorResponse(byte[] msgID) {
        super (BINDING_ERROR_RESPONSE, msgID);
    }


}
