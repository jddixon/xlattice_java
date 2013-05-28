/* TlsConnection.java */
package org.xlattice.transport.tls;

import java.io.IOException;
import javax.net.ssl.SSLSocket;

//import org.xlattice.Connection;
import org.xlattice.Transport;
import org.xlattice.transport.tcp.TcpConnection;

/**
 *
 * XXX This implementation does not allow TLS connections to be 
 * XXX resumed from a saved session, but this capability is very
 * XXX important.
 */
public class TlsConnection                      extends TcpConnection {

    /**
     */
//  protected TlsConnection (Transport tls, TlsAddress near, TlsAddress far) 
//                                              throws IOException {
//      super ( ((Tls)tls).getTcp(),
//              ((Tls)tls).getTcp().makeSocket(near, far));
//      
//      
//      /* STUB - need to wrap in TLS */
//  }
    protected TlsConnection (Transport tls, SSLSocket socket)
                                                throws IOException {
        super (((Tls)tls).getTcp(), socket);
        
        // WE NEED TO BE ABLE TO GET AT THE SESSION ASSOCIATED WITH
        // THE SSLSocket IN ORDER TO SAVE AND RESUME SESSIONS
    }
}
            
