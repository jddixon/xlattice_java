/* TestNodeList.java */
package org.xlattice.corexml.om;

import junit.framework.*;

/**
 * @author Jim Dixon
 **/

public class TestNodeList extends TestCase {

    Element   elm;
    NodeList list;
    
    public TestNodeList (String name) {
        super(name);
    }

    public void setUp () {
        list = null;
    }

    public void tearDown() {
    }
 
    public void testOneNodeList() {
        elm = new Element("abc");
        list = new NodeList(elm);
        assertEquals(1, list.size());
        assertTrue (elm == (Element)list.get(0));
    }

    public void testCopyingNodeLists() {
        Element [] elms = new Element [] {
                                new Element("tom"),     new Element("dick"),
                                new Element("harry"),   new Element("joe") };
        list = new NodeList();
        NodeList list2 = new NodeList();
        for (int i = 0; i < elms.length; i++)
            list.append(elms[i]);
        assertEquals(elms.length, list.size());
        list2.moveFrom(list);
        assertEquals(0,           list.size());
        assertEquals(elms.length, list2.size());
        for (int i = 0; i < elms.length; i++)
            assertTrue (elms[i] == (Element)list2.get(i));
        list2.clear();
        assertEquals(0, list2.size());
    }
}
