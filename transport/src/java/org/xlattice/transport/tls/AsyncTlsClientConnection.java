/* AsyncTlsClientConnection.java */
package org.xlattice.transport.tls;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import static javax.net.ssl.SSLEngineResult.HandshakeStatus.*;
import javax.net.ssl.*;

import java.nio.*;

import org.xlattice.crypto.tls.TlsSession;
import org.xlattice.crypto.tls.TlsClientEngine;
import org.xlattice.transport.SchedulableConnection;

/**
 *
 * @author Jim Dixon
 */
public class AsyncTlsClientConnection    extends AsyncTlsConnection {

    // INSTANCE VARIABLES ///////////////////////////////////////////
   
    // CONSTRUCTORS /////////////////////////////////////////////////
    protected AsyncTlsClientConnection(TlsSession session) 
                            throws GeneralSecurityException, IOException {
        super(session);
        if(session.isClient) {
            engine = (TlsClientEngine)session.getEngine();
            // XXX THIS MAY BE A MISTAKE ;-) XXX
            engine.setEnabledCipherSuites(TLS_ANONYMOUS_CIPHERS);
        } else {
            throw new IllegalStateException(
                "attempt to create client connection from server session");
        }
    }
    // PROPERTIES ///////////////////////////////////////////////////
    public final boolean isClient() {
        return true;
    }
    // INTERFACE ConnectionListener /////////////////////////////////
     /**
      * Tells the listener what connection it is listening to and
      * what input buffer the connection is using.
      * 
      * IOScheduler and SelectionKey are available from the connection.
      * This method should be called once and only once.
      *
      * This is a client connection, so this call will result in an
      * initial sendData() call.  
      *
      * @param cnx    reporting SchedulableConnection
      * @param buffer input data buffer
      */
    public void setConnection (SchedulableConnection cnx, ByteBuffer buffer) {
        super.setCnx(cnx, buffer);
        /* STUB */
    }
}
