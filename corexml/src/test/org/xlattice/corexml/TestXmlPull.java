/* TestXmlPull.java */
package org.xlattice.corexml;

import java.io.IOException;
import java.io.StringReader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import junit.framework.*;

/**
 * @author Jim Dixon
 */

public class TestXmlPull extends TestCase {

    XmlPullParserFactory factory;
    XmlPullParser xpp;
    
    public TestXmlPull (String name) {
        super(name);
    }

    public void setUp () {
    }

    public void tearDown() {
        factory = null;
        xpp     = null;
    }
   
    /** 
     * Exploratory testing which shows that a next() on an empty
     * stream causes an IOException.
     */
    public void testReadingEmpty() {
        try {
            factory = XmlPullParserFactory.newInstance();
            assertNotNull(factory);
            // not namespace-aware
            xpp = factory.newPullParser();
            assertNotNull(xpp);
            StringReader reader = new StringReader ("");
            xpp.setInput( reader );
            int eventType = xpp.getEventType();
            assertEquals (xpp.START_DOCUMENT, eventType);
            try {
                xpp.next();
                fail ("expected IOException");
            } catch (IOException ioe) {
                /* ignore it */
            }
            
        } catch (XmlPullParserException e) {
            fail("unexpected XmlPullParserException " + e);
        }

    }
    /** 
     * Exploratory testing which shows that a next() on a stream consisting
     * only of an XML declaration also causes an IOException.
     */
    public void testReadingJustHeader() {
        try {
            factory = XmlPullParserFactory.newInstance();
            assertNotNull(factory);
            // not namespace-aware
            xpp = factory.newPullParser();
            assertNotNull(xpp);
            StringReader reader = new StringReader (
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            xpp.setInput( reader );
            int eventType = xpp.getEventType();
            try {
                xpp.next();
                fail ("expected IOException");
            } catch (IOException ioe) {
                /* ignore it */
            }
        } catch (XmlPullParserException e) {
            fail("unexpected XmlPullParserException " + e);
        }
    } 
    public void testSimpleXml() {
        try {
            factory = XmlPullParserFactory.newInstance();
            assertNotNull(factory);
            // not namespace-aware
            xpp = factory.newPullParser();
            assertNotNull(xpp);
            StringReader reader = new StringReader (
                  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<tag attr1=\"value1\" attr2=\"value2\" attr3=\"xxx\">\n"
                + "  <tag2 attr4=\"value4\">\n"
                + "    <tag3> some text </tag3>\n"
                + "    <tag4/>\n"
                + "  </tag2>\n"
                + "</tag>\n"
            );
            xpp.setInput( reader );
            int eventType = xpp.getEventType();
            try {
                xpp.nextToken();
                assertEquals (xpp.IGNORABLE_WHITESPACE, xpp.getEventType());

                xpp.nextToken();
                assertEquals (xpp.START_TAG, xpp.getEventType());
                assertEquals ("tag",    xpp.getName());
                assertEquals (3,        xpp.getAttributeCount());
                assertEquals ("attr1",  xpp.getAttributeName(0));
                assertEquals ("attr2",  xpp.getAttributeName(1));
                assertEquals ("attr3",  xpp.getAttributeName(2));
                assertEquals ("value1", xpp.getAttributeValue(0));
                assertEquals ("value2", xpp.getAttributeValue(1));
                assertEquals ("xxx",    xpp.getAttributeValue(2));

                // newline and two spaces before first subelement
                xpp.nextToken();
                assertEquals (xpp.TEXT, xpp.getEventType());
                assertEquals ("\n  ", xpp.getText());

                xpp.nextToken();
                assertEquals (xpp.START_TAG, xpp.getEventType());
                assertEquals ("tag2",   xpp.getName());
                assertEquals (1,        xpp.getAttributeCount());
                assertEquals ("attr4",  xpp.getAttributeName(0));
                assertEquals ("value4", xpp.getAttributeValue(0));
                
                // newline and four spaces before next subelement
                xpp.nextToken();
                assertEquals (xpp.TEXT, xpp.getEventType());
                assertEquals ("\n    ", xpp.getText());

                // note change in style, using nextToken() return value
                assertEquals (xpp.START_TAG, xpp.nextToken());
                assertEquals ("tag3",   xpp.getName());
                assertEquals (0,        xpp.getAttributeCount());
                assertEquals (xpp.TEXT, xpp.nextToken());
                assertEquals (" some text ", xpp.getText());
                assertEquals (xpp.END_TAG, xpp.nextToken());    // </tag3>

                assertEquals (xpp.TEXT, xpp.nextToken());       // ignore it

                assertEquals (xpp.START_TAG, xpp.nextToken());
                assertEquals ("tag4",   xpp.getName());
                assertEquals (0,        xpp.getAttributeCount());
                assertEquals (xpp.END_TAG, xpp.nextToken());    // />
                
                assertEquals (xpp.TEXT, xpp.nextToken());       // whitespace
                assertEquals (xpp.END_TAG, xpp.nextToken());    // </tag2>
                assertEquals (xpp.TEXT, xpp.nextToken());       // whitespace
                assertEquals (xpp.END_TAG, xpp.nextToken());    // </tag>
                assertEquals (xpp.IGNORABLE_WHITESPACE, xpp.nextToken());
                assertEquals (xpp.END_DOCUMENT, xpp.nextToken());
                // any more next()s should cause an IOException
                
            } catch (IOException ioe) {
                fail ("unexpected IOException");
            }

        } catch (XmlPullParserException e) {
            fail("unexpected XmlPullParserException " + e);
        }
        // we are at the end of input, there is no next token
        try {
            xpp.nextToken();
            fail("expected XmlPullParserException!");
        } catch (XmlPullParserException e) { 
            /* ignore it */
        } catch (IOException ioe) {
            fail("unexpected IOException!");
        }  
    } 

    /** 
     * The pull parser cannot handle attributes without values.
     */
    public void testAttrWithoutValue() {
        try {
            factory = XmlPullParserFactory.newInstance();
            assertNotNull(factory);
            // not namespace-aware
            xpp = factory.newPullParser();
            assertNotNull(xpp);
            StringReader reader = new StringReader (
                  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<tag attr1>\n"
                + "</tag>\n"
            );
            xpp.setInput( reader );
            // first nextToken() eats the XML declaration
            assertEquals (xpp.IGNORABLE_WHITESPACE, xpp.nextToken());

            // the parser throws an exception here because attr1 has 
            // no value, it's not followed by an = sign
            xpp.nextToken();
            fail("expected XmlPullParserException!");

        } catch (XmlPullParserException e) {
            /* ignore it */
        } catch (IOException ioe) {
            fail ("unexpected IOException");
        }
    }
    public void testNext() {
        try {
            factory = XmlPullParserFactory.newInstance();
            assertNotNull(factory);
            // not namespace-aware
            xpp = factory.newPullParser();
            assertNotNull(xpp);
            StringReader reader = new StringReader (
                  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<tag attr1=\"value1\" attr2=\"value2\" attr3=\"xxx\">\n"
                + "  <tag2 attr4=\"value4\">\n"
                + "    <tag3> some text </tag3>\n"
                + "    <tag4/>\n"
                + "  </tag2>\n"
                + "</tag>\n"
            );
            xpp.setInput( reader );
            int eventType = xpp.getEventType();
            try {
                // ignorable white space gets ignored
                assertEquals (xpp.START_TAG, xpp.next());
                assertEquals ("tag",    xpp.getName());
                assertEquals (3,        xpp.getAttributeCount());
                assertEquals ("attr1",  xpp.getAttributeName(0));
                assertEquals ("attr2",  xpp.getAttributeName(1));
                assertEquals ("attr3",  xpp.getAttributeName(2));
                assertEquals ("value1", xpp.getAttributeValue(0));
                assertEquals ("value2", xpp.getAttributeValue(1));
                assertEquals ("xxx",    xpp.getAttributeValue(2));

                // newline and two spaces before first subelement
                assertEquals (xpp.TEXT, xpp.next());
                assertEquals ("\n  ", xpp.getText());

                assertEquals (xpp.START_TAG, xpp.next());
                assertEquals ("tag2",   xpp.getName());
                assertEquals (1,        xpp.getAttributeCount());
                assertEquals ("attr4",  xpp.getAttributeName(0));
                assertEquals ("value4", xpp.getAttributeValue(0));
                
                // newline and four spaces before next subelement
                assertEquals (xpp.TEXT, xpp.next());
                assertEquals ("\n    ", xpp.getText());

                assertEquals (xpp.START_TAG, xpp.next());
                assertEquals ("tag3",   xpp.getName());
                assertEquals (0,        xpp.getAttributeCount());
                assertEquals (xpp.TEXT, xpp.next());
                assertEquals (" some text ", xpp.getText());
                assertEquals (xpp.END_TAG, xpp.next());    // </tag3>

                assertEquals (xpp.TEXT, xpp.next());       // ignore it

                assertEquals (xpp.START_TAG, xpp.nextToken());
                assertEquals ("tag4",   xpp.getName());
                assertEquals (0,        xpp.getAttributeCount());
                assertEquals (xpp.END_TAG, xpp.nextToken());    // />
                
                assertEquals (xpp.TEXT, xpp.nextToken());       // whitespace
                assertEquals (xpp.END_TAG, xpp.nextToken());    // </tag2>
                assertEquals (xpp.TEXT, xpp.nextToken());       // whitespace
                assertEquals (xpp.END_TAG, xpp.nextToken());    // </tag>
                assertEquals (xpp.IGNORABLE_WHITESPACE, xpp.nextToken());
                assertEquals (xpp.END_DOCUMENT, xpp.nextToken());
                // any more next()s should cause an IOException
                
            } catch (IOException ioe) {
                fail ("unexpected IOException");
            }

        } catch (XmlPullParserException e) {
            fail("unexpected XmlPullParserException " + e);
        }
    }
}
