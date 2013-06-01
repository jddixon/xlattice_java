/* CoreXmlTestCase.java */
package org.xlattice.corexml;

import java.util.Iterator;
import junit.framework.*;
import org.xlattice.corexml.om.Node;
import org.xlattice.corexml.expr.NodeSet;

/**
 * JUnit's TestCase with some extensions.  Supports tests for 
 * equality between XPath NodeSets and between serializations
 * of XML fragments at the object model level.
 * 
 * @author Jim Dixon
 */
public abstract class CoreXmlTestCase extends TestCase {

    private final static String delim = " \t\r\n";

    public CoreXmlTestCase (String name) {
        super(name);
    }
    /////////////////////////////////////////////////////////////////
    // ASSERT EQUALS ////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////

    // NodeSet - NodeSet //////////////////////////////////
    /**
     * Checks whether two NodeSets consist of identical nodes.
     *
     * @param expected NodeSet expected by the test designer
     * @param actual   the NodeSet actually returned
     */
    public static void assertEquals (NodeSet expected, NodeSet actual) {
        assertEquals (null, expected, actual);
    }
    public static void assertEquals (String msg, 
                                     NodeSet expected, NodeSet actual) {
        if (msg == null)
            msg = "";
        if (expected == actual)     // same NodeSet
            return;
        if (expected == null)
            junit.framework.Assert.fail("null 'expected' NodeSet");
        if (actual == null)
            junit.framework.Assert.fail("null 'actual' NodeSet");
        if (msg.length() > 0)
            msg += ": ";
        int sizeE = expected.size();
        int sizeA = actual  .size();
        if (sizeE != sizeA) {
            msg += "expected " + sizeE + " nodes, found " + sizeA;
        } else {
            Iterator it = expected.iterator();
            boolean ok = true;
            Node node = null;
            while (it.hasNext()) {
                node = (Node)it.next();
                if (!actual.contains(node)) {
                    ok = false;
                    break;
                }
            }
            if (ok) 
                return;
            else 
                msg += "expected but not found: " + node.toXml();
        }
        junit.framework.Assert.fail(msg); 
    }
    // String - String ////////////////////////////////////
    /**
     * Checks whether two Strings are equal.  Somewhat more helpful
     * than the JUnit method it overrides but XXX still a bit buggy.
     */
    public static void assertEquals (String msg, 
                                     String expected, String actual) {
        if (msg == null)
            msg = "";
        if (expected == actual)
            return;
        if (expected == null)
            junit.framework.Assert.fail("null 'expected' String");
        if (expected.equals(actual))
            return;
        if (actual == null) {
            msg += " 'ACTUAL' String is null";
        } else {
            if (expected.length() < 35 && actual.length() < 35) {
                msg += "\n    " + expected + "\n    " + actual;
            } else {
                msg += highLightDifference(expected, actual);
            }
        }
        junit.framework.Assert.fail(msg); 
    }
    /** Override JUnit method to handle Strings better. */
    public static void assertEquals (String expected, String actual) {
        assertEquals(null, expected, actual);
    } 
    
    /////////////////////////////////////////////////////////////////
    // ASSERT SAME SERIALIZATION ////////////////////////////////////
    /////////////////////////////////////////////////////////////////
    /**
     * Collapse all runs of delimiters in a String to a single
     * space.  XXX This didn't work.  Modified to just to eliminate
     * all delimiters.  The problem was that the process sometimes
     * introduces delimiters, for example between XML elements
     * (comments, cdata).  Needs more work.
     */
    protected static String noDelimCopy (String s) {
        StringBuffer sb    = new StringBuffer(s.length());
        boolean inDelim    = false;
        boolean firstDelim = true;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (delim.indexOf(c) < 0) {
                inDelim = false;
                sb.append(c);
            }
//          else {
//              if (!inDelim) {
//                  sb.append(" ");
//                  inDelim = true;
//              }
//          }
        }
        return sb.toString();
    }
    protected static String highLightDifference (String a, String b) {
        int aLen = a.length();
        int bLen = b.length();
        int mark; 
        for (mark = 0; mark < aLen && mark < bLen; mark++)
            if (a.charAt(mark) != b.charAt(mark))
                break;
        // XXX LENGTHS MAY DIFFER
        // DEBUG
        System.out.println(
                    "A: " + a 
                + "\nB: " + b
                + "\nmark = " + mark);
                
        // END
        StringBuffer sb = new StringBuffer("\n");
        if (mark < 35) {
            for (int i = 0; i < 35 && i < aLen && i < bLen; i++)
                if (i != mark)
                    sb.append("-");
                else 
                    sb.append("+");
            sb.append("\n").append(a).append("\n").append(b);
        } else {
            int end = mark + 12;
            
            for (int i = mark - 12; i < end ; i++) {
                if (i != mark)
                    sb.append("-");
                else 
                    sb.append("+");
            }
            sb.append("\n");
            for (int i = mark -12; i < end && i < aLen; i++) 
                sb.append(a.charAt(i));
            sb.append("\n");
            for (int i = mark -12; i < end && i < bLen; i++) 
                sb.append(b.charAt(i));
        }
        sb.append("\n");
        return sb.toString();
    }
    /**
     * Check whether two XML fragments in String format are the same
     * ignoring delimiters.  
     */
    public static void assertSameSerialization(String msg, String expected, 
                                                    String actual ) {
        if (msg == null)
            msg = "";
        if (expected == actual)
            return;
        if (expected == null)
            junit.framework.Assert.fail("null 'expected' String");
        if (expected.equals(actual))
            return;

        String eNoDelim = noDelimCopy(expected.trim());
        String aNoDelim = noDelimCopy(actual.trim());
       
            
        if (eNoDelim.equals(aNoDelim))
            return;
        
        if (eNoDelim.length() < 35 && aNoDelim.length() < 35) {
            msg += "\n    " + eNoDelim + "\n    " + aNoDelim;
        } else {
            msg += highLightDifference(eNoDelim, aNoDelim);
        }
        junit.framework.Assert.fail(msg); 
    }
    public static void assertSameSerialization(String expected, String actual) {
        assertSameSerialization(null, expected, actual);
    }
    
}
