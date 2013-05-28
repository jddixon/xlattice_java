/* AsyncTlsCnxFactory.java */
package org.xlattice.transport.tls;

import java.io.IOException;
//import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.security.GeneralSecurityException;
//import javax.net.ssl.*;

//import java.nio.*;

import org.xlattice.crypto.tls.TlsSession;
//import org.xlattice.transport.AsyncPacketConnection;
//import org.xlattice.transport.AsyncPacketHandler;
//import org.xlattice.transport.ConnectionListener;
//import org.xlattice.transport.SchedulableConnection;
//import org.xlattice.transport.tcp.SchedulableTcpConnection;

/**
 * @author Jim Dixon
 */
public abstract class AsyncTlsCnxFactory {

    protected final TlsSession tlsSession;
    
    protected AsyncTlsCnxFactory(TlsSession session)
                            throws GeneralSecurityException, IOException {
        if (session == null)
            throw new IllegalArgumentException("null TLS config");
        tlsSession = session;
    }
}
