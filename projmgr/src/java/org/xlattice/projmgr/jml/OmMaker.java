/* OmMaker.java */
package org.xlattice.projmgr.jml;

import org.xlattice.corexml.om.Document;

/**
 * @author Jim Dixon
 */

public abstract class OmMaker implements JmlInterpreter {

    protected final Document doc;

    // CONSTRUCTOR //////////////////////////////////////////////////
    /** 
     * @param doc a CoreXml document, assumed to be empty
     */
    public OmMaker(Document d) {
        doc = d;
    }
}
