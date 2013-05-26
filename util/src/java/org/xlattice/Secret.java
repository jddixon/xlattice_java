/* Secret.java */
package org.xlattice;

/**
 * An symmetric cryptographic key. 
 *
 * @author Jim Dixon
 */
public interface Secret {

    /** 
     * @return the name of the algorithm, for example, "aes" 
     */
    public String algorithm();

}
