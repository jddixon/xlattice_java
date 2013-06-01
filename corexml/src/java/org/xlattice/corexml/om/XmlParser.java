/* XmlParser.java */
package org.xlattice.corexml.om;

import java.io.Reader;
import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import org.xlattice.corexml.CoreXmlException;

/**
 * Interface to the XmlPullParser.
 * 
 * @author Jim Dixon
 */
public class XmlParser {
    private XmlPullParser xpp;
    private Document doc;

    /**
     * Create a parser.
     *
     * @param reader source for XML input
     */
    public XmlParser(Reader reader) throws CoreXmlException {
        if (reader == null) 
            throw new NullPointerException();
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            xpp = factory.newPullParser();
            xpp.setInput(reader);
            // THIS SHOULD NOT BE WIRED IN 
            xpp.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
        } catch (XmlPullParserException e) {
            throw new CoreXmlException("could not create parser: " 
                    + e.toString());
        }
    }
    /**
     * Given a parser with open XML input, produce a Document in 
     * XLattice XML object form.
     */
    public Document read () throws CoreXmlException, IOException  {
        boolean rootHasBeenFound = false;
        try {
            doc = new Document(
                (String)xpp.getProperty(
                  "http://xmlpull.org/v1/doc/properties.html#xmldecl-version"),
                xpp.getInputEncoding());
            int eventType = xpp.getEventType();
            if (eventType == XmlPullParser.START_DOCUMENT) 
                doc.populator(xpp, 0, XmlPullParser.END_DOCUMENT);
            return doc;
        } catch (XmlPullParserException e) {
            throw new CoreXmlException("exception reading document: " 
                    + e.toString());
        }
    }
}
