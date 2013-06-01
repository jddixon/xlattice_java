/* MsgParser.java */
package org.xlattice.node.nodereg;

import java.net.InetAddress;
import java.nio.ByteBuffer;

/**
 * @author Jim Dixon
 */

import org.xlattice.CryptoException;
import org.xlattice.DigSigner;
import org.xlattice.SigVerifier;
import org.xlattice.crypto.RSAPublicKey;
import org.xlattice.crypto.SHA1withRSAVerifier;
import org.xlattice.crypto.Key64Coder;
import org.xlattice.crypto.SHA1Digest;
import org.xlattice.util.Base64Coder;
import org.xlattice.util.NonBlockingLog;
import org.xlattice.util.Timestamp;

/**
 * Parser for message(s) received by the node registry server.  At
 * this time the only such message is the registration message, which
 * has this form (msg 0):
 *   "reg" CRLF
 *   base64-encoded-RSA-public-key CRLF
 *   timestamp CRLF
 *   base64-encoded-digital-signature CRLF
 *
 * The encoded public key line may be folded.
 *
 * The parser first locates each of the terminating CRLFs, then
 * checks the content of each line, then sends a reply.  If the
 * request is valid, the reply takes this form (msg 1):
 *     "ok" CRLF
 *     base64-encoded node ID CRLF
 *     Timestamp.toString() CRLF
 *     base64-encoded digital signature CRLF
 *
 * If there was an error, the reply looks like (msg 2):
 *     "err" CRLF
 *     byte[] errorDescription CRLF
 *     Timestamp.toString() CRLF
 *     base64-encoded digital signature CRLF
 *
 * In each case the digital signature covers all preceding lines
 */
public class MsgParser {

    private static NonBlockingLog regLog
                        = NonBlockingLog.getInstance("nodereg.reg.log");
    private void logReg(String s) {
        regLog.message(s);
    }
    private static NonBlockingLog errLog
                        = NonBlockingLog.getInstance("nodereg.err.log");
    private void logErr(String s) {
        errLog.message(s);
    }
    // DEBUG
    private static NonBlockingLog debugLog
                        = NonBlockingLog.getInstance("junk.nodereg.");
    private void logMsg(String s) {
        debugLog.message(s);
    }
    // END
    public final static byte CR = '\r';
    public final static byte LF = '\n';
    public final static byte SP = ' ';
    public final static byte[] CRLF = new byte[] { CR, LF };
    public final static byte[] OK_CRLF   = new byte[] {
                                (byte)'o', (byte)'k', CR, LF };
    public final static byte[] ERR_CRLF  = new byte[] {
                                (byte)'e', (byte)'r', (byte)'r', CR, LF };

    private final ByteBuffer dataIn;
    private       ByteBuffer dataOut;
    private final InetAddress host;
    private final int         port;
    private final DigSigner   signer;
    /**
     * Array of offsets of first character after CRLF, ignoring any
     * line folding (CR-LF-SP sequences).
     */
    private final int[] crlfEnd = new int[4];

    private String cmd;
    private String encKey;          // DEBUG ONLY
    private RSAPublicKey pubkey;
    private String endPoint;
    private String encSig;          // DEBUG
    private byte[] digSig;

    private Timestamp now;

    public MsgParser (ByteBuffer in, ByteBuffer out,
                        InetAddress clientHost, int clientPort) {
        if (in == null || out == null || clientHost == null)
            throw new IllegalArgumentException(
                                        "null ByteBuffer or client");
        if (clientPort <= 0 || clientPort > 65535)
            throw new IllegalArgumentException("port out of range");
        dataIn  = in;
        dataOut = out;
        host    = clientHost;
        port    = clientPort;
        now     = new Timestamp();
        signer  = Server.getSigner();
        if (signer == null)
            throw new IllegalStateException("can't get server signer");
    }
    /**
     * The dataIn buffer contains a message.  Parse it and write a
     * reply into the dataOut buffer.
     *
     * @return dataOut if reply to be sent, null otherwise
     */
    ByteBuffer parse () {
        int limit = dataIn.limit();
        if (limit == 0)
            return null;
        dataOut.clear();

        byte[] buf = dataIn.array();
        boolean lastWasCR   = false;

        // search for CRLF sequences; expect four, excluding those
        // followed by spaces (CR LF SP)
        int k = 0;      // CRLF index
        for (int i = 0; i < limit; i++) {
            byte b = buf[i];
            if (b == CR) {
                lastWasCR = true;
            } else if (b == LF) {
                if (lastWasCR) {
                    if (k < 4) {
                        if ( i + 1 >= limit || buf[i + 1] != SP)
                            crlfEnd[k++] = i + 1;
                    } else {
                        return null;        // too many CRLFs
                    }
                }
                lastWasCR = false;
            } else {
                lastWasCR = false;
            }
        }
        if (k == 4) {
            // we got exactly four fields
            //                          offset      length
            cmd      = new String (buf, 0,          crlfEnd[0] - 2);
            encKey   = new String (buf, crlfEnd[0], 
                                                crlfEnd[1] - crlfEnd[0] - 2);
            endPoint = new String (buf, crlfEnd[1], 
                                                crlfEnd[2] - crlfEnd[1] - 2);
            encSig   = new String (buf, crlfEnd[2],
                                                crlfEnd[3] - crlfEnd[2] - 2);
        }
        boolean cryptoIsGood = false;
        try {
            pubkey = Key64Coder.decodeRSAPublicKey(encKey);
            digSig = Base64Coder.decode(encSig);         
            SigVerifier verifier = new SHA1withRSAVerifier();
            verifier.init(pubkey);
            verifier.update(buf, 0, crlfEnd[2]);
            cryptoIsGood = verifier.verify(digSig);
        } catch (CryptoException ce) {
            logErr("cryptoException reading client request");
        }
        if (!cryptoIsGood) {
            // STUB 
            return null;
        }
        // FORMAT A msg 1 REPLY: SUCCESS //////////////////
        //      "ok" CRLF
        //      base64-encoded node ID CRLF
        //      Timestamp.toString() CRLF
        //      base64-encoded digital signature CRLF
        byte[] binaryNow = now.toString().getBytes();
        byte[] encodedNodeID = null;
        cryptoIsGood = true;
        try {
            SHA1Digest sha1 = new SHA1Digest();
            sha1.update(pubkey.getModulus().toByteArray());
            sha1.update(binaryNow);
            encodedNodeID = Base64Coder.encode(sha1.digest()).getBytes();
        } catch (CryptoException ce) {
            logErr("cryptoException writing reply to client");
            cryptoIsGood = false;           // SHOULD USE
            return null;
        }
        dataOut.put(OK_CRLF);   // line 1
        dataOut.put(encodedNodeID);
        dataOut.put(CRLF);      // line 2
        dataOut.put(binaryNow);
        dataOut.put(CRLF);      // line 3
        // now sign it
        byte[] myEncodedSig = null;
        try {
            signer.update(dataOut.array(), 0, dataOut.position());
            myEncodedSig = Base64Coder.encode(signer.sign()).getBytes();
        } catch (CryptoException ce) {
            logErr("cryptoException signing reply to client");
            cryptoIsGood = false;           // SHOULD USE
            return null;
        }
        dataOut.put(myEncodedSig);
        dataOut.put(CRLF);
        
        return dataOut;
    }
    // PROPERTIES ///////////////////////////////////////////////////
    int [] getCrlfEnds() {
        int[] copy = (int[]) crlfEnd.clone();
        return copy;
    }
    String getCmd() {
        return cmd;
    }
    RSAPublicKey getPublicKey() {
        return pubkey;
    }
    String getEndPoint() {
        return endPoint;
    }
}
