/* AsyncTlsClientCnxFactory.java */
package org.xlattice.transport.tls;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.GeneralSecurityException;

import org.xlattice.crypto.tls.TlsSession;

/**
 * @author Jim Dixon
 */
public abstract class AsyncTlsClientCnxFactory extends AsyncTlsCnxFactory{
    
    protected AsyncTlsClientCnxFactory(TlsSession session)
                            throws GeneralSecurityException, IOException {
    
        super(session);
    }
    public AsyncTlsClientConnection getInstance() 
                            throws GeneralSecurityException, IOException {
        return new AsyncTlsClientConnection (tlsSession);
    }
}
