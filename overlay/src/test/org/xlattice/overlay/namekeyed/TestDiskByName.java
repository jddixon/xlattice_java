/* TestDiskByName.java */
package org.xlattice.overlay.namekeyed;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Random;

/**
 * @author Jim Dixon
 **/

import org.xlattice.overlay.CallBack;
import org.xlattice.overlay.DelBack;
import org.xlattice.overlay.GetBack;
import org.xlattice.overlay.PutBack;

import junit.framework.*;

/**
 * Exercises the DiskByName interface.
 *
 * Better tests would create and remove files in subdirectories.
 *
 */
public class TestDiskByName extends TestCase {

    private static final int BUFSIZE = 128;

    private File   baseDir;
    private String baseDirName;

    private DiskByName dirA,  dirB;
    private byte[]     bA,    bB,    bC;
    private ByteBuffer bufA,  bufB,  bufC;      // NO LONGER
    private ByteBuffer bufD,  bufE,  bufF;      //           NEEDED
    private String     nameA, nameB, nameC;
    private File       fileA, fileB, fileC;
    
    private Random rng = new Random ( new Date().getTime() );

    public TestDiskByName (String name) {
        super(name);
    }

    public void setUp () {
        dirA  = null;    dirB  = null;
        nameA = null;    nameB = null;    nameC = null;
        /* variables used for I/O are set up in setUpABC() */
    }

    public void testBadGetInstances()           throws Exception {
        try {
            dirA = DiskByName.getInstance((String)null);
            fail("opened null directory");
        } catch (IllegalArgumentException iae) { /* success */ }
        try {
            dirA = DiskByName.getInstance("");
            fail("opened directory with empty name");
        } catch (IllegalArgumentException iae) { /* success */ }
        // XXX We might want to permit this
        try {
            dirA = DiskByName.getInstance("noSuchDirectory");
            fail("opened non-existent directory");
        } catch (IllegalArgumentException iae) { /* success */ }
    }
    public void testEquivalentGets()            throws Exception {
        baseDirName = "junk.zzz";
        baseDir = new File(baseDirName);
        if (!baseDir.exists() && !baseDir.mkdir())
            fail("can't create " + baseDirName);
        dirA = DiskByName.getInstance(baseDirName);
        dirB = DiskByName.getInstance(baseDir);
        assertTrue (dirA == dirB);
        baseDir.delete();
    }
    public void testBadFileNames()              throws Exception {
        baseDirName = "junk.zzz";
        baseDir = new File(baseDirName);
        if (!baseDir.exists() && !baseDir.mkdir())
            fail("can't create " + baseDirName);
        dirA = DiskByName.getInstance(baseDirName);

        GetBack listener = new GetBack();       // a mock
        dirA.get(null, listener);
        assertEquals(CallBack.BAD_ARGS, listener.status);
        dirA.get("", listener);
        assertEquals(CallBack.BAD_ARGS, listener.status);
        dirA.get("abc/../def", listener);
        assertEquals(CallBack.BAD_ARGS, listener.status);
        // XXX ??? 
        dirA.get(File.separator + "abc", listener);
        assertEquals(CallBack.BAD_ARGS, listener.status);

        baseDir.delete();
    }
    private void setUpABC() {
        baseDirName = "junk.abc";
        baseDir     = new File(baseDirName);
        if (baseDir.exists()) {
            if (!baseDir.isDirectory())
                baseDir.delete();
        }
        if (!baseDir.exists()) {
            if (!baseDir.mkdir())
                fail("couldn't create " + baseDirName);
        }
        dirA = DiskByName.getInstance(baseDirName);
        
        bA = new byte[BUFSIZE];
        rng.nextBytes(bA);
        bufA = ByteBuffer.allocate(BUFSIZE);
        bufA.clear();
        bufA.put(bA);
        bufA.flip();
        bB = new byte[BUFSIZE];
        rng.nextBytes(bB);
        bufB = ByteBuffer.allocate(BUFSIZE);
        bufB.clear();
        bufB.put(bB);
        bufB.flip();
        bC = new byte[BUFSIZE];
        rng.nextBytes(bC);
        bufC = ByteBuffer.allocate(BUFSIZE);
        bufC.clear();
        bufC.put(bC);
        bufC.flip();
        bufD  = bufE  = bufF  = null;
        fileA = fileB = fileC = null;
    }
    /** 
     * Deletes x and, if it is a directory, all files contained in
     * it, including subdirectories, recursively.
     *
     * XXX This should be a library function.
     */
    public static void recursingDelete(File x)        throws Exception {
        if (x.exists()) {
            if (x.isDirectory()) {
                String[] files = x.list();
                for (int i = 0; i < files.length; i++)
                    recursingDelete (new File(files[i]));
            }
            x.delete();
        }
    }
    private void tearDownABC()                  throws Exception {
        recursingDelete (baseDir);
    }
    public void testIO()                        throws Exception {
        setUpABC();
       
        GetBack getBack = new GetBack();
        PutBack putBack = new PutBack();

        nameA = "oohoo";
        dirA.put(nameA, bA, putBack);     // used to return fileA
        dirA.get(nameA, getBack);
        assertEquals(CallBack.OK, putBack.status);
        assertEquals(CallBack.OK, getBack.status);
        byte[] bD = getBack.data;
        assertEquals(BUFSIZE, bD.length);
        for (int i = 0; i < BUFSIZE; i++)
            assertEquals(bA[i], bD[i]);

        nameB = "piggy";
        dirA.put(nameB, bB, putBack);     // returned fileB
        dirA.get(nameB, getBack);
        assertEquals(CallBack.OK, putBack.status);
        assertEquals(CallBack.OK, getBack.status);
        byte[] bE = getBack.data;
        assertEquals(BUFSIZE, bE.length);
        for (int i = 0; i < BUFSIZE; i++)
            assertEquals(bB[i], bE[i]);

        nameC = "wiggy";
        dirA.put(nameC, bC, putBack);     // returned fileC
        dirA.get(nameC, getBack);
        assertEquals(CallBack.OK, putBack.status);
        assertEquals(CallBack.OK, getBack.status);
        byte[] bF = getBack.data;
        assertEquals(BUFSIZE, bF.length);
        for (int i = 0; i < BUFSIZE; i++)
            assertEquals(bC[i], bF[i]);

        tearDownABC();
    }
    public void testDelete ()                   throws Exception {
        setUpABC();
        PutBack putBack = new PutBack();
        nameA = "oohoo";
        File fileA = new File (nameA);
        dirA.put(nameA, bA, putBack);
        assertEquals(CallBack.OK, putBack.status);
        
        DelBack delBack = new DelBack();
        dirA.delete(nameA, delBack);
        assertFalse(fileA.exists());
        assertEquals(CallBack.OK, delBack.status);
        tearDownABC();
    }
}
