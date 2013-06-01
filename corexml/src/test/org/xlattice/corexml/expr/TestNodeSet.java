/* TestNodeSet.java */
package org.xlattice.corexml.expr;

import junit.framework.*;
import org.xlattice.corexml.CoreXmlTestCase;
import org.xlattice.corexml.om.*;

/**
 * @author Jim Dixon
 **/

public class TestNodeSet extends CoreXmlTestCase {

    Element elm;
    NodeSet nodes;
    
    public TestNodeSet (String name) {
        super(name);
    }

    public void setUp () {
        nodes = null;
    }

    public void tearDown() {
    }
 
    public void testSingleton() {
        elm = new Element("abc");
        nodes = new NodeSet();
        assertNotNull(nodes);
        assertTrue(nodes.isEmpty());
        assertEquals(0, nodes.size());
        nodes.add(elm);
        assertFalse(nodes.isEmpty());
        assertEquals(1, nodes.size());
        // false because not present
        assertFalse(nodes.remove(new Element("def")));
        assertEquals(1, nodes.size());
        assertTrue(nodes.remove(elm));
        assertEquals(0, nodes.size());
        assertTrue(nodes.isEmpty());
        try {
            nodes.moveFrom(null);
            fail("expected NPE!");
        } catch (NullPointerException npe) { /* success */ }
    }

    public void testCopyingNodeSets() {
        Element [] elms = new Element [] {
                                new Element("tom"),     new Element("dick"),
                                new Element("harry"),   new Element("joe") };
        nodes = new NodeSet();
        NodeSet nodes2 = new NodeSet();
        for (int i = 0; i < elms.length; i++)
            nodes.add(elms[i]);
        assertEquals(elms.length, nodes.size());
        nodes2.moveFrom(nodes);
        assertEquals(0,           nodes.size());
        assertEquals(elms.length, nodes2.size());
        for (int i = 0; i < elms.length; i++)
            assertTrue (nodes2.contains(elms[i]));
        nodes2.clear();
        assertEquals(0, nodes2.size());
    }
    // TEST EQUALS() ////////////////////////////////////////////////
    public void testEquals() {
        Element [] elms = new Element [] {
                                new Element("tom"),     new Element("dick"),
                                new Element("harry"),   new Element("joe") };
        nodes = new NodeSet();
        assertFalse(nodes.equals(null));
        assertTrue (nodes.equals(nodes));
        assertTrue (nodes.equals(NodeSet.EMPTY));
        
        NodeSet nodes2 = new NodeSet();
        assertTrue (nodes2.equals(NodeSet.EMPTY));
        assertTrue (nodes.equals(nodes2));
        for (int i = 0; i < elms.length; i++) 
            nodes.add(elms[i]);
        assertFalse(nodes.equals(nodes2));
        for (int i = 0; i < elms.length; i++) 
            nodes2.add(elms[i]);
        assertEquals(elms.length, nodes.size());
        assertTrue(nodes.equals(nodes2));
    }
    // TEST UNION AND DIFFERENCE OPERATIONS /////////////////////////
    public void testUnionAndDiff() {
        Element [] elms = new Element [] {
                          new Element("tom"),     new Element("dick"),
                          new Element("harry"),   new Element("joe"), 
                          new Element("fred"),    new Element("burt")
        };
        nodes = new NodeSet();
        for (int i = 0; i < 3; i++) 
            nodes.add(elms[i]);

        NodeSet nodes2 = new NodeSet();
        for (int i = 3; i < elms.length; i++) 
            nodes2.add(elms[i]);

        NodeSet nodes3 = new NodeSet();
        for (int i = 0; i < elms.length; i++)
            nodes3.add(elms[i]);
        
        NodeSet copy = new NodeSet(nodes);
        assertTrue (copy.equals(nodes));
        copy.subtract(nodes2);       // no elements in common
        assertTrue (copy.equals(nodes));
        copy.subtract(nodes);        // identical so ...
        assertTrue (copy.equals(NodeSet.EMPTY));

        copy.add(nodes2);
        assertTrue (copy.equals(nodes2));
        copy.add(nodes);
        assertTrue (copy.equals(nodes3));
        
    }
}
