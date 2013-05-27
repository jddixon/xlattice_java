/* TlsClientEngine.java */
package org.xlattice.crypto.tls;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

public class TlsClientEngine                    extends TlsEngine {


    protected TlsClientEngine (TlsContext ctx, TlsSession sess)
                            throws GeneralSecurityException, IOException {
        super (ctx, sess);
        engine.setUseClientMode(true);
    }

}
