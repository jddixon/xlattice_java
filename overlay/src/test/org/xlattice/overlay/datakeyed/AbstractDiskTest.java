/* AbstractDiskTest.java */
package org.xlattice.overlay.datakeyed;

import java.io.File;
import java.util.Random;

/**
 * @author Jim Dixon
 **/

import junit.framework.*;

import org.xlattice.NodeID;
import org.xlattice.crypto.SHA1Digest;
import org.xlattice.overlay.CallBack;
import org.xlattice.overlay.GetBack;    // mock
import org.xlattice.overlay.PutBack;    // another mock
import org.xlattice.util.StringLib;

public abstract class AbstractDiskTest extends TestCase {

    private Random rng      = new Random ();
    private SHA1Digest sha1;

    // TEST DATA ////////////////////////////////////////////////////
    AbstractDisk disk;

    int      itemCount;
    byte[][] keyValue;
    NodeID[] id  ;
    byte[][] data;
    long     bytesWritten;

    // CONSTRUCTOR //////////////////////////////////////////////////
    public AbstractDiskTest (String name) {
        super(name);
    }

    public void setUp ()                        throws Exception {
        sha1 = new SHA1Digest();

        disk = null;
        File store = new File (AbstractDisk.PATH_TO_STORE);
        if (!store.exists()) 
            if (!store.mkdirs())
                System.err.println("could not create "
                        + AbstractDisk.PATH_TO_STORE);
        _setUp();               // sets itemCount, creates disk
        makeData();
        // DEBUG
        System.out.println("itemCount = " + itemCount);
        // END
    }
    /**
     * Must set itemCount and create an AbstractDisk, assigning
     * it to disk.
     */
    public abstract void _setUp();
    public void tearDown () {
        if (disk != null)
            disk.clear();
        File store = new File (AbstractDisk.PATH_TO_STORE);
        if (store.exists()) {
            if (!store.delete())
                System.err.println("could not delete " 
                        + AbstractDisk.PATH_TO_STORE
                        + " - probably contains files or subdirectories");
        }
    }
    private void makeData()                    throws Exception {
        keyValue = new byte   [itemCount][];
        id       = new NodeID [itemCount];
        data     = new byte   [itemCount][];
        bytesWritten = 0L;

        for (int i = 0; i < itemCount; i++) {
            // values are random-length byte arrays
            data[i]     = new byte[64 + rng.nextInt(512)];
            rng.nextBytes(data[i]);             // fill with random bits

            // keys are NodeIDs
            keyValue[i] = sha1.digest(data[i]);;
            id[i]       = new NodeID(keyValue[i]);
        }
    }
    public void testEmptyDisk()                 throws Exception {
        assertTrue ("store/ doesn't exist",  
                        new File(AbstractDisk.PATH_TO_STORE).exists() );
        assertEquals (0, disk.fileCount());
        assertEquals (0L, disk.bytesStored());
    }
    public void testWithData()                  throws Exception {
        // write it out, keyed by content hash 
        for (int i = 0; i < itemCount; i++) {
            PutBack callBack = new PutBack();
            disk.put(id[i], data[i], callBack);
            assertEquals(CallBack.OK, callBack.status);
        }
        // read it back and check
        byte[][] actual = new byte[itemCount][];
        for (int i = 0; i < itemCount; i++) {
            GetBack callBack = new GetBack();
            disk.get(id[i], callBack);
            assertEquals(CallBack.OK, callBack.status);
            actual[i] = callBack.data;
            assertEquals(data[i].length, actual[i].length);
            for (int j = 0; j < data[i].length; j++)
                assertEquals(data[i][j], actual[i][j]);
        }
    }
}
