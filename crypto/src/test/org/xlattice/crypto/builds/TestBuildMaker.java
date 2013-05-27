/* TestBuildMaker.java */
package org.xlattice.crypto.builds;

import junit.framework.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.HashSet;

import org.xlattice.CryptoException;
import org.xlattice.crypto.RSAKey;
import org.xlattice.crypto.RSAPublicKey;
import org.xlattice.crypto.RSAKeyGen;
import org.xlattice.crypto.SHA1Digest;
import org.xlattice.util.Base64Coder;
import org.xlattice.util.StringLib;

/**
 * @author Jim Dixon
 */

public class TestBuildMaker extends TestCase {

    private BuildMaker maker;
    private BuildList list;
    private RSAKeyGen keyGen;
    private RSAKey key;
    private RSAPublicKey pubKey;

    private SHA1Digest sha1 = new SHA1Digest();

    private final String TEST_TITLE 
                    = "org.xlattice.crypto.builds.TestBuildMaker v1.0 java";
    private final String SRC_DIR = "./";

    private final String[] fileNames = new String[] {
        "build.sh",     
        "build.xml",        
        "classpath.sh",
        "project.xml"
    };
    
    public TestBuildMaker (String name)     throws CryptoException {
        super(name);
        keyGen= new RSAKeyGen();
    }

    public void setUp () {
        maker  = null;
        list   = null;
        key    = null;
        pubKey = null;
    }
    private void _keysAndFiles()                throws Exception {
        key = (RSAKey) keyGen.generate();
        pubKey = (RSAPublicKey) key.getPublicKey();
    }
    public void testEmpty()                     throws Exception {
        _keysAndFiles();
        maker = new BuildMaker(key, pubKey, TEST_TITLE,   SRC_DIR, 
                    (String[])null, BuildMaker.NO_DIR, null);
        assertNotNull(maker);
        assertEquals(0, maker.size());
        try {
            maker = new BuildMaker(key, pubKey, TEST_TITLE, SRC_DIR,
                    null, BuildMaker.NO_DIR - 1, null);
            fail("didn't catch negative strategy");
        } catch (IllegalArgumentException iae) { /* success */ }
        try {
            maker = new BuildMaker(key, pubKey, TEST_TITLE, SRC_DIR,
                    null, BuildMaker.DIR256x16 + 1, null);
            fail("didn't catch out of range strategy");
        } catch (IllegalArgumentException iae) { /* success */ }
        list = maker.makeBuildList();
        assertNotNull(list);
        assertEquals (0, list.size());
        assertTrue (list.isSigned());
        assertTrue (list.verify());
    }
    public void testNoDiskStorage()             throws Exception {
        _keysAndFiles();
        maker = new BuildMaker(key, pubKey, TEST_TITLE, SRC_DIR,
                                fileNames, BuildMaker.NO_DIR, "foo");
        assertEquals (fileNames.length, maker.size());
        try {
            maker.add("non-existent-file");
            fail("added non-existent file");
        } catch (IllegalArgumentException iae) { /* success */ }
        File junkDir = new File("junk.dir");
        junkDir.mkdir();
        try {
            maker.add("junkDir");
            fail("added directory!");
        } catch (IllegalArgumentException iae) { /* success */ }
        if (!junkDir.delete())
            fail("couldn't delete " + junkDir.getName());
        assertEquals (fileNames.length, maker.size());
        list = maker.makeBuildList();
        assertTrue (list.isSigned());
        assertEquals(fileNames.length, list.size());
        assertTrue (list.verify());
    }
    
    private final static String MY_SCRATCH_DIR = "junk.hashes/";
    private void cleanUpDisk()                  throws Exception {
        // XXX LEAVES MY_SCRATCH_DIR ON THE DISK
        new File(BuildMaker.SCRATCH_DIRECTORY).delete();
    }
    
    /**
     * Return the content hash for a file.
     */
    byte[] getFileHash(File f)                  throws Exception {
        int len = (int) f.length(); 
        byte[] buf = new byte[len];
        FileInputStream ins = new FileInputStream(f);
        int count = ins.read(buf);
        assertEquals (len, count);
        sha1.reset();
        return sha1.digest(buf);
    }
    /**
     * Confirm that the build list is correct: that the hash on 
     * each content line corresponds to a file in the hash
     * directory, that that file's content hash matches its name,
     * and that the content hash of the source file is the same.
     *
     * Deletes each hash file after checking it.
     * 
     * @param hashDir  name of the FLAT hash file directory 
     * @param hash     the build list's signed-key hash
     * @param removing whether to remove files once found
     */
    void readAndCheckBuildList(
            String pathToSrc, String title, // added 05-12-01
            String hashDir, 
            byte[] hash, 
            boolean removing)
                                                throws Exception {
        StringBuffer sbPath = new StringBuffer();
        if (pathToSrc == null || pathToSrc.equals("")) {
            sbPath.append("./");
        } else {
            sbPath.append(pathToSrc);
            if (pathToSrc.charAt(pathToSrc.length() - 1) 
                                                 != File.separatorChar)
                sbPath.append(File.separator);
        }
        if (title != null && !title.equals("./")) {
            sbPath.append(title);
            if (title.charAt(title.length() - 1) != File.separatorChar)
                sbPath.append(File.separator);
        }
        pathToSrc = sbPath.toString();
 
        // retrieve BuildList from hash directory (FLAT) 
        File listFile = new File(hashDir  
                                + Base64Coder.encode(hash));
        assertTrue(listFile.exists());
        BuildList listFromDisk = new BuildList (new FileReader(listFile));
        HashSet removed = null;
        if (removing)
            removed = new HashSet (listFromDisk.size());

//      // DEBUG
//      System.out.println(
//              "#####################################################"
//         +  "\nBUILD LIST"
//         +  "\n#####################################################\n"
//         +     listFromDisk.toString()
//         +    "#####################################################");
//      // END
        for (int i = 0; i < listFromDisk.size(); i++) {
            // build list content line
            String s = listFromDisk.toString(i);
            int spaceAt = s.indexOf(' ');
            assertTrue ("can't find space in build list line", spaceAt > 0);
            String hashText = s.substring(0, spaceAt);
            byte[] fHash     = Base64Coder.decode(hashText);
            assertEquals(20, fHash.length);
            
            // file in hash directory
            String name     = s.substring(spaceAt + 1);
            String pathName = hashDir + StringLib.byteArrayToHex(fHash);
            File f = new File(pathName);
            if (!f.exists()) {
                if (removing && removed.contains(hashText))
                    continue;

                // DEBUG
                System.out.println(
                        i + ": " + pathName + " does not exist");
                continue;
                // END
                
                // fail(pathName + " does not exist");
            }
            byte[] actualHash = getFileHash(f);
            
            // source file and its content hash
            File src = new File(pathToSrc + name);
            // DEBUG
            if (!src.exists())
                System.out.println( pathToSrc + name + " doesn't exist" );
            // END
            assertTrue (src.exists());
            byte[] srcHash = getFileHash(src);
            
            for (int k = 0; k < 20; k++) {
                assertEquals(srcHash[k], actualHash[k]);
                assertEquals(srcHash[k], fHash[k]);
            }
            if (removing) {
                // DEBUG
                //System.out.println("removing " + hashText);
                // END
                if (!removed.add(hashText))
                    System.out.println("couldn't add " + hashText
                            + " to removed");
                f.delete();         // tidy up as we go along
            }
        }
        listFile.delete();
        new File(hashDir).delete();

    }
    public void testWithDiskStorage()           throws Exception {
        _keysAndFiles();
        maker = new BuildMaker(key, pubKey, TEST_TITLE, SRC_DIR,
                            fileNames, BuildMaker.FLAT, MY_SCRATCH_DIR);
        assertEquals (fileNames.length, maker.size());
        list = maker.makeBuildList();
        assertTrue (list.isSigned());
        assertEquals(fileNames.length, list.size());
        assertTrue (list.verify());

        readAndCheckBuildList(
                SRC_DIR, SRC_DIR,       // added 2005-12-01
                MY_SCRATCH_DIR, list.getHash(), true);

        cleanUpDisk();
    }
    // XXX This test used a directory which only exists on Westbury by
    // XXX chance and so the test is dropped.
    /*
    public void testTheBigOne()                 throws Exception {
        _keysAndFiles();
        maker = new BuildMaker(key, pubKey, TEST_TITLE, "../target/docs/",
                        BuildMaker.FLAT, MY_SCRATCH_DIR);
        list = maker.makeBuildList();
        assertTrue (list.isSigned());
        assertTrue (list.verify());
        // DEBUG
        // System.out.println("Big One file count: " + list.size());
        // END
        readAndCheckBuildList(
                "../target/docs/", null,   // added 05-12-01
                MY_SCRATCH_DIR, list.getHash(), true);
        cleanUpDisk();
    }  
    */
}
