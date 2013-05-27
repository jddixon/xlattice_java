/* BuildMaker.java */
package org.xlattice.crypto.builds;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Date; 
import java.util.HashMap; 
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.xlattice.CryptoException;
import org.xlattice.crypto.RSAKey;
import org.xlattice.crypto.RSAPublicKey;
import org.xlattice.crypto.SHA1Digest;
import org.xlattice.crypto.SignedList;
import org.xlattice.util.Base64Coder;
import org.xlattice.util.StringLib;

/** 
 * Utility for creating from a set of files (a) a BuildList and optionally 
 * (b) a directory of data-keyed files.  The BuildList contains a
 * list of file names and the associated SHA1-based content keys.
 * 
 * If created, the directory contains a snapshot, a copy, of the
 * file set in which the copy of each file is named by its content key.
 * This directory may be flat, or it may contain 256 subdirectories, or 
 * it may contain 256x16 subdirectories.  
 *
 * In the second case, each subdirectory has a name corresponding to the 
 * hex values <b>00</b> through <b>ff</b>, and a file is stored in the 
 * subdirectory corresponding to the value of the first byte of its SHA1 key.  
 *
 * In the third case, subdirectories <b>00</b> through <b>ff</b> each have
 * sub-subdirectories named <b>0</b> through <b>f</b>, and files are stored 
 * in the directory whose concatenated path name corresponds to the first 12
 * bits of their keys.  So if the hex value of the file's key begins with
 * <b>0xabcd</b>, the file will be found in the directory <b>ab/c</b>.
 *
 * @author Jim Dixon
 */
public class BuildMaker {

    /** unacceptable titles */
    public static final String[] BAD_TITLES = {
        ".",    "..",   "./",   "../"
    };
    public static final String[] BAD_FILENAMES = BAD_TITLES;

    /** 
     * Storage strategies, doubtless to be elaborated.  Compression
     * is the obvious extension.
     *
     * XXX CURRENTLY ONLY NO_DIR AND FLAT ARE SUPPORTED XXX
     */
    public static final int BAD_DIR   = -2;
    public static final int DISCOVER  = -1;
    public static final int NO_DIR    = 0;
    public static final int FLAT      = 1;
    public static final int DIR256    = 2;
    public static final int DIR256x16 = 3;

    public static final String SCRATCH_DIRECTORY 
        = new StringBuffer("tmp.testing").append(File.separator).toString();
    public static final String THIS_DIR 
        = new StringBuffer (".").append(File.separator).toString();
    private final boolean copying;

    /** paths to BuildList files from the source directory */
    private TreeSet fileNames;
    /** maps temporary file names to SHA1 digest (content key) */
    private HashMap tmpName2Digest;

    /** code for content key directory structure */
    private final int cDirStruc;
    /** code for title key directory structure */
    private final int tDirStruc;

    /** once set, no more files can be added */
    private boolean listFinished;
    /** the directory we store in */
    private final File target;
    /** the target directory's name */   
    private String targetName;
   
    /** scratch directory name */
    private final String tmpDirName = SCRATCH_DIRECTORY;
    /** scratch directory */
    private File tmpDir;

    public static final int BUFSIZE = 16384;
    private ByteBuffer buffer = ByteBuffer.allocate(BUFSIZE);

    private SHA1Digest sha1;
    private Random rng = new Random ( new Date().getTime() );

    private final RSAKey key;
    private final RSAPublicKey pubKey;
    private final String srcPath;
    private final File   srcDir;
    private final String title;
    
    // CONSTRUCTORS /////////////////////////////////////////////////
    /**
     * Get ready to make a BuildList.  If the storage strategy is 
     * NO_DIR, no key files will be generated.  Otherwise, each data
     * file will be hashed and its contents stored in a subdirectory 
     * of the target directory in a file whose name is derived from 
     * the base64-encoded content key.  A copy of the build list 
     * itself is also stored in the target directory, twice: once
     * under its content key and again under its title key, the SHA1
     * key derived from the RSA public key and the title.
     *
     * The title of the build list is a unique name for the set
     * of files.  Whoever supplies the RSA key used to create the
     * build list is responsible for ensuring the uniqueness of 
     * the name during the lifetime of the RSA key.  The title
     * may not be null or empty.  It is desirable that it contain
     * a substring that is likely to be globally unique, such as
     * a fully qualified domain name.  <b>www.xlattice.org</b> is
     * such a name.  It <b>should</b> contain a version number;
     * if appropriate, it should also contain a <b>branch</b> name.
     * A language identifier such as <b>java</b> or <b>cpp</b> might 
     * be useful in some contexts.  It <b>should not</b> contain a
     * build number.
     *
     * A more elaborate example is then 
     *   <b>org.xlattice.util-foo-0.3.6 java</b>
     * This is version <b>0.3.6</b> of the <b>foo</b> branch of the 
     * Java package <b>org.xlattice.util</b>.
     * 
     * The source directory is where the files used in the build 
     * list are located.  This expressed as a path from the current
     * location.  If a web site were being build in target/docs, for
     * example, this would be the <code>src</code> argument to the 
     * constructor.
     * 
     * The file names must be relative to that path.  This path (the
     * title) is UNIX-style, using a forward slash as a separator.  
     * The last character in the path must be a forward slash.  If it
     * is missing, it is added.
     *
     * If the storage strategy is other than NO_DIR, the BuildList is
     * automatically added to the <b>t/<b> subdirectory of the target 
     * directory under its title key, the SignedList hash.
     *
     * In this implementation if there is an existing BuildList with
     * the same title key in the target directory (set) it will be 
     * silently overwritten if and only if the timestamp on the 
     * existing BuildList is earlier than the timestamp of this 
     * BuildList.
     * 
     * In this implementation strategy title-keyed items and 
     * content-keyed items in separate directories (<b>t/</b> and 
     * <b>c</b> respectively).  
     * 
     * @param key       RSA key used to sign the list
     * @param pubKey    corresponding RSA public key
     * @param title     unique name for BuildList
     * @param src       path to source directory
     * @param files     array of names of files to be added to the BuildList
     * @param strategy  target directory structure, integer code
     * @param targ      name of target directory that data gets copied to
     */
    public BuildMaker (
            final RSAKey key,       final RSAPublicKey pubKey, 
            final String title,     final String src,     
            final String[] files,   final int strategy,   
            final String targ) 
                                throws CryptoException, IOException {
        sha1 = new SHA1Digest();
        
        // KEYS ///////////////////////////////////////////
        // Keys are in ~/.xlattice/keys/ and projectDir/.xlattice/keys/
        // The latter should normally be used.
        if (key == null || pubKey == null)
            throw new IllegalArgumentException("null key or public key");
        this.key = key;
        this.pubKey = pubKey;
       
        // TITLE //////////////////////////////////////////
        if (title == null || title.equals(""))
            throw new IllegalArgumentException("null or empty title");
        for (int i = 0; i < BAD_TITLES.length; i++)
            if (title.equals(BAD_TITLES[i]))
                throw new IllegalArgumentException("unacceptable title: "
                        + title);
        this.title = title;
        
        // SOURCE DIRECTORY ///////////////////////////////
        if (src == null || src.length() == 0) {
            srcPath = THIS_DIR;
        } else if (src.endsWith(File.separator)) {
            srcPath = src;
        } else {
            srcPath = new StringBuffer(src).append(File.separator).toString();
        }
        
        srcDir = new File(srcPath);
        if(!srcDir.exists())
            throw new IllegalArgumentException("source directory must exist");
        if(!srcDir.isDirectory())
            throw new IllegalArgumentException(
                    "source directory is not a directory!");

        // FILE NAMES /////////////////////////////////////
        fileNames = new TreeSet();
        if (files != null && files.length > 0) {
            for (int i = 0; i < files.length; i++)  {
                if (files[i] == null || files[i].equals(""))
                    throw new IllegalArgumentException("source file " 
                            + i + " name is null or empty");
                // any duplication silently ignored             
                _add (files[i]);
            }
        } 
        tmpName2Digest = new HashMap(
                    fileNames.size() > 8 ? fileNames.size() : 8);

        // STRATEGY ///////////////////////////////////////
        // XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        // XXX Replacing single variable strategy with cDirStruc
        // XXX for content key subdirectory and tDirStruc for 
        // XXX title key subdirectory, u/c and u/t respectively.
        // 
        // XXX Also allowing -1 to mean "discover or default".
        // XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        if (strategy < NO_DIR || strategy > DIR256x16)
            throw new IllegalArgumentException ("invalid strategy: " 
                    + strategy);
        cDirStruc = tDirStruc = strategy;
        copying = cDirStruc != NO_DIR;
       
        // TARGET DIRECTORY ///////////////////////////////

        // XXX NEED A RECURSIVE rmDir, BUT ONE THAT WON'T DELETE
        // XXX THE UNIVERSE.
        // Limit possible damage XXX GENERALIZE, using File.separator
        if ( tmpDirName.equals("./") || tmpDirName.startsWith("../") 
                || tmpDirName.indexOf("/../") != -1 )
            throw new IllegalStateException("unacceptable tmpDir name: "
                    + tmpDirName);

        if (!copying) {
            target = null;          // ignore any parameter supplied
        } else {
            tmpDir = new File(tmpDirName);
            if (tmpDir.exists()) {
                if (!tmpDir.delete())
                    System.out.println("couldn't delete " + tmpDirName);
            }
            tmpDir.mkdirs();

            if (targ == null || targ.length() == 0)
                throw new IllegalArgumentException(
                    "null/void directory name");
            target = new File(targ);
            if (target.exists()) {
                if (!target.isDirectory())
                    throw new IllegalArgumentException(
                        "target must be a directory");
            } else {
                target.mkdirs();
            }
            if (targ.endsWith(File.separator))
                targetName = targ;
            else
                targetName = targ + File.separator;
        }

    }
    /**
     * Create a BuildList from all of the files in and below a given
     * directory.  If the strategy is other than NO_DIR, the files are
     * copied into the target directory with names derived from their
     * content keys.
     *
     * XXX Currently any strategy other than NO_DIR is treated as though
     * XXX it were FLAT.
     *
     * @param key       RSA key used to sign the list
     * @param pubKey    corresponding RSA public key
     * @param src       path to source directory
     * @param title     unique name for BuildList
     * @param strategy  integer code for target directory structure
     * @param targ      name of target directory that data gets copied to
     */
    public BuildMaker (
            final RSAKey key,       final RSAPublicKey pubKey, 
            final String title,     final String src,     
            final int strategy,     final String targ) 
                                throws CryptoException, IOException {
        this(key, pubKey, title, src, null, strategy, targ);

        // now walk the source tree and add each file you find
        String [] fileList = srcDir.list();
        for (int i = 0; i < fileList.length; i++) {
            String name = fileList[i];
            if (name.equals(".") || name.equals(".."))
                continue;
            String pathName = srcPath + name;
            File f = new File(pathName);
            if (f.isDirectory()) 
                walkSubDir(name + File.separator);
            else
                fileNames.add(name);
        }
    }
    // UNUSED ?? ////////////////////////////////////////////////////
    public int size() {
        return fileNames.size();
    }
    // OTHER METHODS ////////////////////////////////////////////////
    /**
     * Add a file name to the set being used to create the BuildList,
     * making checks for reasonableness.
     * 
     * @param fileName file name including path relative to source directory
     */
    private final void _add(final String fileName) {
        if (listFinished)
            throw new IllegalStateException("can't add after list finished");
        if (fileName == null || fileName.equals(""))
            throw new IllegalArgumentException ("null or empty fileName");
        for (int i = 0; i < BAD_FILENAMES.length; i++)
            if (fileName.equals(BAD_FILENAMES[i]))
                    throw new IllegalArgumentException(
                        "unacceptable file name: " + fileName);
        if ( fileName.indexOf("/../") != -1) 
            throw new IllegalArgumentException(
                        "unacceptable file name: " + fileName);
                
        String fullerName 
                = new StringBuffer(srcPath).append(fileName).toString();
        File f = new File(fullerName);
        if (!f.exists())
            throw new IllegalArgumentException("fileName does not exist: "
                    + fileName);
        if (!f.isFile())
            throw new IllegalArgumentException("not a fileName: " 
                    + fileName);
        fileNames.add(fileName);
    }
    /**
     * Add a file name to the set.  The name is the path relative to
     * the source directory.  For example, if the source directory is
     * A/B and the file is A/B/C/D, then the name passed is C/D.
     *
     * @param name file name including path from source directory
     */
    public void add (String name) {
        _add (name);
    }
 
    /**
     * Read a file to determine its content hash.  If copying, 
     * write the file to the tmp directory.
     *
     * XXX If there is already a file with this hash in the
     * XXX target directory, should not overwrite it.
     *
     * @param in  data file, guaranteed to exist
     */
    protected final byte[] contentHash (File in)
                                                throws IOException {
        FileChannel inChan  = new FileInputStream(in).getChannel();
        FileChannel outChan = null;
        String tmpName = null;
        File   tmpFile = null;
        if (copying) {
            // generate a unique hex value 
            while (true) {
                tmpName = new StringBuffer (tmpDirName)
                            .append(Integer.toHexString(rng.nextInt()))
                            .toString();
                if (tmpName2Digest.get(tmpName) == null)
                    break;
            }
            tmpFile = new File(tmpName);
            outChan = new FileOutputStream(tmpFile).getChannel();
        }
        sha1.reset();
        long len   = in.length();       // DEBUG
        long total = 0;                 // DEBUG
        buffer.clear();
        for (int count = inChan.read(buffer); count >0; 
                                       count = inChan.read(buffer)) {
            total += count;
            sha1.update(buffer.array(), 0, count);
            if (copying) {
                buffer.flip();
                outChan.write(buffer);
            }
            buffer.clear();
        }
        byte[] digest = sha1.digest();
        if (copying) {
            outChan.close();    // closes tmpFile
            tmpName2Digest.put(tmpName, digest);
            File newFile = hexFileName(digest);
            if (!tmpFile.renameTo(newFile)) {
                System.err.println("couldn't rename "
                    + tmpName + " to " + newFile.getName());
            }
        }
        inChan.close();
        return digest;

    } // GEEP
    /**
     * Get the name of the target file corresponding to this source
     * file.  The target file name is the base64-encoding of the
     * 20-byte content (or signed key) hash of the source file
     * appended to the target directory.  The target directory name
     * must end in a path separation character ('/' under UNIX).
     * 
     * @param f    source file
     * @param hash content or signed hash of the source file
     */
    private File hexFileName (byte[] hash) {
        StringBuffer sb = new StringBuffer(targetName);
        if (cDirStruc != FLAT)
            throw new UnsupportedOperationException(
                    "don't know how to build file name for strategy "
                    + cDirStruc);
        return new File(sb
            .append(org.xlattice.util.StringLib.byteArrayToHex(hash))
            .toString());
    }

    /** 
     * Create the build list and optionally add copies of files
     * named in the list to a hash file directory.  If copying, 
     * also store a serialized copy of the build file in that 
     * hash copy directory.
     */
    public BuildList makeBuildList()        
                                throws CryptoException, IOException {
        // XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX 
        // If c/tDirStruc is -1, look for an existing target;
        // set c/tDirStruc to the existing type OR default: if 
        // file count is < 1024, FLAT; if < 256x16, DIR256;
        // otherwise DIR256x16.
        // XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX 
        BuildList list = new BuildList (pubKey, title);
        Iterator it = fileNames.iterator();
        while( it.hasNext() ) {
            // path to file from source directory top
            String name = (String)it.next();
            // above with path to source directory prepended
            String fName = new StringBuffer(srcPath)
                                .append(name).toString();
            File f = new File(fName); 
            byte[] hash = contentHash(f);
            list.add(hash, name);   
        }
        list.sign(key);
        listFinished = true;
        if (copying) {
            // XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
            // XX THIS SHOULD BE A LINK, AT LEAST UNDER UNIX. 
            // XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
            // put the BuildList itself into the target directory
            File file = new File(targetName 
                                + Base64Coder.encode(list.getHash()));
            // ... unless it already exists
            if (!file.exists()) {
                FileWriter f = new FileWriter (file);
                f.write (list.toString());
                f.close();
            }
        }
        return list;
    }
    /**
     * If the directory target (with name targetName) exists, 
     * return its type.  Checking is cursory.  If the directory 
     * does not exist, return NO_DIR.  If there is any obvious
     * error, return BAD_DIR.
     */
    public int quickCheckTarget()               throws IOException {
        if (!target.exists())
            return NO_DIR;

        String name = new StringBuffer(targetName)
                        .append("c/").toString();
        File subDir = new File(name);
        if (!subDir.exists())
            return BAD_DIR;
        
        name   = new StringBuffer(name).append("00/").toString();
        subDir = new File(name);
        if (subDir.exists()) {
            name = new StringBuffer(name).append("0/").toString();
            subDir = new File(name);
            if (subDir.exists())
                return DIR256x16;
            else
                return DIR256;
        } else {
            // no c/00 subdirectory
            return FLAT;
        }
    }
    public int slowCheckTarget()               throws IOException {
        if (!target.exists())
            return NO_DIR;

        /* STUB */
        return BAD_DIR;
    }
    /**
     * @param subDirName relative to source file directory, ends with 
     *                     separator 
     */
    private void walkSubDir (String subDirName) {
        File dir = new File(srcPath + subDirName);
        String [] fileList = dir.list();
        for (int i = 0; i < fileList.length; i++) {
            String name = fileList[i];
            String relName = subDirName + name;
            File f = new File(srcPath + relName);
            if (f.isDirectory()) 
                walkSubDir(relName + File.separator);
            else
                fileNames.add(relName);
        }
    }
}
