/* SharedSecretErrorResponse.java */
package org.xlattice.protocol.stun;

/**
 *
 * @author Jim Dixon
 */
public class SharedSecretErrorResponse extends StunMsg {

    public SharedSecretErrorResponse () {
        super (SHARED_SECRET_ERROR_RESPONSE);
    }
    public SharedSecretErrorResponse(byte[] msgID) {
        super (SHARED_SECRET_ERROR_RESPONSE, msgID);
    }


}
