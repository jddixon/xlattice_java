/* TestAttrList.java */
package org.xlattice.corexml.om;

import junit.framework.*;

/**
 * @author Jim Dixon
 **/

public class TestAttrList extends TestCase {

    private AttrList list;
    
    public TestAttrList (String name) {
        super(name);
    }

    public void setUp () {
        list = null;
    }

    public void tearDown() {
    }
 
    public void testOneAttrList() {
        Attr abc = new Attr("abc", "def");
        list = new AttrList(abc);
        assertEquals(1, list.size());
        assertTrue (abc == (Attr)list.get(0));
        assertEquals (" abc=\"def\"", list.toXml());
    }

    public void testOnePrefixedAttrList() {
        Attr abc = new Attr("p", "abc", "def");
        list = new AttrList(abc);
        assertEquals(1, list.size());
        assertTrue (abc == (Attr)list.get(0));
        assertEquals (" p:abc=\"def\"", list.toXml());
    }

    public void testMultiAttrList() {
        Attr abc = new Attr("abc", "123");
        Attr def = new Attr("def", "456");
        Attr ghi = new Attr("z", "ghi", "789");
        
        list = new AttrList().add(abc).add(def).add(ghi);
        assertEquals(3, list.size());
        assertTrue (abc == (Attr)list.get(0));
        assertTrue (def == (Attr)list.get(1));
        assertTrue (ghi == (Attr)list.get(2));
        assertEquals (" abc=\"123\" def=\"456\" z:ghi=\"789\"", list.toXml());
    }
}
