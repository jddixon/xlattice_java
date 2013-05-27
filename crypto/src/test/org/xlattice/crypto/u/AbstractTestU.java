/* AbstractTestU.java */
package org.xlattice.crypto.u;

import java.io.File;
import java.util.Random;

import junit.framework.*;

import static org.xlattice.crypto.u.UConst.*;

/**
 * @author Jim Dixon
 */

public abstract class AbstractTestU extends TestCase {

    public final static String TEST_DIR = "tmp-u";
    
    protected U store;
    protected Counter johnny;
    protected Walker  walker;
    protected byte[]  data;

    protected Random rng = new Random();

    public AbstractTestU (String name)      throws Exception {
        super(name);
    }

    public void setup () {
        johnny = null;
        walker = null;
        try {
            if (store != null) {
                // DEBUG
                System.out.println("object 'store' existed, has been deleted");
                // END
                if (store.isOpen())
                    store.close();
                store.delete();
            }
            store = null;

            // Brute force :-(
            File tmpDir = new File(TEST_DIR);
            if (tmpDir.exists()) {
                if (tmpDir.isDirectory()) {
                    U.recursiveRemove(TEST_DIR);
                    // DEBUG
                    System.out.printf(
                        "directory '%s' existed, has been deleted\n", TEST_DIR);
                    // END
                } else { 
                    tmpDir.delete();
                    // DEBUG
                    System.out.printf(
                        "file '%s' existed, has been deleted\n", TEST_DIR);
                    // END
                }
            }
        } catch (Exception e) {
            /* ignore */
        }
    }

    // UTILITY METHODS ///////////////////////////////////////////////
    public void checkSameByteArrays(byte[] a, byte[] b) {
        int lenA = a.length;
        int lenB = b.length;
        assertEquals (lenA, lenB);
        for (int i = 0; i < lenA; i++) {
            assertEquals(a[i], b[i]);
        }
    }
    public byte[][] writeRandomFile(U store, int max) throws Exception {
        max = max >= 16 ? max : 16;
        int count = 8 + rng.nextInt(max - 8);
        byte[] data  = new byte[count];
        rng.nextBytes(data);
        byte[] key = store.putData(data);
        return new byte[][] {key, data};
    }
    // UNIT TESTS ///////////////////////////////////////////////////
    /**
     * On a Debian Linux machine the first DIR256 store consumes 
     * 1.1 MB of disk space and the code takes approximately 2s.  
     * The second store, DIR256x16, uses 18 MB of disk space and
     * appears to take no additional time - so presumably nearly
     * all of the time involved is in setting up the unit test.
     */
    public void doTestEmpty(int dirStruct)                throws Exception {
        store = U.createU(TEST_DIR, dirStruct);
        // assertTrue(store instanceof UFlat);                  // XXX
        assertEquals(TEST_DIR,   store.uDirName);
        assertEquals(dirStruct,    store.getDirStruc());      
        assertTrue(store.isOpen());

        johnny = new Counter();
        walker = new Walker(johnny, TEST_DIR);
        walker.walk();
        assertEquals(0, johnny.getCount());

        store.close();
        assertFalse(store.isOpen());
        assertTrue (store.delete());

        File testDir = new File(TEST_DIR);
        assertFalse( testDir.exists() );
    }
    public void doTestLazy(int dirStruct, int maxCount) throws Exception {
        doTestSimpleIO(dirStruct, maxCount, true);
    }
    public void doTestNotLazy(int dirStruct, int maxCount) throws Exception {
        doTestSimpleIO(dirStruct, maxCount, false);
    }
    public void doTestSimpleIO(int dirStruct, int maxCount, boolean lazy) 
                                                        throws Exception {
        if (maxCount < 32)
            maxCount = 32;
        store = U.createU(TEST_DIR, dirStruct, lazy);
        if (lazy)   assertTrue ( store.isLazy() );
        else        assertFalse( store.isLazy() );
//      assertTrue(store instanceof UFlat);           // XXX
        assertEquals(TEST_DIR,   store.uDirName);
        assertEquals(dirStruct,    store.getDirStruc());  
        assertTrue(store.isOpen());

        int fileCount = 16 + rng.nextInt(maxCount - 16);
        byte[][] keys     = new byte[fileCount][];
        byte[][] contents = new byte[fileCount][];
        for (int i = 0; i < fileCount; i++) {
            // why can't we write 
            //   {keys[i], contents[i]} = writeRandomFile(store, 1024);
            // ???
            byte[][] results = writeRandomFile(store, 1024);
            keys[i]     = results[0];
            contents[i] = results[1];
        }
        johnny = new Counter();
        walker = new Walker(johnny, TEST_DIR);
        walker.walk();
        assertEquals(fileCount, johnny.getCount());

        // read files back, verify contents are the same
        for (int i = 0; i < fileCount; i++) {
            byte[] found = store.getData(keys[i]);
            assertNotNull(found);
            checkSameByteArrays(contents[i], found);
        }
        store.close();
        assertFalse(store.isOpen());
        assertTrue (store.delete());

        File testDir = new File(TEST_DIR);
        assertFalse( testDir.exists() );
    } // GEEP
}
