/* TimeoutException.java */
package org.xlattice;

/**
 * Thrown to indicate that an I/O operation was interrupted, more
 * specifically that it timed out.  The bytesTransferred field may
 * indicate how many bytes were successfully transferred before the
 * interrupt occurred.
 * 
 * An indirect subclass of java.io.IOException.
 */
public class TimeoutException 
                            extends java.io.InterruptedIOException {
    /** 
     * How many bytes were transferred by the I/O operation before 
     * the interrupt occurred.
     */
    public int bytesTransferred;

    /**
     * Constructs a TimeoutException with a null detail message.
     */
    public TimeoutException() {
        super();
    }

    /**
     * Constructs a TimeoutException with a detail message which may
     * later be retrieved by Throwable.getMessage().
     * 
     * @param msg the detail message
     */
    public TimeoutException(String msg) {
        super(msg);
    }
}
