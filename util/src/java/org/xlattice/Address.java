/* Address.java */
package org.xlattice;

/**
 * An Address provides enough information to identify an endpoint.  
 * The information needed depends upon the communications protocol
 * used.
 * 
 * @author Jim Dixon
 */
public interface Address {

    public boolean equals(Object o);
    public int hashCode();
    public String toString();
}
