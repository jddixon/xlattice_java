/* TestClassRefs.java */
package org.xlattice.corexml.bind;

import java.io.StringReader;

/**
 * @author Jim Dixon
 **/

import junit.framework.*;

import org.xlattice.corexml.CoreXmlException;

/**
 */
public class TestClassRefs extends ThreeLevelMapping {

    public TestClassRefs (String name) {
        super(name);
    }

    public void setUp () {
        map     = null;     seq       = null;
        attr1   = null;     emptyEl   = null;
        subEl   = null;     textG     = null;

        subMapD = null;     dSeq      = null;
        collector  = null;  
        subMapF = null;     fSeq      = null;
    }

    public void tearDown() {
    }

    public void testSimplerMapping () throws CoreXmlException {
        buildSimplerMapping();
        assertNotNull(map);
        assertNotNull(attrBag);
        assertNotNull(seq);
        assertNotNull(attr1);
        // attr2 is anonymous
        assertNotNull(emptyEl);
        assertNotNull(subEl);
        assertNotNull(textG);
        assertEquals (2, attrBag.size());   // attr1, attr2
        assertEquals (3, seq.size());       // emptyEl, subEl, textG

        assertNotNull(map.getClazz());
        assertTrue (A.class == map.getClazz());
        assertNotNull(seq.getClazz());
        assertTrue (A.class == seq.getClazz());

        assertNotNull(attr1.getClazz());
        assertTrue (A.class == attr1.getClazz());
        assertNotNull(emptyEl.getClazz());
        assertTrue (A.class == emptyEl.getClazz());
        assertNotNull(subEl.getClazz());
        assertTrue (A.class == subEl.getClazz());
        assertNotNull(textG.getClazz());
        assertTrue (A.class == textG.getClazz());
    }        
    public void testFullMapping () throws CoreXmlException {
        buildFullMapping();
        assertNotNull(subMapD);
        assertNotNull(subMapD.getClazz());
        assertTrue (A.class == subMapD.getUpClazz());  // parent
        assertTrue (D.class == subMapD.getClazz());
        
        assertNotNull(dSeq);
        assertNotNull(dSeq.getClazz());
        assertTrue (D.class == dSeq.getClazz());    // what submap maps into
        
        assertNotNull(collector);
        assertNotNull(collector.getClazz());
        assertTrue (D.class == collector.getClazz()); 
        assertEquals ("tagFs", collector.getName());

        assertNotNull(subMapF);
        assertNotNull(subMapF.getClazz());
        assertTrue (D.class == subMapF.getUpClazz());   // parent
        assertTrue (F.class == subMapF.getClazz());
        
        assertNotNull(fSeq);
        assertNotNull(fSeq.getClazz());
        assertTrue (F.class == fSeq.getClazz());    // what submap maps into
    } 
}
