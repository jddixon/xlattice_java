/* BindingRequest.java */
package org.xlattice.protocol.stun;

/**
 *
 * @author Jim Dixon
 */
public class BindingRequest extends StunMsg {



    public BindingRequest () {
        super (BINDING_REQUEST);
    }
    public BindingRequest(byte[] msgID) {
        super (BINDING_REQUEST, msgID);
    }

}
