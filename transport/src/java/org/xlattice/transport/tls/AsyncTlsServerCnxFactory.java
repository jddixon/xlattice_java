/* AsyncTlsServerCnxFactory.java */
package org.xlattice.transport.tls;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.GeneralSecurityException;

import org.xlattice.crypto.tls.TlsSession;

/**
 * @author Jim Dixon
 */
public abstract class AsyncTlsServerCnxFactory extends AsyncTlsCnxFactory{
    
    protected AsyncTlsServerCnxFactory(TlsSession session)
                            throws GeneralSecurityException, IOException {
    
        super(session);
    }
    public AsyncTlsServerConnection getInstance() 
                            throws GeneralSecurityException, IOException {
        return new AsyncTlsServerConnection (tlsSession);
    }
}
