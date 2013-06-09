/* RndSite.java */
package org.xlattice.httpd;

import java.io.File;
import java.lang.Math;
import java.nio.ByteBuffer;
import java.util.Random;

/**
 * @author Jim Dixon
 */

import org.xlattice.CryptoException;
import org.xlattice.crypto.SHA1Digest;
import org.xlattice.crypto.builds.BuildList;
import org.xlattice.overlay.CallBack;
import org.xlattice.overlay.GetCallBack;
import org.xlattice.overlay.PutCallBack;
import org.xlattice.util.ArrayStack;
import org.xlattice.util.NonBlockingLog;

public class RndSite                        implements RndDir {

    protected NonBlockingLog debugLog;
    protected NonBlockingLog errorLog;
    protected Random rng = new Random();

//  protected final static int FIXED_LEN_FILES = 9;
    protected final static int FIXED_LEN_FILES = 4;
    protected final static int BUFSIZE   = RndSites.BUFSIZE;

    protected final static String[] EXT = new String[] {
        "dat", "htm", "html", "img", "png", "txt"
    };
    // file names are preceded by "sites/TITLE/", so we need to adjust 
    // this to allow for "sites/" and the second separator
    protected final static int SITES_OFFSET = RndSites.SITES_LEN + 1;

    public final RndSites parent;

    /** domain name such as "www.xlattice.org" */
    public final String name;
    /** above plus separator */
    public final String dirName;
    /** path to base directory for this site, including separator */
    public final String fullerDirName;
    /** base directory for this site */
    public final File   dir;

    /** maximum number of subdirectories */
    public final int M;
    /** maximum subdirectory depth */
    public final int D;
    /** file count */
    public final int N;

    public final BuildList buildList;

    // RANDOM DATA ////////////////////////////////////////
    protected byte[][]   b;
    // immediate descendents
    protected ArrayStack subDirs;
    protected ArrayStack files;
    // *all* descendents including the above and self
    protected RndDir   [] rndDirs;
    protected SiteFile [] rndFiles;

    // CONSTRUCTORS /////////////////////////////////////////////////
    /**
     * Create a Web site with the specified characteristics.  The
     * site will contain m subdirectories below the main site
     * directory.  Files in that directory are at level 1.  There is
     * always an images/ subdirectory and files in that subdirectory
     * are at level 2, so the minimum value of d, the deepest level,
     * is 2.  If d == 2, all subdirectories are contained in the
     * topmost site directory.
     *
     * @param sites the parent
     * @param s     name of this site, such as www.xlattice.org
     * @param m     number of subdirectories, at least 1
     * @param d     subdirectory depth, at least 2
     * @param n     number of files, excluding directories
     */
    public RndSite (RndSites sites, String s, int m, int d, int n)
                                                    throws Exception {
        setDebugLog("debug.log");
        setErrorLog("error.log");

        if (sites == null)
            throw new IllegalArgumentException ("null RndSites");
        parent = sites;

        if ( s == null || s.equals("") )
            throw new IllegalArgumentException ("null or empty site name");
        name = s;

        if (n < 1)
            n = 1;      // always at least one data file
        if (m < 1)
            m = 1;      // there is always an images subdirectory
        if (d < 2)
            d = 2;      // ... whose contents are at level 2

        M = m;
        D = d;
        N = n;

        // DEBUG
        DEBUG_MSG(" constructor for new site:\n    " + s
                            + ", M = " + M
                            + ", D = " + D
                            + ", N = " + N);
        // END

        // CLEAN UP NAME, CREATE SITE DIRECTORY ///////////
        StringBuffer sb = new StringBuffer("sites")
                            .append(File.separator)
                            .append(name);
        if (!name.endsWith(File.separator))
                sb.append(File.separator);
        dirName = sb.toString();
        fullerDirName = new StringBuffer(parent.baseDirName)
                        .append(dirName)
                        .toString();
        DEBUG_MSG(" constructor: fullerDirName is:\n    " + fullerDirName);
        dir     = new File(fullerDirName);
        if (dir.exists())
            throw new IllegalStateException ("site directory already exists");
        if (!dir.mkdirs())
            throw new IllegalStateException ("can't create site directory");

        // LOCAL DIRECTORIES AND FILES ////////////////////
        subDirs = new ArrayStack();
        files   = new ArrayStack();

        // SITE DIRECTORIES AND FILES /////////////////////
        DEBUG_MSG(" constructor: making directories");
        mkDirs();
        DEBUG_MSG(" constructor: making files");
        mkFiles();

        // BUILD LIST /////////////////////////////////////
        buildList = new BuildList (parent.pubKey, name);
        SHA1Digest sha1 = new SHA1Digest();
        int offset = SITES_OFFSET + name.length();
        for (int q = 0; q < N; q++) {
            String stub = rndFiles[q].fullName.substring(offset);
            byte[] hash = sha1.digest(b[q]);
            buildList.add (hash, stub);
        }
        buildList.sign(parent.masterKey);
        // System.out.println("\nBUILD LIST:\n" + buildList.toString());
    }
    // LOGGING //////////////////////////////////////////////////////
    private void checkLogName(String name) {
        if (name == null || name.equals(""))
            throw new IllegalArgumentException("null or empty log name");
    }
    public void setDebugLog (String name) {
        checkLogName(name);
        if (debugLog != null)
            throw new IllegalStateException("can't change log name");
        debugLog   = NonBlockingLog.getInstance(name);
    }
    protected void DEBUG_MSG(String msg) {
        // debugLog.message("RndSite" + msg);
        System.out.printf("RndSite DEBUG: %s\n", msg);
    }
    public void setErrorLog (String name) {
        if (errorLog != null)
            throw new IllegalStateException("can't change log name");
        checkLogName(name);
        errorLog   = NonBlockingLog.getInstance(name);
    }
    protected void ERROR_MSG(String msg) {
        errorLog.message("RndSite" + msg);
    }
    // IMPLEMENTATION ///////////////////////////////////////////////
    /**
     * Create m subdirectories reaching a depth of d if possible.  If
     * m is large enough to create more than the minimum number of
     * directories to reach a depth of d, then the extra subdirectories
     * are as evenly distributed, with K-1 or K subdirectories in each
     * parent subdirectory, where K is the largest number such that m
     * is less than or equal to
     *     ceiling ( (k ^ d)/(k - 1) ) - 1
     *
     * XXX This may no longer be quite right, due to special treatment
     * XXX of images subdirectory, but hey it's just a test driver, right?
     */
    private   int  findK (int myM, int myD) {
        // by default, put one subdirectory in each until m reached
        int myK = 1;
        if ( myM >= myD ) {
            double _k;
            double _d = myD;
            double _m = myM;
            // XXX the 100.00 is an arbitrary upper bound
            for (_k = 2.0; _k < 100.0; _k++) {
                double maxM = Math.ceil(Math.pow(_k, _d)/(_k - 1.0)) - 1.0;
                DEBUG_MSG(" maxM = " + maxM);
                if ( _m - maxM < 0.000000001 || _m <= maxM )
                    break;
            }
            myK = (int) _k;
        }
        if (myK < 1)
            myK = 1;
        DEBUG_MSG (" m = " + myM + ", d = " + myD + " ==> k = " + myK);
        return myK;
    }
    protected void mkDirs ()                    throws Exception {
//      // DEBUG
//      k = findK ( 5,  2);
//      k = findK (21, 3);
//      k = findK (85, 4);
//      k = findK (13, 3);
//      k = findK ( 7, 3);
//      // END

        final int K = findK (M, D);     // subdirectories per directory
        DEBUG_MSG(".mkDirs : subdirectories per directory, K =  " + K);
        rndDirs  = new RndDir[M + 1];   // site directory + m

        rndDirs[1] = this;
        rndDirs[0] = rndDirs[1].addDir("images");

        if (M == 1)
            return;

        int i;                  // index of allocated subdirectory
        // first we just go for depth, while allowing no images/ subdirs
        for (i = 2; i < M  && i <= D; i++) {
            rndDirs[i] = rndDirs[i - 1].addDir("dir_" + i);
        }

        // CLEAN THIS UP ! //////////////////////////////////////////
        // then widen the structure out to k subdirectories below each,
        // looping until we reach m
        int top    = 1;
        int bottom = i;
        int j;
        while (i <= M) {
            // bring each directory up to K subdirectories, while
            // creating M directories in all
            for (j = top; i <= M && j < bottom; j++) {
                int parent = j;
                int count  = rndDirs[parent].dirCount();
                int kMore  = K - count;
                for (int kk = 0; i <= M && kk < kMore ; kk++, i++) {
                    rndDirs[i] = rndDirs[parent].addDir("dir_" + i);
                    DEBUG_MSG(".mkDirs: i=" + i + ", j=" + j
                            + ", K=" + K + ", kk=" + kk
                            + ", kMore=" + kMore + ", M=" + M
                    );
                }
            }
            if (top == bottom)
                break;
            top    = bottom;
            bottom = i;
        }

        DEBUG_MSG(".mkDirs: making physical directories");
        // remember that rndDir[1] is this, rndDir[0] is images
        ((SiteDir)rndDirs[0]).mkdir(parent.baseDirName);        // images
        for (int q = 2; q <= M; q++) {
            ((SiteDir)rndDirs[q]).mkdir(parent.baseDirName);
        }

    }
    /**
     * Create N files with random extensions (for future use) and
     * random binary content.  Scatter these among the directories
     * just created.
     */
    protected void mkFiles ()                   throws Exception {
        rndFiles = new SiteFile[N];
        // create n siteFiles full of random data, keeping the byte[] for
        // each for comparison
        b = new byte[N][];
        rndFiles = new SiteFile[N];
        int j = 0;              // subdirectory index
        for (int i = 0; i < N; i++) {
            int bufLen;
            if (i < FIXED_LEN_FILES) {
                switch (i) {
//                  case 0:     bufLen =     BUFSIZE - 1; break;
//                  case 1:     bufLen =     BUFSIZE    ; break;
//                  case 2:     bufLen =     BUFSIZE + 1; break;
//                  case 3:     bufLen = 2 * BUFSIZE - 1; break;
//                  case 4:     bufLen = 2 * BUFSIZE    ; break;
//                  case 5:     bufLen = 2 * BUFSIZE + 1; break;
//                  case 6:     bufLen = 3 * BUFSIZE - 1; break;
//                  case 7:     bufLen = 3 * BUFSIZE    ; break;
//                  
                    case 0:     bufLen = 10000; break;
                    case 1:     bufLen = 11000; break;
                    case 2:     bufLen = 12000; break;
                    case 3:     bufLen = 13000; break;
                    case 4:     bufLen = 14000; break;
                    case 5:     bufLen = 15000; break;
                    case 6:     bufLen = 16000; break;
                    case 7:     bufLen = 17000; break;

                    default:    bufLen = 3 * BUFSIZE + 1; break;
                }
            } else
                bufLen = 1 + rng.nextInt(BUFSIZE);
            b[i] = new byte[bufLen];
            parent.rng.nextBytes(b[i]);
            String fname = new StringBuffer("file_").append(i).toString();
            String extension = EXT[i % EXT.length];
            rndFiles[i] = rndDirs[j].addFile(fname, extension, b[i]);

            String nameAsKey = rndFiles[i].fullName
                                          .substring(RndSites.SITES_LEN);
            parent.pathMap.put(nameAsKey, rndFiles[i]);
    
            PutBack putBack = new PutBack();    // ye olde Mocke
            parent.tmpDisk.put(rndFiles[i].fullName, b[i], putBack);
            if (putBack.status != CallBack.OK)
                throw new IllegalStateException("put call back has status "
                        + putBack.status);
            // we just round-robin through the list of directories
            j = (++j) % rndDirs.length;
        }
    }
    // INTERFACE RndDir /////////////////////////////////////////////
    /** @return name of site directory, including path and separator */
    public String dirName() {
        return dirName;
    }
    /**
     * Add a subdirectory below this one, prefixing a path.
     * @param s name of the subdirectory, excluding any path
     */
    public SiteDir  addDir  (String s)      throws Exception {
        SiteDir dir = new SiteDir ( new StringBuffer(dirName)
                                    .append(s).toString() );
        subDirs.push(dir);
        DEBUG_MSG(".addDir (" + dir.dirName() + ")");
        return dir;
    }
    /**
     * Add a data file below this directory, prefixing a path.
     * @param s name of the data file, excluding any path
     * @param b contents of the file
     */
    public SiteFile addFile (String s, String ext, byte[] b)
                                            throws Exception {
        String name = new StringBuffer(dirName)
                                            .append(s).toString();
        SiteFile file = new SiteFile (name, ext, b);
        files.push(file);
        return file;
    }
    public int dirCount () {
        return subDirs.size();
    }
    public int fileCount () {
        return files.size();
    }
}
