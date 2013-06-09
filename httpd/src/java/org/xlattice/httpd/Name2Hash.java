/* Name2Hash.java */
package org.xlattice.httpd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.xlattice.CryptoException;
import org.xlattice.NodeID;
import org.xlattice.crypto.SHA1Digest;
import org.xlattice.crypto.builds.BuildList;
import org.xlattice.overlay.CallBack;
import org.xlattice.overlay.DelCallBack;
import org.xlattice.overlay.GetCallBack;
import org.xlattice.overlay.NameKeyed;
import org.xlattice.overlay.PutCallBack;
import org.xlattice.overlay.datakeyed.MemCache;
import org.xlattice.transport.SchedCnxReader;
import org.xlattice.transport.SchedCnxWriter;
import org.xlattice.util.ArrayStack;
import org.xlattice.util.NonBlockingLog;
import org.xlattice.util.StringLib;

/**
 * Maintains data structures mapping path names to NodeIDs, which
 * are used to retrieve data from a MemCache, an in-memory cache of 
 * byte arrays.
 *
 * @author Jim Dixon
 */
public class Name2Hash                      implements NameKeyed {

    private NonBlockingLog debugLog;

    public final static int  DEFAULT_MAX_COUNT 
                                    = MemCache.DEFAULT_MAX_COUNT;
    
    private static Name2Hash _instance;
    private static String    _pathToXLattice;

    // care has to be taken to make sure that these are not used 
    // concurrently
    private final SHA1Digest sha1;
    private final Object     sha1Lock = new Object();
   
    private final Map        map;
    private final MemCache   hashCache;

    private SiteList siteList;
    private int siteCount;
    private ArrayStack siteNames;
    private ArrayStack buildLists;

    // CONSTRUCTORS AND SUCH ////////////////////////////////////////
    private Name2Hash () {
        hashCache = MemCache.getInstance(_pathToXLattice);
        map       = new HashMap (DEFAULT_MAX_COUNT);
        SHA1Digest sha1_;
        try {
            sha1_  = new SHA1Digest();
        } catch (CryptoException ce) {
            sha1_  = null;
            System.err.println("couldn't create SHA1 digest!");
        }
        sha1 = sha1_;
    
        setDebugLog("debug.log");
    }
    public static Name2Hash getInstance() {
        return getInstance("");
    }
    public static Name2Hash getInstance (String s) {
        if (_instance == null) {
            if (s == null)
                s = "";
            _pathToXLattice = s;
            _instance = new Name2Hash();
        }
        return _instance;
    }
    
    // LOGGING //////////////////////////////////////////////////////
    public final void setDebugLog (String name) {
        if (debugLog != null)
            throw new IllegalStateException("can't change debug log name");
        debugLog   = NonBlockingLog.getInstance(name);
    }
    protected void DEBUG_MSG(String msg) {
        if (debugLog != null)
            debugLog.message("Name2Hash" + msg);
    }
    // INTERFACE NameKeyed //////////////////////////////////////////
    public void delete (String path, DelCallBack callBack) {
        // STUB
    }
    /** 
     * Retrieve data by (path to) file name.  This path INCLUDES
     * the Web site directory name.
     * 
     * @param path file name as path from "sites" directory.
     * @return reference to the byte array holding the data
     */
    public void get (String path, GetCallBack callBack) {
        int status = checkPath(path);
        NodeID id  = null;
        if (status == CallBack.OK) {
            synchronized(map) {
                id = (NodeID) map.get(path);
            }
            if (id == null) 
                status = CallBack.NOT_FOUND;
        }
        if (status == CallBack.OK)
            hashCache.get(id, callBack);
        else {
            callBack.finishedGet(status, (byte[])null);
        }
    } 
    /**
     * Store data by file name.  The file name must be an absolute
     * path name containing no relative path constructions (..).
     *
     * @param path   absolute path name file of file to be written 
     * @param b      buffer holding data to be written
     */
    public void put (String path, byte[] b, PutCallBack callBack) {
        int status = checkPath(path);
        if (status == CallBack.OK && b == null)
            status = CallBack.BAD_ARGS;
        if (status != CallBack.OK) {
            callBack.finishedPut(status);
        } else {
            byte[] hash;
            synchronized (sha1Lock) {
                sha1.update(b, 0, b.length);
                hash = sha1.digest();
            }
            NodeID nodeID = new NodeID(hash);
            byte[] data = new byte[b.length];
            for (int i = 0; i < b.length; i++)
                data[i] = b[i];
            hashCache.put (nodeID, data, callBack);
        }
    } 
    // PROPERTIES ///////////////////////////////////////////////////
    /**
     * Clear both Name2Hash and the underlying MemCache.
     *
     * XXX There is a possibility of deadlock here.  Anything locking
     * XXX MemCache must *first* lock Name2Hash's map.
     */
    public void clear () {
        synchronized (map) {
            map.clear();
            hashCache.clear();          // XXX does this make sense??
        }
    }
    public String getPathToXLattice() {
        return _pathToXLattice;
    }
    public int size() {
        return map.size();
    }
    /**
     * XXX Returns references, not copies.  XXX LOCKING ???
     */
    public BuildList[] getBuildLists() {
        if (buildLists == null)
            return null;
        BuildList[] listArray = new BuildList[buildLists.size()];
        for (int i = 0; i < buildLists.size(); i++)
            listArray[i] = (BuildList)buildLists.peek(i);
        return listArray;
    }
    /**
     * XXX Returns a reference, not a copy. XXX LOCKING ???
     */
    public SiteList getSiteList() {
        return siteList;
    }
    /////////////////////////////////
    // low efficiency, easy coding //
    /////////////////////////////////

    // SITE LIST METHODS ////////////////////////////////////////////

    public void replaceSiteList (SiteList newList) {
        removeSiteList ();
        addSiteList (newList);
    }
    public void removeSiteList() {
        // STUB
    }
    public void addSiteList (SiteList newList) {
        if (siteList != null) {
            throw new IllegalStateException ("SiteList already exists");
        } else {
            siteList = newList;
            // STUB
        }
    }
    // BUILD LIST METHODS ///////////////////////////////////////////

    /**
     * Add the items in the build list to the name-to-hash mapping.
     * This does not affect the underlying MemCache.
     */
    public void addBuildList (BuildList newList)
                                                throws CryptoException {
        if (!newList.verify())
            throw new CryptoException (
                                    "build list does not verify");

        String buildDir = newList.getTitle();
        if (!buildDir.endsWith(File.separator))
            buildDir = new StringBuffer(buildDir).append(File.separator)
                        .toString();
    
        /////////////////////////////////////////////////////////////
        // STUB - NEED TO MAKE SURE THAT THIS LIST IS NOT ALREADY 
        // PRESENT, AND IT SHOULD BE STORED UNDER BOTH KEYS
        /////////////////////////////////////////////////////////////
        
        // DEBUG
        //System.out.println ("Name2Hash.addBuildList, base directory: " 
        //                                                    + buildDir);
        // END
        
        // add the items in the build list; does NOT add file contents
        for (int i = 0; i < newList.size(); i++) {
            // DEBUG
            // System.out.println ("    " + i + " " + newList.getPath(i));
            // END
            String path = new StringBuffer(buildDir)
                                .append(newList.getPath(i))
                                .toString();
                            
            addName(path, new NodeID(newList.getHash(i)) );
        }
    }
    /**
     * Remove the items in the build list from the name-to-hash mapping.
     * This does not affect the underlying MemCache.
     */
    public void removeBuildList (BuildList theList)
                                                throws CryptoException {
        if (!theList.verify())
            throw new CryptoException (
                                    "build list does not verify");

        String buildDir = theList.getTitle();
        if (!buildDir.endsWith(File.separator))
            buildDir = new StringBuffer(buildDir).append(File.separator)
                        .toString();
    
        /////////////////////////////////////////////////////////////
        // STUB - SHOULD NOT PROCEED UNLESS THE BUILD LIST IS IN USE
        /////////////////////////////////////////////////////////////
        
        // remove the items in the build list; does NOT affect file contents
        for (int i = 0; i < theList.size(); i++) {
            String path = new StringBuffer(buildDir)
                                .append(theList.getPath(i))
                                .toString();
                            
            removeName(path);
        }
    } 
    // IMPLEMENTATION METHODS ///////////////////////////////////////
    /**
     * Add a name-to-hash mapping.  The name is a file name in 
     * relative path form; that is, the name may not begin with a separator
     * and contains no ".." sequences.  The NodeID should represent
     * the key (content key or signed key) of a data file, but this
     * is not enforced here.  This operation has no effect on the
     * underlying data cache (MemCache);
     */
    protected int addName (String path, NodeID id) {
        int status = checkPath(path);
        if (status == CallBack.OK) {
            status = checkNodeID(id);
            if (status == CallBack.OK) {
                synchronized (map) {
                    map.put(path, id);
                }
            }
        }
        return status;
    }
    protected NodeID checkName (String path) {
        int status = checkPath(path);
        if (status != CallBack.OK)
            return null;
        NodeID id;
        synchronized (map) {
            id = (NodeID)map.get(path);
        }
        return id;
    }
    /**
     * Remove the name from the name-to-hash mapping, leaving the
     * underlying data cache (MemCache) unaffected.  The name should
     * take the form of a relative path to a file, but this is not
     * checked.
     * 
     * @param path an absolute path to a file
     */
    protected NodeID removeName (String path) {
        if (path == null)
            return null;
        NodeID id;
        synchronized (map) {
            id = (NodeID)map.remove(path);
        }
        return id;
    }
    // FOR TESTING ONLY /////////////////////////////////////////////
    /**
     * CONSIDER ME DEPRECATED
     *
     * @return CallBack-compatible int status codes
     */
    protected int addContent (String path, NodeID id, byte [] data) {
        LocalPutBack callBack = new LocalPutBack();
        int status = checkPath(path);
        if (status == CallBack.OK)
            status = checkByteArray(data);
        if (status == CallBack.OK) {
            hashCache.put(id, data, callBack); 
            status = callBack.status;
        }
        if (status == CallBack.OK) 
            status = addName(path, id);
        return status;
    }
    /**
     * Add a name-to-hash mapping implicitly.  Calculates the SHA1
     * hash for the byte array and adds a corresponding String-to-hash
     * mapping.  The underlying in-memory data cache (MemCache) is
     * also updated.
     *
     * @param path file name in absolute path form
     * @param data byte array, the contents of the file
     */
    protected NodeID addContent (String path, byte [] data) {
        if (checkByteArray(data) != CallBack.OK)
            return null;
        NodeID id;
        synchronized (sha1Lock) {
            sha1.update(data);
            id = new NodeID ( sha1.digest() );
        }
        if (addContent (path, id, data) == CallBack.OK)
            return id;
        else 
            return null;
    }
    // ARGUMENT CHECKERS ////////////////////////////////////////////
    protected final static int checkByteArray (byte[] b) {
        if (b == null || b.length == 0)
            return CallBack.BAD_ARGS;
        else 
            return CallBack.OK;
    }
    protected final static int checkNodeID(NodeID id) {
        if (id == null)
            return CallBack.BAD_ARGS;
        else 
            return CallBack.OK;
    }
    /**
     * Utility checking to see that path is relative (not absolute),
     * does not contain any double-dots (".."), and is not null.
     */
    protected final static int checkPath (String path) {
        if (path == null || path.equals("") || 
            path.startsWith(File.separator) || path.indexOf("..") != -1)
            return CallBack.BAD_ARGS;
        else
            return CallBack.OK;
    }
    // UTILITIES ////////////////////////////////////////////////////
    /** 
     * Read the SiteList and then the BuildLists from the node 
     * directory, using these to populate Name2Hash.
     *
     * XXX MESSY, NEEDS CLEANING UP.  MUST VERIFY THAT ALL SignedLists
     * XXX ARE SIGNED BY THE SAME KEY.
     * 
     * @return a reference to the SiteList
     * @throws IllegalStateException 
     */
    public SiteList loadFromNodeDir (String nodeDirName) {
        DEBUG_MSG(".loadFromNodeDir(" + nodeDirName + ")");
        String httpdDirName = new StringBuffer(nodeDirName)
                            .append("overlays").append(File.separator)
                            .append("httpd").append(File.separator)
                            .toString();
        String buildDir = new StringBuffer(httpdDirName)
                            .append("builds").append(File.separator)
                            .toString();
        String siteFileName = new StringBuffer(httpdDirName)
                            .append("sites.cfg")
                            .toString();
        File siteFile = new File(siteFileName);
        FileReader reader;
        try {
            reader = new FileReader (siteFile);
        } catch (FileNotFoundException fnfe) {
            throw new IllegalStateException (
                    "SiteList file missing: " + siteFileName);
        }
        try {
            siteList = new SiteList (reader);
        } catch (CryptoException ce) {
            throw new IllegalStateException(
                    "exception interpreting SiteList: " + ce);
        } catch (IOException ioe) {
            throw new IllegalStateException (
                    "exception reading SiteList: " + ioe);
        }
        try {
            if (!siteList.verify())
                throw new IllegalStateException("SiteList does not verify");
        } catch (CryptoException ce) {
            throw new IllegalStateException(
                    "exception verifying SiteList: " + ce);
        }
            
        siteCount  = siteList.size();
        buildLists = new ArrayStack(siteCount);
        siteNames  = new ArrayStack(siteCount);
        for (int i = 0; i < siteCount; i++) {
            String siteName      = siteList.toString(i);
            siteNames.push(siteName);
            String buildFileName = new StringBuffer(buildDir)
                                    .append(siteName)
                                    .append(".cfg")
                                    .toString();
            File buildFile = new File(buildFileName);
            if(!buildFile.exists())
                throw new IllegalStateException ("missing build file: "
                        + buildFileName);
            BuildList buildList;
            try {
                reader = new FileReader(buildFile);
                buildList = new BuildList(reader);
                buildLists.push(buildList);
                if (!buildList.verify())
                    throw new CryptoException("verification failure");
            } catch (FileNotFoundException fnfe) {
                throw new IllegalStateException (
                        "can't find " + buildFileName);
            } catch (IOException ioe) {
                throw new IllegalStateException (
                        "exception reading " + buildFileName
                        + " - " + ioe);
            } catch (CryptoException ce) {
                throw new IllegalStateException (
                        "BuildList " + buildFileName + " corrupt? -"
                        + ce);
            }
            if (!siteName.equals(buildList.getTitle())) 
                throw new IllegalStateException (
                        "BuildList title (" + buildList.getTitle() 
                        + " does not match site name " + siteName);
        }
        // populate Name2Hash from BuildLists /////////////
        for (int i = 0; i < siteCount; i++) {
            BuildList buildList = (BuildList)buildLists.peek(i);
            try {
                addBuildList(buildList);
            } catch (CryptoException ce) {
                throw new IllegalStateException (
                    "build list " + buildList.getTitle()
                    + " corrupt? - " + ce);
            }
        }
        return siteList;
    }
    /**
     * Load Name2Hash and the underlying MemCache (datakeyed data cache)
     * from files found in the directories below sitesDir using 
     * configuration data (SiteList and BuildLists) found in nodeDir.
     * 
     * All exceptions generated have been reduced to IllegalStateExceptions.
     * 
     * Invoking this method will reinitialize Node2Hash and MemCache.
     * All data will be lost.
     *
     * XXX NEED TO THINK ABOUT MULTI-THREADING
     *
     * @param nodeDirName  name of the node configuration directory
     * @param sitesDirName path to the directory containing site directories
     * @throws IllegalStateException 
     */
    private class LocalPutBack implements PutCallBack {
        int status;
        public void finishedPut (int code) {
            status = code;
        }
        public int getStatus () {
            return status;
        }
    }
    public void loadFromSiteDirs (
                            String nodeDirName, String sitesDirName) {
        this.clear();       // locks
        loadFromNodeDir(nodeDirName);   
        LocalPutBack callBack = new LocalPutBack();

        // populate MemCache //////////////////////////////
        for (int i = 0; i < siteCount; i++) {
            String siteName = (String)siteNames.peek(i);
            String siteDirName = new StringBuffer(sitesDirName)
                                    .append(siteName)
                                    .append(File.separator)
                                    .toString();
            BuildList buildList = (BuildList)buildLists.peek(i);
            for (int j = 0; j < buildList.size(); j++) {
                String listPath = buildList.getPath(j);
                String fName = new StringBuffer (siteDirName)
                                .append(listPath)
                                .toString();
                String pathForFetch = new StringBuffer(siteName)
                                .append(File.separator)
                                .append(listPath)
                                .toString();
                // DEBUG
                // System.out.println("pathForFetch: " + pathForFetch);
                // END
                File dataFile = new File(fName);
                long len = dataFile.length();
                if (len > Integer.MAX_VALUE) {
                    DEBUG_MSG(".loadFromSiteDirs: file too long: " 
                                                    + len + " bytes");
                    continue;
                }
                byte[] b = new byte[ (int)len ];
               
                try {
                    FileInputStream ins = new FileInputStream(dataFile);
                    ins.read(b);
                } catch (FileNotFoundException fnfe) {
                    DEBUG_MSG (".loadFromSiteDirs: can't find data file " 
                            + fName);
                    continue;
                } catch (IOException ioe) { 
                    DEBUG_MSG(".loadFromSiteDirs: can't read data file " 
                            + fName + " - " + ioe);
                    continue;
                }
                sha1.update(b);
                hashCache.put (new NodeID(sha1.digest()), b, callBack);
                int retCode = callBack.status;
                if (retCode != CallBack.OK) 
                    DEBUG_MSG(".loadFromSiteDirs: return code is " 
                            + CallBack.STATUS_CODES[retCode]);
            }
        }
    } 

    /**
     * Given an existing node directory, load Name2Hash from the
     * SiteList and BuildLists found there, and then load
     * MemCache from the content-keyed data in store/
     */
    public void loadFromStore (String nodeDirName) {
        this.clear();       // locks
        loadFromNodeDir(nodeDirName);   
    
        // run through the BuildLists, loading each entry from store/
        // STUB
        
        // run through the BuildLists again, this time fetching each
        // entry from Name2Hash by name and then verifying data against hash
        // STUB
        
    }
}
