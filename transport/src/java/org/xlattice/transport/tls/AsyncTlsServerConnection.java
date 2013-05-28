/* AsyncTlsServerConnection.java */
package org.xlattice.transport.tls;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import static javax.net.ssl.SSLEngineResult.HandshakeStatus.*;
import javax.net.ssl.*;

import java.nio.*;

import org.xlattice.crypto.tls.TlsSession;
import org.xlattice.crypto.tls.TlsServerEngine;
//import org.xlattice.transport.ConnectionListener;
import org.xlattice.transport.SchedulableConnection;

/**
 * Bear in mind that the TCP server end of a connection need not be
 * the TLS server end - it may be a TLS client - although it may be
 * a useful simplification to require that the TCP server end also be
 * the TLS server end.
 *
 * @author Jim Dixon
 */
public class AsyncTlsServerConnection         extends AsyncTlsConnection {

    // INSTANCE VARIABLES ///////////////////////////////////////////
   
    // CONSTRUCTORS /////////////////////////////////////////////////
    protected AsyncTlsServerConnection(TlsSession session)
                            throws GeneralSecurityException, IOException {
        super(session);
        if (session.isClient) {
            throw new IllegalStateException(
                "attempt to create server connection from client session");
        } else {
            engine = (TlsServerEngine)session.getEngine();
            // XXX THIS IS PROBABLY WRONG XXX
            engine.setEnabledCipherSuites(TLS_ANONYMOUS_CIPHERS);
            ((TlsServerEngine)engine).setNeedClientAuth(false); // XXX DITTO
        }
    }
    // PROPERTIES ///////////////////////////////////////////////////
    public final boolean isClient() {
        return false;
    }
    // INTERFACE ConnectionListener /////////////////////////////////
     /**
      * Tells the listener what connection it is listening to and
      * what input buffer the connection is using.
      * 
      * IOScheduler and SelectionKey are available from the connection.
      * This method should be called once and only once.
      *
      * This is a TLS server connection, so it will result in an 
      * initiateReading() call on the connection.
      *
      * @param cnx    reporting SchedulableConnection
      * @param buffer input data buffer
      */
    public void setConnection (SchedulableConnection cnx, ByteBuffer buffer) {
        super.setCnx(cnx, buffer);
        /* STUB */
    } 
}
