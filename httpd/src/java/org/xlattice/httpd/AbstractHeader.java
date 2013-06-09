/* AbstractHeader.java */
package org.xlattice.httpd;

/**
 * @author Jim Dixon
 */

public interface AbstractHeader {

    /**
     * Returns the name of the tag in canonical form: first
     * character and other word-starting characters in upper
     * case, others in lower case, in the same form as RFC 2616.
     */
    public String getTag();
    
    /** 
     * The header in String format, including a terminating CR.
     */
    public String toString();

    /**
     * The header as a byte array, including the terminating CR.
     */
    public byte[] toByteArray();
}
