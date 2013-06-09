/* TestName2HashLite.java */
package org.xlattice.httpd;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Random;

/**
 * @author Jim Dixon
 */

import junit.framework.*;

import org.xlattice.NodeID;
import org.xlattice.crypto.RSAKey;
import org.xlattice.crypto.RSAKeyGen;
import org.xlattice.crypto.RSAPublicKey;
import org.xlattice.crypto.SHA1Digest;
import org.xlattice.crypto.builds.BuildList;
import org.xlattice.overlay.CallBack;
import org.xlattice.overlay.datakeyed.MemCache;

/**
 * Exercises the Name2Hash module without having any data on disk.
 * That is, this tests Name2Hash and MemCache without any underlying
 * disk store.  Simple data sets are generated here.
 *
 * XXX THIS IS INCOMPLETE.  THERE ARE NO TESTS OF SiteList OPS
 * XXX OR SCHEDULABLE GETS AND PUTS.
 */
public class TestName2HashLite extends TestCase {

    private final static int RUN_SIZE = 7;

    private RSAKeyGen keyGen;
    private RSAKey key;
    private RSAPublicKey pubKey;

    private SHA1Digest sha1;
    private Random     rng       = new Random(new Date().getTime());
    private Name2Hash  name2Hash = Name2Hash.getInstance();
    private MemCache   memCache  = MemCache.getInstance();

    private String[] path;
    private NodeID[] id;
    private byte[][] data;
    private byte[][] hash;

    public TestName2HashLite (String name)      throws Exception {
        super(name);
        keyGen= new RSAKeyGen();
    }

    public void setUp ()                        throws Exception {
        key = (RSAKey) keyGen.generate();
        pubKey = (RSAPublicKey) key.getPublicKey();

        sha1 = new SHA1Digest();
        path = new String [RUN_SIZE];
        id   = new NodeID [RUN_SIZE];
        data = new byte [RUN_SIZE][];
        hash = new byte [RUN_SIZE][];
    }
    public void tearDown () {
    }
    public void testEmpty()                     throws Exception {
        name2Hash.clear();
        assertEquals(0, name2Hash.size());
    }
    public void testCheckers()                  throws Exception {
        assertEquals (CallBack.BAD_ARGS, Name2Hash.checkPath("/abc"));
        assertEquals (CallBack.BAD_ARGS,
                    Name2Hash.checkPath(File.separator + "a/../bc"));
        assertEquals (CallBack.BAD_ARGS,
                    Name2Hash.checkPath(File.separator + "a..bc"));
    }
    private void makeData() {
        for (int i = 0; i < RUN_SIZE; i++) {
            path[i] = new StringBuffer("dir")
                        .append(File.separator)
                        .append("file").append((char)('A' + i))
                        .toString();
            data[i] = new byte[128];
            rng.nextBytes(data[i]);
            sha1.update(data[i]);
            hash[i] = sha1.digest();
            id  [i] = new NodeID(hash[i]);
        }
    }
    public void testLowLevelOps()               throws Exception {
        NodeID[] idFound = new NodeID[RUN_SIZE];

        // addContent(String, byte[]) /////////////////////
        name2Hash.clear();
        makeData();
        for (int k = 0; k < RUN_SIZE; k++) {
            idFound[k] = name2Hash.addContent(path[k], data[k]);
            assertNotNull(idFound[k]);
            assertTrue (id[k].equals(idFound[k]));
            // make sure it got into the MemCache
            GetBack callBack = new GetBack();        // mock
            memCache.get(id[k], callBack);
            if (callBack.status != CallBack.OK)
                throw new IllegalStateException(
                        "call back returned status " + callBack.status);
            byte[] b = callBack.data;
            assertEquals(data[k].length, b.length);
            for (int j = 0; j < b.length; j++)
                assertEquals(data[k][j], b[j]);
        }

        // addContent(String, NodeID, byte[]) /////////////
        name2Hash.clear();
        makeData();
        for (int k = 0; k < RUN_SIZE; k++) {
            name2Hash.addContent(path[k], id[k], data[k]);
            // make sure it got into the MemCache
            GetBack callBack = new GetBack();        // mock
            memCache.get(id[k], callBack);
            if (callBack.status != CallBack.OK)
                throw new IllegalStateException(
                        "callback status is " + callBack.status);
            byte[] b = callBack.data;
            assertEquals(data[k].length, b.length);
            for (int j = 0; j < b.length; j++)
                assertEquals(data[k][j], b[j]);
        }

        // addName(String, NodeID) ////////////////////////
        name2Hash.clear();
        makeData();
        for (int k = 0; k < RUN_SIZE; k++) {
            name2Hash.addName(path[k], id[k]);
            // make sure it didn't get into the MemCache
            GetBack callBack = new GetBack();        // mock
            memCache.get(id[k], callBack);
            assertNull(callBack.data);
        }
    }
    /**
     * This should exercise Name2Hash's ability to add, remove, and
     * replace BuildLists as units.
     */
    public void testBuildListOps()              throws Exception {

        // addBuildList (BuildList) ///////////////////////
        name2Hash.clear();
        makeData();

        // create a BuildList
        BuildList list = new BuildList(pubKey, "www.test.com");
        for (int i = 0; i < RUN_SIZE; i++)
            list.add(hash[i], path[i]);
        list.sign(key);
        assertEquals(RUN_SIZE, list.size());
        // add its entries to Name2Hash
        name2Hash.addBuildList(list);
        assertEquals(RUN_SIZE, name2Hash.size());
        // verify that the entries are there
        for (int i = 0; i < RUN_SIZE; i++) {
            String pathToFile = new StringBuffer()
                    .append("www.test.com")
                    .append(File.separator)
                    .append(path[i])
                    .toString();
            NodeID nodeID = name2Hash.checkName(pathToFile);
            assertNotNull (id);
            assertTrue (id[i].equals(nodeID));
            
        }
        // KEEPING THE DATA ABOVE:
        // removeBuildList (BuildList) ////////////////////
        name2Hash.removeBuildList(list);
        // verify that all of the entries are gone
        for (int i = 0; i < RUN_SIZE; i++) {
            String pathToFile = new StringBuffer()
                    .append("www.test.com")
                    .append(File.separator)
                    .append(path[i])
                    .toString();
            NodeID nodeID = name2Hash.checkName(pathToFile);
            assertNull (pathToFile + " is still mapped", nodeID);
        }
        assertEquals(0, name2Hash.size());

        // replaceBuildList (current, new) ////////////////
        // XXX STUB
    }
    public void testSiteListOps()               throws Exception {
        name2Hash.clear();
        // name2Hash.addSiteList(siteList) ////////////////
        // name2Hash.removeSiteList(siteList) /////////////
        // name2Hash.replaceSiteList(siteList) ////////////
    }
    public void testSchedOps()                  throws Exception {
        name2Hash.clear();
        // name2Hash.get (String path, SchedCnxWriter cnx)

        // name2Hash.put (String path, ByteBuffer buf, ////
        //                             SchedCnxReader cnx)
    }
}
