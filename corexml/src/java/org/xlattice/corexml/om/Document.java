/* Document.java */
package org.xlattice.corexml.om;

/**
 * An XML Document, a Holder which can contain only one Element in 
 * its NodeList, has no attributes, and no namespaces.
 *
 * @author Jim Dixon
 */
public class Document extends Holder {

    private String version  = "1.0";
    private String encoding = "UTF-8";

    private DocumentType docType;

    private Element elNode;
    
    public static final String DEFAULT_XML_DECL = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

    /**
     * Create an XML document with the XML declaration passed.
     *
     * @param decl the XML declaration
     */
    public Document(String decl) {
        super();
        // XXX SHOULD PROPERLY PARSE DECLARATION ! //

    }
    /** Create an XML document with the default XML declaration. */
    public Document() {
        this(DEFAULT_XML_DECL);
    }
    /**
     * Create an XML document with the version number and encoding
     * specified.
     * 
     * XXX CHECKS NEEDED
     *
     * @param version  XML version number; if null, uses the default
     * @param encoding if null, the default is used
     */
    public Document (String version, String encoding) {
        super();
        if (version != null)
            this.version = version;
        if (encoding != null)
            this.encoding = encoding;
    }
    // PROPERTIES ///////////////////////////////////////////////////
   
    /**
     * @return the XML document type
     */
    public DocumentType getDocType() {
        return docType;
    }
    /**
     * 
     * @return a reference to this document, to ease chaining
     */
    Document setDocType(DocumentType type) {
        // XXX NEED MORE CHECKS
        if (type == null)
            throw new NullPointerException("null DocType");
        docType = type;
        return this;
    }

    /** @return the XML encoding used in the document */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Get the document's element node; there may only be one.
     * 
     * @return a reference to the document's element node
     */
    public Element getElementNode() {
        return elNode;
    }
    /**
     * Set the document's element node.   There may only be one. 
     *
     * XXX THIS SHOULD BE EFFECTED IN NodeList.  IT IS AN ERROR
     * XXX TO DO IT HERE.
     * 
     * XXX There must be some checks to ensure that the 
     * element is well-formed AND that this does not introduce
     * cycles into the graph.
     */
    public Document setElementNode(Element newElementNode) {
        newElementNode.setHolder(this);
        elNode = newElementNode;
        newElementNode.setDocument(this);  // XXX THIS IS NECESSARY
        return this;
    }

    /** @return the XML version of this document */
    public String getVersion () {
        return version;
    }
    // NODE METHODS /////////////////////////////////////////////////
    /** @return true: this is a document node */
    public boolean isDocument() {
        return true;
    }
    /**
     * Generate the XML document in String form.  The standard XML
     * declaration is prefixed.  This method traverses the entire
     * document recursively.  The document is <b>not</b> indented.
     * 
     * @return the entire document in String form
     */
    public String toXml() {
        StringBuffer sb = new StringBuffer("<?xml version=\""
                + version + "\" encoding=\"" + encoding + "\"?>\n");
        for (int i = 0; i < nodes.size(); i++)
            sb.append( ((Node)nodes.get(i)).toXml() );
        return sb.toString();
    }
}
