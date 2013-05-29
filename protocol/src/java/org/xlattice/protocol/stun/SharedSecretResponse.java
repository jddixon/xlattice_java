/* SharedSecretResponse.java */
package org.xlattice.protocol.stun;

/**
 * A SharedSecretResponse must have UserName and Password attributes.
 *
 * @author Jim Dixon
 */
public class SharedSecretResponse extends StunMsg {

    public SharedSecretResponse () {
        super (SHARED_SECRET_RESPONSE);
    }
    public SharedSecretResponse(byte[] msgID) {
        super (SHARED_SECRET_RESPONSE, msgID);
    }


}
