/* Client.java */
package org.xlattice.node.nodereg;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import org.xlattice.Connector;
import org.xlattice.CryptoException;
import org.xlattice.DigSigner;
import org.xlattice.EndPoint;
import org.xlattice.NodeID;
import org.xlattice.SigVerifier;
import org.xlattice.Transport;
import org.xlattice.crypto.Key64Coder;
import org.xlattice.crypto.RSAKey;
import org.xlattice.crypto.RSAPublicKey;
import org.xlattice.transport.IPAddress;
import org.xlattice.transport.tcp.Tcp;
import org.xlattice.transport.tcp.TcpConnection;
import org.xlattice.transport.tcp.TcpConnector;
import org.xlattice.util.Base64Coder;
import org.xlattice.util.Timestamp;

/**
 * Blocking NodeReg client for an XLattice node. If the node does not
 * have a NodeID, this is used to obtain one from a node registration 
 * server.
 *
 * @author Jim Dixon
 */
public class Client {

    private final RSAKey _key;
    private final RSAPublicKey pubkey;

    private final Tcp tcp = new Tcp();

    /** Connector used to contact the node registry. */
    private final Connector ktr;
    
    // CONSTRUCTORS /////////////////////////////////////////////////
    /**
     * Create a node registry client for a node.  
     */
    public Client (RSAKey key)                  throws IOException {
        if (key == null) 
            throw new IllegalArgumentException ("key cannot be null");
        _key   = key;
        pubkey = (RSAPublicKey)_key.getPublicKey();

        InetAddress thisHost;
        try {
            thisHost = InetAddress.getLocalHost();
        } catch (java.net.UnknownHostException uhe) {
            throw new IOException ("can't get name of this host! - " 
                    + uhe);
        }
        int nodeRegPort = NodeRegSListener.NODE_REG_SERVER_PORT;
      
        // XXX NOTE THE ASSUMPTION THAT THE SERVER IS ON THIS HOST!
        try {
            ktr = (TcpConnector) ((Tcp)Tcp.class.newInstance())
                        .getConnector(
                             new IPAddress( thisHost, nodeRegPort), true);
        } catch (IllegalAccessException iae) {
            throw new IOException ("can't make connector: " + iae);
        } catch (InstantiationException ie) {
            throw new IOException ("can't make connector: " + ie);
        }
    }
    // REGISTRATION /////////////////////////////////////////////////
    /**
     * Register the Node's RSA public key and a contact EndPoint 
     * with the Node Registry server.  A node may have any number of
     * EndPoints.  This method registers the one specified with the
     * server.
     *
     * @param thisEnd EndPoint to be registered, may be null
     * @return the NodeID assigned by the server or null if unsuccessful
     */
    public NodeID register (EndPoint thisEnd)   
                                throws CryptoException, IOException {
        if (thisEnd == null)
            thisEnd = new EndPoint (tcp, 
                            new IPAddress(InetAddress.getLocalHost(), 0));
        NodeID nodeID = null;
       
        StringBuffer sb = new StringBuffer("reg\r\n")
            .append( Key64Coder.encodeRSAPublicKey (pubkey) )
            .append( "\r\n" )
            .append( new Timestamp().toString() )
            .append( "\r\n" );
        DigSigner signer = _key.getSigner("sha1");
        signer.update(sb.toString().getBytes());
        String response = sb.append( Base64Coder.encode(signer.sign()) )
                            .append( "\r\n" )
                            .toString();
       
        TcpConnection knx;
        try { 
            knx = (TcpConnection) ktr.connect(thisEnd, true);
        } catch (IOException ioe) {
            // COMPLAIN 
            return null;
        }
        Socket sock = knx.socket();
        if (sock.getTcpNoDelay()) {         // EXPECTED
            System.out.println("Nagle's algorithm is enabled");
        }
        sock.setTcpNoDelay(false);
        OutputStream outs = sock.getOutputStream();
        outs.write(response.getBytes());
        outs.flush();
        
        InputStream ins = sock.getInputStream();
        byte [] inbuf = new byte[1024];     // XXX
        int count = ins.read(inbuf);
        ins.close();
        if (count == -1) {
            // COMPLAIN
            return null;
        }

        String lines[] = (new String (inbuf, 0, count)).split("\r\n");
        if (lines.length != 4) {
            // COMPLAIN
            return null;
        }
        if (!lines[0].equals("ok"))
            return null;
        nodeID = new NodeID ( Base64Coder.decode(lines[1]) );
        //String timestamp = lines[2];
        byte[] digsig = Base64Coder.decode(lines[3]);
        // XXX SHOULD VERIFY THE DIGITAL SIGNATURE XXX
        return nodeID;
    }

    // QUERY FUNCTIONS //////////////////////////////////////////////
    /** 
     * Given a NodeID, retrieve its RSA public key from the server.
     *
     * @param nodeID Peer's NodeID
     * @return       its registered RSA public key
     */
    RSAPublicKey getKey (NodeID nodeID) {
        RSAPublicKey pubKey;
        
        // STUB STUB STUB STUB STUB
        // open connection
        // send message
        // receive reply

        pubKey = null;                      // STUB
        return pubKey;
    }
    /** 
     * Given an RSA public key, retrieve its NodeID from the server.
     *
     * @param pubKey an RSA public key
     * @return       the NodeID that registered that key
     */
    NodeID getNodeID (RSAPublicKey pubKey) {
        NodeID nodeID;
        
        // STUB STUB STUB STUB STUB
        // open connection
        // send message
        // receive reply
       
        nodeID = null;                      // STUB
        return nodeID;
    }
    /** 
     * Given the NodeID of a Peer, retrieve its registered EndPoint 
     * from the server.
     *
     * @param nodeID Peer's NodeID
     * @return       the registered EndPoint
     */
    EndPoint getEndPoint (NodeID nodeID) {
        EndPoint endPoint;
        
        // STUB STUB STUB STUB STUB
        // open connection
        // send message
        // receive reply
    
        endPoint = null;                    // STUB
        return endPoint;
    }
        
}
