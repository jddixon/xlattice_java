/* CallBack.java */
package org.xlattice.overlay;

/**
 * @author Jim Dixon
 **/

public interface CallBack { 

    /** reports success */
    public static final int OK              = 0;
    /** already present; not usually an error status */
    public static final int EXISTS          = 1;
    /** error found checking parameter list */
    public static final int BAD_ARGS        = 2; 
    /** IOException occurred */
    public static final int IO_EXCEPTION    = 3;
    /** operation not successful because item is a directory */
    public static final int IS_DIRECTORY    = 4;
    /** item not found */
    public static final int NOT_FOUND       = 5;
    /** operation is not implemented */
    public static final int NOT_IMPLEMENTED = 6;
    /** operation failed because item is too large */
    public static final int TOO_BIG         = 7;
    /** crypto verification fails */
    public static final int VERIFY_FAILS    = 8;
    
    public static String[] STATUS_CODES = {
        "OK",           "EXISTS",    "BAD_ARGS",        "IO_EXCEPTION",
        "IS_DIRECTORY", "NOT_FOUND", "NOT_IMPLEMENTED", "TOO_BIG",
        "VERIFY_FAILS"
    };  

    public int getStatus();
}
