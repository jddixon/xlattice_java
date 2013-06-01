/* CoreXmlException.java */
package org.xlattice.corexml;

/** 
 * Superclass of all exceptions thrown by CoreXml.
 *
 * XXX NEED LINE NUMBER SUPPORT
 *
 * @author Jim Dixon
 */
public class CoreXmlException extends Exception {
    /** No-argument constructor. */
    public CoreXmlException () {
        super();
    }
    /** Constructor taking a single String argument. */
    public CoreXmlException (String msg) {
        super(msg);
    }
}
