/* SharedSecretRequest.java */
package org.xlattice.protocol.stun;

/**
 *
 * @author Jim Dixon
 */
public class SharedSecretRequest extends StunMsg {



    public SharedSecretRequest () {
        super (SHARED_SECRET_REQUEST);
    }
    public SharedSecretRequest(byte[] msgID) {
        super (SHARED_SECRET_REQUEST, msgID);
    }

}
