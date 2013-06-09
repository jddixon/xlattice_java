/* NodeDir.java */
package org.xlattice.httpd;

import java.io.File;
import java.io.FileWriter;
import java.util.Random;

/**
 * @author Jim Dixon
 */

import org.xlattice.CryptoException;
import org.xlattice.NodeID;
import org.xlattice.corexml.bind.Mapping;
import org.xlattice.corexml.om.Document;
import org.xlattice.crypto.RSAKey;
import org.xlattice.crypto.RSAKeyGen;
import org.xlattice.crypto.RSAPublicKey;
import org.xlattice.crypto.SHA1Digest;
import org.xlattice.node.Configurer;
import org.xlattice.node.NodeConfig;
import org.xlattice.node.OverlayConfig;
import org.xlattice.node.RSAInfo;
import org.xlattice.util.ArrayStack;

public abstract class NodeDir {

    // this node's key; not the same as the masterKey
    protected final RSAKey       nodeKey;
    protected final RSAPublicKey nodePubKey;
    protected final RSAKeyGen    keyGen;
    protected final SHA1Digest   sha1;
    protected final NodeID       id;
    protected final NodeConfig   nc;

    // set in the subclass; must end with separator
    protected       String       nodeDirName;
    
    // CONSTRUCTOR //////////////////////////////////////////////////
    public NodeDir ()                           throws Exception {
        this(5);
    }
    public NodeDir (int overlayHint)            throws Exception {
        keyGen     = new RSAKeyGen();
        nodeKey    = (RSAKey) keyGen.generate();
        nodePubKey = (RSAPublicKey)nodeKey.getPublicKey();

        // XXX generates a NodeID from an SHA1 hash of the RSA public key
        sha1     = new SHA1Digest();;
        sha1.update(nodePubKey.getModulus().toByteArray());
        id       = new NodeID( sha1.digest() );
        
        nc       = new NodeConfig();
        nc.setNodeID(id);
        nc.setKey(new RSAInfo(nodeKey));
        // subclass must add overlay directory and class names
    }
    // UTILITY METHODS //////////////////////////////////////////////
    /**
     * A wrapper around NodeConfig.addOverlay.
     */
    protected void addOverlay(String dir, String clazzName)
                                                throws Exception {
        if (dir == null || dir.length() == 0)
            throw new IllegalArgumentException(
                    "null or empty directory name");
        if (clazzName == null || clazzName.length() == 0)
            throw new IllegalArgumentException(
                    "null or empty class name");
        OverlayConfig oCfg = new OverlayConfig();
        oCfg.setDir(dir);
        oCfg.setClassName(clazzName);
        nc.addOverlay(oCfg);
    }
    /**
     * Write a node configuration file (xlattice.xml).
     */
    protected void writeNodeConfig ()           throws Exception {
        Mapping ncMap = Configurer.getMap();
        // DEBUG
        if (ncMap == null)
            System.out.println("ncMap is null!");
        // END
        Document doc = ncMap.generate(nc);
        File configFile = new File ( new StringBuffer(nodeDirName)
                                        .append("xlattice.xml")
                                        .toString() );
        if (doc != null) {
            // write a new configuration file
            FileWriter writer = new FileWriter (configFile);
            writer.write(doc.toXml());
            writer.flush();
            writer.close();
        }
    }
}
