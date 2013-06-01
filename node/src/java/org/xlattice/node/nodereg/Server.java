/* Server.java */
package org.xlattice.node.nodereg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.xlattice.CryptoException;
import org.xlattice.DigSigner;
import org.xlattice.SigVerifier;
import org.xlattice.corexml.CoreXmlException;
import org.xlattice.corexml.bind.*;
import org.xlattice.corexml.om.Document;
import org.xlattice.corexml.om.XmlParser;
import org.xlattice.crypto.RSAKey;
import org.xlattice.crypto.RSAKeyGen;
import org.xlattice.crypto.RSAPublicKey;
import org.xlattice.crypto.SHA1withRSAVerifier;
import org.xlattice.crypto.Key64Coder;
import org.xlattice.crypto.SHA1Digest;
import org.xlattice.node.RSAInfo;
import org.xlattice.util.Base64Coder;
import org.xlattice.util.NonBlockingLog;
import org.xlattice.util.Timestamp;

/**
 * Node registry server configuration.  At this time, the only 
 * configuration data is the server's private RSA key.
 * 
 * Provides digital key signers and verifiers using the Node registry
 * key.  If the key file does not exist, a 1024-bit RSA key is created
 * for this purpose and written to the key file.  By default this is
 * nodereg.xml in the current directory.
 *
 * In its current incarnation, the configuration file is an XML
 * file following this form:
 * <pre>
 *   &lt;nodereg&gt;
 *     &lt;rsa&gt;
 *       &lt;p&gt;PPPPPPPPPPPPP&lt/p&gt;
 *       &lt;q&gt;QQQQQQQQQQQQQ&lt/q&gt;
 *       &lt;d&gt;DDDDDDDDDDDDD&lt/d&gt;
 *       &lt;e&gt;EEEEEEEEEEEEE&lt/e&gt;
 *     &lt;/rsa&gt;
 *  &lt;/nodereg&gt;
 * </pre>
 *
 * The node ID and RSA n and d key values are base-64 encoded.  e is
 * commonly one of 3, 17, or 65537.  n (which together with e is the
 * public key) and d are typically quite large but nevertheless are
 * encoded without line breaks.  If n is a 1024 bit value, 128 bytes,
 * its base64-encoded equivalent occupies 172 bytes.
 *
 * @author Jim Dixon
 */
public class Server {

    public  static final String CONFIG_FILE = "nodereg.xml";
    private static final Server INSTANCE = new Server();
   
    private static File keyFile;
    private static RSAKey key;
    private static RSAPublicKey pubkey; 

    private static Document doc;
    private static FileReader reader;
    private static ServerConfig sConfig;

    /**
     * No-arg constructor, executed only once.
     */
    private Server () {
        try { 
            buildMapping();     // don't forget this essential step ;-)
        } catch (CoreXmlException cxe) {
            throw new IllegalStateException(
                    "INTERNAL ERROR: can't build mapping for XML file");
        }
        RSAKey _key = null;
        boolean needNewKey = true;
        
        keyFile = new File(CONFIG_FILE);
        if (keyFile.exists() && keyFile.length() > 0) {
            try { 
                reader = new FileReader (keyFile);
            } catch (FileNotFoundException fnfe) {
                reader = null;
            }
            if (reader != null) {
                try {
                    doc     = new XmlParser (reader).read();
                    sConfig = (ServerConfig) map.apply(doc);
                } catch (CoreXmlException cxe) {
                    exceptionReadingXml(cxe);
                } catch (IOException ioe) {
                    exceptionReadingXml(ioe);
                } catch (NullPointerException npe) {
                    exceptionReadingXml(npe);
                }
            } 
            if (sConfig != null) {   
                // RSA Key
                RSAInfo rsa = sConfig.getKey();
                if (rsa != null) {
                    try {
                        _key = new RSAKey( rsa.getBigP(), rsa.getBigQ(), 
                                      rsa.getBigD(), rsa.getBigE());
                    } catch (CryptoException ce) {
                        rsa  = null;
                        _key = null;
                    }
                }
            }
            needNewKey = (_key == null);
        } 
        if (needNewKey) {
            // no key file or key file corrupt
            try {
                _key = (RSAKey)new RSAKeyGen().generate();
            } catch (CryptoException ce) { 
                System.err.println("couldn't generate new key: " + ce);
                _key = null;
                // XXX DO SOMETHING
            }
            if (_key != null) {
                sConfig     = new ServerConfig();
                RSAInfo rsa = new RSAInfo ();
                rsa.setBigP(_key.getP());
                rsa.setBigQ(_key.getQ());
                rsa.setBigD(_key.getD());
                rsa.setBigE(_key.getE());
                sConfig.setKey(rsa);
                try {
                    doc = map.generate(sConfig);
                } catch (CoreXmlException cxe) {
                    exceptionWritingXml (cxe);
                } catch (NullPointerException npe) {
                    exceptionWritingXml (npe);
                } catch (IllegalArgumentException iae) {
                    exceptionWritingXml (iae);
                }
                if (doc != null) {
                    // write a new key file
                    try {
                        FileWriter writer = new FileWriter (keyFile);
                        writer.write(doc.toXml());
                        writer.flush();
                        writer.close();
                    } catch (IOException ioe) {
                        System.err.println(
                                "error writing server configuration file");
                    }
                }
            }
        }
        key    = _key;
        if (key == null) {
            System.err.println(
                    "Server constructor failed to read or generate key");
            pubkey = null;
        } else {
            pubkey = (RSAPublicKey) key.getPublicKey();
        }
    }
    // UTILITIES ////////////////////////////////////////////////////
    private static void exceptionReadingXml(Exception e) {
        reader = null;
        sConfig = null;
    }
    private static void exceptionWritingXml(Exception e) {
        System.out.println("exception generating Xml file " + e);
        doc = null;
    }
    // PROPERTIES ///////////////////////////////////////////////////
    /**
     * @return a deep copy of the public key, or null if some error
     */
    public static RSAPublicKey getPublicKey() {
        if (key == null) 
            return null;
        else
            return (RSAPublicKey) key.getPublicKey();
    }
    /**
     * Constructs a new DigSigner for this RSA key.
     *
     * @return the new signer or null if the construction failed.
     */
    public static DigSigner getSigner() {
        if (key == null) {
            // DEBUG
            System.err.println("null key, returning null signer");
            // END
            return null;
        }
        try {
            return key.getSigner("rsa");
        } catch (CryptoException ce) {
            // DEBUG
            System.err.println(
                "CryptoException getting from key, returning null signer");
            // END
            return null;
        }
    }
    /**
     * Constructs an SHA1withRSA verifier initialized with this
     * public key.
     *
     * XXX This seems to sometimes fail; see TestMsgParser, remarks at end.
     *
     * @return a verifier or null if the construction failed.
     */
    public static SigVerifier getVerifier() {
        if (key == null)
            return null;
        try {
            SigVerifier verifier = new SHA1withRSAVerifier();
            verifier.init(pubkey);
            return verifier;
        } catch (CryptoException ce) {
            return null;
        }
    }
    // XML BINDING //////////////////////////////////////////////////
    private static Mapping map;
    /**
     * @return a reference to the XML configuration file mapping
     */
    public static Mapping getMap() {
        return map;
    }
    /**
     * Construct the data binding that translates between the 
     * XML representation of the node's configuration and a set
     * of objects conveying the same information.
     */
    public static Mapping buildMapping () throws CoreXmlException {
        // mapping between XML and RSAInfo class
        SubMapping subKey = new SubMapping("rsa", 
                                "org.xlattice.node.RSAInfo", "key")
                    .add(new SubElBinding   ("p"))
                    .add(new SubElBinding   ("q"))
                    .add(new SubElBinding   ("d"))
                    .add(new SubElBinding   ("e"))
                    .optional();
       
        // top element
        map = new Mapping ("nodereg", "org.xlattice.node.nodereg.ServerConfig")
                .add(subKey);                   // RSA key
        // we're done
        map.join();
        return map;
    } 
}
