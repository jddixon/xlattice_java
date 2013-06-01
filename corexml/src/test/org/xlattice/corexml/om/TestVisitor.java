/* TestVisitor.java */
package org.xlattice.corexml.om;

import junit.framework.*;

/**
 * Exercises the OM walker-visitor code, primarily using Node.NodeCounter.
 * XXX SHOULD THIS BE Holder.NodeCounter??
 *
 * @author Jim Dixon
 */
public class TestVisitor extends org.xlattice.corexml.CoreXmlTestCase {

    Document doc;
    Node.NodeCounter counter;
    NodeList docNodes;      // children of the Document
    Element root;
    NodeList rootNodes;     // children of the root node
    AttrList rootAList;

    public TestVisitor (String name) {
        super(name);
    }

    public void setUp () {
        doc       = new Document();
        counter   = doc.new NodeCounter();
        docNodes  = doc.getNodeList();
        root      = null;
        rootNodes = null;
        rootAList = null;
    }

    public void tearDown() {
    }

    void checkCounts (Node.NodeCounter counter,
            int expectedAttr, int expectedComment,
            int expectedDoc, int expectedDocType, int expectedElement,
            int expectedPI, int expectedText) {
        assertEquals("error in attr count",
                expectedAttr, counter.attrCount());
        assertEquals("error in comment count",
                                expectedComment, counter.commentCount());
        assertEquals("error in doc count",
                                expectedDoc, counter.docCount());
        assertEquals("error in docType count",
                                expectedDocType, counter.docTypeCount());
        assertEquals("error in element count",
                                expectedElement, counter.elementCount());
        assertEquals("error in pi count",
                                expectedPI, counter.piCount());
        assertEquals("error in text count",
                                expectedText, counter.textCount());
    }
    /** Test an empty document. */
    public void testSimplestCase() {
        checkCounts (counter, 0, 0, 0, 0, 0, 0, 0);
        doc.walkAll(counter);
        checkCounts (counter, 0, 0, 1, 0, 0, 0, 0);
        // counts are cumulative !
        doc.walkAll(counter);
        checkCounts (counter, 0, 0, 2, 0, 0, 0, 0);
    }
    public void testSingleElHolders() {
        root = new Element("rootNode");
        docNodes.append(root);
        doc.setElementNode(root);      // XXX SHOULD NOT BE NECESSARY
        rootNodes = root.getNodeList();

        assertTrue (root == doc.getElementNode());
        assertTrue (doc  == root.getHolder());
        assertTrue (null == doc.getHolder());
        assertNotNull(rootNodes.getHolder());
        assertTrue (root == rootNodes.getHolder());

        assertNotNull (root.getDocument());     
        assertTrue (doc == root.getDocument());
    } 
    /** Set up a document with a root and four subelements. */
    void setUpRootPlus4() {
        root = new Element("rootNode");
        docNodes.append(root);
        doc.setElementNode(root);      // XXX SHOULD NOT BE NECESSARY
        rootNodes = root.getNodeList();
        rootNodes.append(new Element("abc")).append(new Element("def"))
                 .append(new Element("ghi"));
    }
    /** Test the document with a root plus four subelements. */
    public void testSubElCounts() {
        setUpRootPlus4();
        doc.walkAll(counter);
        checkCounts (counter, 0, 0, 1, 0, 4, 0, 0);
    }
    /** Test that document sprinkled with Comments, Texts, and PIs. */
    public void testWithCommentsTextPI() {
        setUpRootPlus4();
        docNodes.append(new Comment("first comment"))
                .append(new Text("some floating text..."));
        rootNodes.append(new Text("  "))
                .append(new ProcessingInstruction("perl", "chomp;"))
                .append(new Comment("second comment"));
        doc.walkAll(counter);
        checkCounts (counter, 0, 2, 1, 0, 4, 1, 2);
    }                      
    /** Add some attrs to the element node in the 1+4 document, check counts. */
    public void testWithAttrOnElementNode() {
        setUpRootPlus4();
        root.addAttr("attr1", "val1").addAttr("attr2", "val2")
            .addAttr("attr3", "val3").addAttr("attr4", "val4")
            .addAttr("attr5", "val5");
        doc.walkAll(counter);
        checkCounts (counter, 5, 0, 1, 0, 4, 0, 0);

        assertTrue (root == doc.getElementNode());
        assertTrue (doc == root.getDocument());

        rootAList = root.getAttrList();
        for (int i = 0; i < rootAList.size(); i++) {
            assertTrue (doc == rootAList.get(i).getDocument());
            assertTrue (root == rootAList.get(i).getHolder());
        }
        for (int i = 0; i < docNodes.size(); i ++) {
            assertTrue ( doc == docNodes.get(i).getDocument() );
            assertTrue ( doc == docNodes.get(i).getHolder() );
        }
        for (int i = 0; i < rootNodes.size(); i++) {
            assertTrue ( doc == rootNodes.get(i).getDocument() );
            assertTrue ( root == rootNodes.get(i).getHolder() );
        }
        // DETACH THE NodeList FROM THE DOCUMENT
        docNodes.setHolder(null);
        // XXX DOCUMENT STILL POINTS TO THE NodeList XXX DESIGN FLAW
        assertTrue (docNodes == doc.getNodeList());

        /////////////////////////////////////////////////////////////
        // XXX NEXT LINE SHOULD BE UNNECESSARY, BUT IF IT IS NOT PRESENT
        // XXX THE TESTS LABELLED "A" FAIL           XXX BUG !!!!!!!!!!
        root.setDocument(null);         // SHOULD NOT HAVE TO DO THIS! 
        /////////////////////////////////////////////////////////////

        assertNull(docNodes.getHolder());
        assertNull(root.getHolder());       
        assertNull(root.getDocument());
        for (int i = 0; i < rootAList.size(); i++) {
            assertNull(rootAList.get(i).getDocument());         // A
            assertTrue (root == rootAList.get(i).getHolder());
        }
        for (int i = 0; i < rootNodes.size(); i++) {
            assertNull(rootNodes.get(i).getDocument());         // A 
            assertTrue (root == rootNodes.get(i).getHolder());
        }
      
        /////////////////////////////////////////////////////////////
        // REATTACH NODE LIST TO DOCUMENT 
        root.setDocument(doc);          
        // NEXT LINE SHOULD NOT BE NECESSARY; causes (B) to fail
        // docNodes.setHolder(doc);                                 // X
        /////////////////////////////////////////////////////////////
        for (int i = 0; i < rootAList.size(); i++) {
            // NEXT LINE FAILS IF X IS PRESENT
            assertTrue (doc == rootAList.get(i).getDocument());  // B
            assertTrue (root == rootAList.get(i).getHolder());
        }
        for (int i = 0; i < docNodes.size(); i ++) {
            // NEXT LINE FAILS IF X IS PRESENT
            assertTrue ( doc == docNodes.get(i).getDocument() ); 
            // THIS LINE FAILS IF (X) IS COMMENTED OUT
            //assertTrue ( doc == docNodes.get(i).getHolder() );  // C
        }
        for (int i = 0; i < rootNodes.size(); i++) {
            // NEXT LINE FAILS IF X IS PRESENT
            assertTrue ( doc == rootNodes.get(i).getDocument() );
            assertTrue ( root == rootNodes.get(i).getHolder() );
        }
    }
    /////////////////////////////////////////////////////////////////
    // NO DOCTYPE TESTS
    /////////////////////////////////////////////////////////////////
    //
    //
}
