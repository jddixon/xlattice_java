/* Configurer.java */
package org.xlattice.node;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.net.InetAddress;

import org.xlattice.CryptoException;
import org.xlattice.EndPoint;
import org.xlattice.NodeID;
import org.xlattice.Overlay;
import org.xlattice.corexml.CoreXmlException;
import org.xlattice.corexml.bind.*;
import org.xlattice.corexml.om.Document;
import org.xlattice.corexml.om.XmlParser;
import org.xlattice.crypto.*;
import org.xlattice.node.nodereg.Client;
import org.xlattice.transport.IPAddress;
import org.xlattice.transport.tcp.Tcp;
import org.xlattice.util.cmdline.Bindery;
import org.xlattice.util.cmdline.BooleanOpt;
import org.xlattice.util.cmdline.CmdLineOpt;

/**
 * Reads the Node's configuration file, parses it, and creates the 
 * Node.
 *
 * In its current incarnation, the configuration file is an XML
 * file following this form:
 * <pre>
 *   &lt;node&gt;
 *     &lt;nodeID&gt;... base-64 encoded value ... &lt;/nodeID&gt;
 *     &lt;rsa&gt;
 *       &lt;p&gt;PPPPPPPPPPPPP&lt/p&gt;
 *       &lt;q&gt;QQQQQQQQQQQQQ&lt/q&gt;
 *       &lt;d&gt;DDDDDDDDDDDDD&lt/d&gt;
 *       &lt;e&gt;EEEEEEEEEEEEE&lt/e&gt;
 *     &lt;/rsa&gt;
 *     &lt;overlays&gt;
 *       &lt;overlay dir="dirName" class="org.xlattice.overlays.CLASSNAME" /&gt;
 *       &lt;overlay dir="dirName" class="org.xlattice.overlays.CLASSNAME" /&gt;
 *       ...
 *    &lt;/overlays&gt;
 *  &lt;/node&gt;
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
public class Configurer {

    private NodeID    nodeID;
    private RSAKey    key;
    private Overlay[] overlays;
    private RSAKeyGen keyGen;

    private Document  doc;
    private final Tcp tcp = new Tcp();

    // VARIABLES SET FROM THE COMMAND LINE //////////////////////////
    private boolean verbose;

    /** 
     * Command line specifications, description of the command line
     * arguments.  This is used to convert command line arguments into
     * options by reflection.  
     */
    static CmdLineOpt[] options = {
                       // option  field
                       //  name    name     min      max
            new BooleanOpt ("v", "verbose"                )
    };
    // CONSTRUCTORS /////////////////////////////////////////////////
    public Configurer ()                     throws CryptoException {
        keyGen = new RSAKeyGen();       // defaults to 1024 bit keys
    }
    
    // OTHER METHODS ////////////////////////////////////////////////
    /**
     * Given xlattice.xml, the Node configuration file, set up the
     * configuration for the node, a NodeConfig object.
     * 
     */
    AbstractNode configureNode (File configFile) 
                throws CoreXmlException, CryptoException, IOException {
        // XXX VERY FRAGILE CODE -- config file may not exist, may be
        // XXX corrupt, etc
        Reader reader = new FileReader(configFile);
        boolean needNewConfigFile = false;
       
        NodeConfig nc = null;
        // SHOULD BE IN TRY
        doc =  new XmlParser (reader).read();
        nc  = (NodeConfig) map.apply(doc);
        reader.close();
        // END SHOULD-BE TRY 

        // RSA Key
        RSAInfo rsa = nc.getKey();
        if (rsa != null) {
            key = new RSAKey( rsa.getBigP(), rsa.getBigQ(), 
                              rsa.getBigD(), rsa.getBigE());
        } else {
            System.out.println("generating new key");   // DEBUG
            key = (RSAKey) keyGen.generate();
            rsa = new RSAInfo ();
            rsa.setBigP(key.getP());
            rsa.setBigQ(key.getQ());
            rsa.setBigD(key.getD());
            rsa.setBigE(key.getE());
            nc.setKey(rsa);
            needNewConfigFile = true;
        }
        // NodeID
        nodeID = nc.getNodeID();
        if (nodeID == null) {
            System.out.println("getting new NodeID");   // DEBUG
            // XXX REGISTERS AN EPHEMERAL PORT XXX
            EndPoint myEnd = new EndPoint(tcp,
                                new IPAddress(InetAddress.getLocalHost(), 0));
            nodeID = new Client(key).register(myEnd);
            nc.setNodeID(nodeID);
            needNewConfigFile = true;
        }
        // Overlays -- for the moment, we don't actually construct any
        int overlayCount = nc.sizeOverlay();

        if (verbose) {
            System.out.println(
                  "\nRSA p:    " + rsa.getP()
                + "\n    q:    " + rsa.getQ()
                + "\n    d:    " + rsa.getD()
                + "\n    e:    " + rsa.getE()
                + "\nnodeID:   " + nc.getId()
                + "\noverlays: " + overlayCount);
            for (int i = 0; i < overlayCount; i++)
                System.out.println(
                    "            " + nc.getOverlay(i).toString());
        }
        if (needNewConfigFile) {
            System.out.println("writing new configuration file");
            try {
                doc = map.generate(nc);
            } catch (CoreXmlException cxe) {
                exceptionWritingXml (cxe);
            } catch (NullPointerException npe) {
                exceptionWritingXml (npe);
            } catch (IllegalArgumentException iae) {
                exceptionWritingXml (iae);
            }
            if (doc != null) {
                // write a new configuration file
                try {
                    FileWriter writer = new FileWriter (configFile);
                    writer.write(doc.toXml());
                    writer.flush();
                    writer.close();
                } catch (IOException ioe) {
                    System.err.println(
                            "error writing node configuration file");
                }
            } 
        }
        /* STUB */
        // return new NodeImpl(key, nodeID, overlays);
        return null;
    }
    public JNode createJNode() {
        /* STUB */
        return null;
    }
    public TNode createTNode() {
        /* STUB */
        return null;
    }
    // UTILITIES ////////////////////////////////////////////////////
//  private void exceptionReadingXml(Exception e) {
//      reader = null;
//      sConfig = null;
//  }
    private void exceptionWritingXml(Exception e) {
        System.out.println("exception generating Xml file " + e);
        doc = null;
    }
    // MAIN /////////////////////////////////////////////////////////
    private static Mapping map;
    /**
     * @return a reference to the XML configuration file mapping
     */
    public static Mapping getMap()          throws CoreXmlException {
        if (map == null)
            buildMapping();
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
       
        // descriptions of individual overlays
        SubMapping overlay = new SubMapping ("overlay", 
                                "org.xlattice.node.OverlayConfig", "overlay")
                                .add(new AttrBinding("dir"))
                                .add(new AttrBinding("class")
                                        .setGetter("getClassName")
                                        .setSetter("setClassName")
                                        )
                                .repeats()
                                .optional();    // ADDED 2011-08-23
        // groups together overlay descriptions
        Collector overlays = new Collector ("overlays")
                    .add(overlay);
        
        // top element
        map = new Mapping ("node", "org.xlattice.node.NodeConfig")
                .add(new SubElBinding ("id").optional())    // the nodeID
                //.add(new SubElBinding ("nodeId").optional())    // the nodeID
                .add(subKey)                                // RSA key
                .add(overlays);
        // we're done
        map.join();
        return map;
    }
    /**
     * Main.  The only command line argument supported at this 
     * point is -v, setting the verbose option.
     *
     * XXX UNLIKELY TO EVER BE USED XXX
     */
    public static void main (String [] args)    throws Exception {
        Configurer instance;
        instance = new Configurer();
            
        // read command line, set options
        int next = Bindery.bind (args, options, instance);
       
        // read the configuration file
        boolean configOK = true;
        File cfgFile = new File("xlattice.xml");
        if (!cfgFile.exists())
            throw new IllegalStateException("missing configuration file");
        try {
            buildMapping();
        } catch (CoreXmlException e) {
            System.out.println("INTERNAL ERROR in XML binding: " + e);
            configOK = false;
        }
        if (configOK) {
            AbstractNode node = null;
            boolean runnable = false;
            try {
                node = instance.configureNode (cfgFile);
                runnable = true;
            } catch (CoreXmlException e) {
                System.out.println("error in XML configuration file " + e);
            } catch (IOException ioe) {
                System.out.println("can't read configuration file " + ioe);
            }
            if (runnable) {
                node.run();
            }
        }
    }
}
