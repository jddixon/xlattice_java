/* TlsServerEngine.java */
package org.xlattice.crypto.tls;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

public class TlsServerEngine                    extends TlsEngine {



    protected TlsServerEngine (TlsContext ctx, TlsSession sess)
                            throws GeneralSecurityException, IOException {
        super (ctx, sess);
        engine.setUseClientMode(false);
    }
    // SETUP/PROPERTIES /////////////////////////////////////////////
    public boolean getNeedClientAuth() {
        return engine.getNeedClientAuth();
    }
    public void setNeedClientAuth(boolean whether) {
        engine.setNeedClientAuth(whether);
    }
    public boolean getWantClientAuth() {
        return engine.getWantClientAuth();
    }
    public void setWantClientAuth(boolean whether) {
        engine.setWantClientAuth(whether);
    }
}
