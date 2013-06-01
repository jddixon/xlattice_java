/* NodeInfoMgr.java */
package org.xlattice.node;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.xlattice.CryptoException;
import org.xlattice.NodeID;
import org.xlattice.corexml.CoreXmlException;
import org.xlattice.corexml.bind.Mapping;
import org.xlattice.corexml.om.Document;
import org.xlattice.corexml.om.XmlParser;
import org.xlattice.crypto.RSAKey;
import org.xlattice.crypto.RSAKeyGen;

/**
 * Manages a collection of skeletal node configuration files.  The 
 * files are by convention in ~/nodeInfo/ but for testing should be 
 * in ./test.nodeInfo/  
 *
 * The directory contains a number (possibly one) of node configuration
 * files.  These have names which are sometimes fully qualified domain
 * names, names like www.siteA.com, with ".xml" appended.  Configuration
 * names do NOT include the .xml extension.
 *
 * This class is intended for use in managing automatically generated
 * nodes, as for example in managing the keys for a number of Web 
 * sites.
 *
 * Node configurations are named.  If the name passed as parameter 
 * ends with .xml, it will be truncated.
 *
 * Configurations may be read using the get() interface and written
 * using the put() interface.  If a configuration does not exist, a
 * minimal configuration - NodeID and RSA key - will be automatically
 * generated and written to disk under the appropriate file name 
 * during the get() operation.
 *
 * This implementation is not thread-safe.
 *
 * @author Jim Dixon
 */
public class NodeInfoMgr {

    private static Compost compost;

    /** where the configuration files are located */
    private final String  infoDirName;
    private final File    infoDir;
    /** maps between XML and object representation, NodeConfig etc */
    private final Mapping map;  
   
    /** key is name, value is NodeConfig */
    private final Map configs = new HashMap();

    /**
     * If a file with the name passed (pathFromHere) does not exist,
     * create it as the directory; otherwise confirm that the existing
     * file is in fact a directory.
     *
     * @param pathFromHere absolute or relative path to the directory
     * @throws CoreXmlException - INTERNAL ERROR - if the mapping is bad
     * @throws IllegalArgumentException if the path is bad
     * @throws IllegalStateException if the file exists and is not a directory
     */
    public NodeInfoMgr (String pathFromHere) 
                throws CoreXmlException, CryptoException, IOException {
        if (pathFromHere == null || pathFromHere.equals(""))
            throw new IllegalArgumentException(
                                    "null or empty path to directory");
        if (!pathFromHere.endsWith(File.separator))
            pathFromHere += File.separator;
        infoDirName = pathFromHere;
        infoDir = new File(infoDirName);
        if (infoDir.exists()) {
            if (!infoDir.isDirectory())
                throw new IllegalStateException("not a directory");
        } else /* infoDir doesn't exist */ {
            if (!infoDir.mkdirs())
                throw new IllegalStateException("can't create " 
                    + infoDirName);
        }
        // get in touch with our little pool of randomness
        compost = Compost.getInstance();
        
        // set up binding between NodeConfig and XML configuration files
        map = Configurer.buildMapping();

        // load whatever configuration data is present
        String[] fileNames = infoDir.list();
        for (int i = 0; i < fileNames.length; i++) {
            NodeConfig nc = readInfoFile(fileNames[i]);
            if (nc != null)
                configs.put (checkConfigName(fileNames[i]), nc);
        }
    }
    // PROPERTIES ///////////////////////////////////////////////////
    public int size () {
        return configs.size();
    }
    // EXTERNAL INTERFACE ///////////////////////////////////////////
    /**
     * Get the node configuration with the given name.  If there is
     * no such name, create one.
     */
    public NodeConfig get (String name) 
                throws CoreXmlException, CryptoException, IOException {
        String baseName = checkConfigName(name);
        NodeConfig nc = (NodeConfig)configs.get(baseName);
        if (nc == null) {   // not found
            nc = buildRandomNC();
            configs.put(baseName, nc);
            // 2011-08-23 CoreXML EXCEPTION, violation of minOccur = 1yy
            put (baseName, nc);
        }
        return nc;
    }
    public void put (String name, NodeConfig nc) 
                                throws CoreXmlException, IOException {
        if (nc == null)
            throw new IllegalArgumentException ("null NodeConfig");
        String baseName = checkConfigName(name);
        configs.put(baseName, nc); 
        // 2011-08-23 CoreXML EXCEPTION, violation of minOccur = 1yy
        storeInfoFile (baseName, nc);
    }
    // LOW-LEVEL METHODS ////////////////////////////////////////////
    /** 
     * Create a random node configuration.  Uses Java's SecureRandom
     * class and the Compost heap, a collection of randomized bits.
     *
     * @throws CryptoException if a library is not installed
     * @throws IOException if there is a problem with compost heap I/O
     */
    protected static NodeConfig buildRandomNC() 
                                throws CryptoException, IOException {
        NodeConfig nc = new NodeConfig();
        // RSA Key
        RSAKeyGen keyGen = new RSAKeyGen();
        RSAKey  key = (RSAKey)keyGen.generate();
        RSAInfo rsa = new RSAInfo();
        rsa.setBigP(key.getP());
        rsa.setBigQ(key.getQ());
        rsa.setBigD(key.getD());
        rsa.setBigE(key.getE());
        nc.setKey(rsa);

        // get a node ID in exchange for what randomness there is 
        // in the RSA key
        byte[][] payment = new byte[][] {key.getP().toByteArray(),
                                         key.getQ().toByteArray()};
        nc.setNodeID ( compost.getNodeID(payment) );
        return nc;
    }
    /**
     * Validates a possible configuration name, stripping off any
     * '.xml' extension.
     *
     * @param name proposed configuration name
     * @return  a valid name
     */
    protected static String checkConfigName(String name) {
        if (name == null || name == "")
            throw new IllegalArgumentException("null or empty name");
        if (name.startsWith(File.separator))
            throw new IllegalArgumentException(
                    "name cannot be an absolute path");
        if (name.indexOf("..") != -1)
            throw new IllegalArgumentException("name cannot contain ..");
        if (name.endsWith(".xml")) {
            if (name.length() == 4)
                throw new IllegalArgumentException(
                        "name is just the .xml extension");
            return name.substring(0, name.length() - 4);
        } else
            return name;
    }
    /**
     * Convert the name passed into a relative path from the
     * current directory with a .xml extension appended.
     */
    protected String getFileName(String name) {
        String baseName = checkConfigName(name);
        return new StringBuffer(infoDirName)
                    .append(baseName).append(".xml").toString();
    }
    /**
     * Read a file assumed to be in XML format and containing node
     * configuration information.  If the file does not exist, cannot
     * be read, contains bad XML, or does not conform to the NodeConfig
     * XML-to-object binding, an exception will be thrown.  Otherwise
     * the NodeConfig object is returned.  There is no guarantee that
     * the object contains a NodeID or RSA key.
     *
     * @param name bare name of a file in the NodeInfo directory
     * @return     NodeConfig object mapped from the XML file
     * @throws CoreXmlException if there is an XML error
     * @throws IOException if the file cannot be read
     * @throws IllegalArgumentException if the name is unacceptable
     */
    protected NodeConfig readInfoFile (String name) 
                                throws CoreXmlException, IOException {
        String fullerName = getFileName(name);
        File infoFile = new File(fullerName);
        if (!infoFile.exists())
            throw new IllegalArgumentException("file does not exist: "
                    + fullerName);
        Document doc =  new XmlParser (new FileReader(infoFile)).read();
        return (NodeConfig)map.apply(doc);
    }
    /**
     * Store the node configuration passed in a file with the bare
     * name passed.  If the file already exists it is overwritten.
     *
     * @param name bare name of the file, such as www.siteA.com
     * @param nc   node configuration data, a NodeConfig
     * @throws CoreXmlException if there is an XML error
     * @throws IOException if the file cannot be written
     * @throws IllegalArgumentException if the name is unacceptable
     */
    protected void storeInfoFile (String name, NodeConfig nc) 
                                throws CoreXmlException, IOException {
        if (nc == null)
            throw new IllegalArgumentException ("null NodeConfig");
        String fullerName = getFileName(name);
        File infoFile = new File(fullerName);
        
        // 2011-08-23 CoreXML EXCEPTION, violation of minOccur = 1yy
        // DEBUG
        System.out.printf("node %s, id %s\n", name, nc.getId());
        // END
        Document doc = map.generate(nc);
        if (doc != null) {
            FileWriter writer = new FileWriter (infoFile);
            writer.write(doc.toXml());
            writer.flush();
            writer.close();
        } 
    }
}
