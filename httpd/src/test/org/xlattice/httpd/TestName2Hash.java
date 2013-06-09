/* TestName2Hash.java */
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
import org.xlattice.crypto.SHA1Digest;
import org.xlattice.crypto.builds.BuildList;
import org.xlattice.overlay.CallBack;
import org.xlattice.overlay.datakeyed.MemCache;
import org.xlattice.transport.mockery.MockConnListener;
import org.xlattice.transport.mockery.MockSchConnection;
import org.xlattice.transport.mockery.MockSocketChannel;

/**
 * Tests the operation of both Name2Hash and MemCache.  Loads
 * both from the RndSites file set, then verifies that all can
 * be retrieved.
 */
public class TestName2Hash extends RndSites {

    private Name2Hash name2Hash  = Name2Hash.getInstance();
    private MemCache  memCache   = MemCache.getInstance();
    private SHA1Digest sha1      = new SHA1Digest();

    public TestName2Hash (String name)          throws Exception {
        super(name);
    }

    public void _setUp () {
        BASE_DIR_NAME = "junk.testName2Hash";
    }
    public void _tearDown () {
    }
    public void testFull()                      throws Exception {
        name2Hash.clear();
        
        // populate Name2Hash from BuildLists /////////////
        BuildList[] buildLists = new BuildList[SITE_COUNT];
        int filesAdded = 0;
        for (int i = 0; i < SITE_COUNT; i++) {
            RndSite site = (RndSite)sites.peek(i);
            buildLists[i] = site.buildList;
            assertTrue (buildLists[i].verify());
            assertEquals (site.N, buildLists[i].size());
            name2Hash.addBuildList(buildLists[i]);
            filesAdded += site.N;
            assertEquals (filesAdded, name2Hash.size());
        }
        // populate MemCache //////////////////////////////
        int count = 0;
        for (int i = 0; i < SITE_COUNT; i++) {
            RndSite site = (RndSite)sites.peek(i);
            for (int j = 0; j < site.N; j++) {
                byte[] b = site.rndFiles[j].data;
                sha1.update(b);
                PutBack putBack = new PutBack();    // a mock
                memCache.put (new NodeID(sha1.digest()), b, putBack);
                if (putBack.status != CallBack.OK)
                   throw new IllegalStateException(
                           "call back status is " + putBack.status);
            }
            count += site.N;
        }
        assertEquals (count  , memCache.itemCount());
            
        // retrieve all files in all sites by name ////////
        for (int i = 0; i < SITE_COUNT; i++) {
            RndSite site = (RndSite)sites.peek(i);
            for (int j = 0; j < site.N; j++) {
                byte[] expected = site.rndFiles[j].data;
                // XXX skip over "sites/" in front of file name XXX
                String name = site.rndFiles[j].fullName.substring(6);
                GetBack getBack = new GetBack();    // a mock
                name2Hash.get(name, getBack);
                assertEquals(CallBack.OK, getBack.status);
                byte [] b = getBack.data;
                assertNotNull(b);
                assertEquals (expected.length, b.length);
                for (int k = 0; k < expected.length; k++)
                    assertEquals(expected[k], b[k]);
            }
        }
    } 
}
