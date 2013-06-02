/* TestSimpleConfig.java */
package org.xlattice.corexml;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import junit.framework.*;

/**
 * Test a facility for binding a simple XML configuration file to 
 * a flat data structure with getters and setters.
 *
 * @author Jim Dixon
 */
public class TestSimpleConfig extends TestCase {

    class ConfigBean {
        private char c;
        private int x;
        private double y;
        private float z;
        private String s;
        private boolean b;
        private long l;
        ConfigBean() {}
        public void setC (char val)    { c = val; }
        public void setX (int  val)    { x = val; }
        public void setL (long  val)   { l = val; }
        public void setY (double val)  { y = val; }
        public void setZ (float val)   { z = val; }
        public void setS (String val)  { s = val; }
        public void setB (boolean val) { b = val; }
        public boolean getB() { return b; }
        public char    getC() { return c; }
        public String  getS() { return s; }
        public int     getX() { return x; }
        public long    getL() { return l; }
        public double  getY() { return y; }
        public float   getZ() { return z; }
    } 
    ConfigBean bean;
        

    public TestSimpleConfig (String name) {
        super(name);
    }

    public void setUp () {
        bean = new ConfigBean();
    }

    public void tearDown() {
    }
    public void testSettingAll () {
        StringReader reader = new StringReader (
                  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<config>\n"
                + "  <b>true</b>\n"
                + "  <c>f</c>\n"
                + "  <s>persnickety</s>\n"
                + "  <x>44</x>\n"
                + "  <y>37.523</y>\n"
                + "  <z>-14.5</z>\n"
                + "  <l>17500</l>\n"
                + "</config>\n"
        );
        try {
            SimpleConfig.bind(bean, reader);
        } catch (IOException ioe) {
            fail ("unexpected " + ioe);
        } catch (CoreXmlException e) {
            fail ("unexpected " + e);
        }
        assertEquals(44,            bean.getX());           // int
        assertEquals(17500,         bean.getL());           // long
        assertTrue(                 bean.getB());           // boolean
        assertEquals('f',           bean.getC());           // char
        assertEquals("persnickety", bean.getS());           // String
        assertEquals(37.523,        bean.getY(), 0.001);    // double
        assertEquals(-14.5f,        bean.getZ(), 0.001);    // float
    } 
    public void testHexValues () {
        StringReader reader = new StringReader (
                  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<config>\n"
                + "  <x>0x44</x>\n"           // int
                + "  <l>0x17500</l>\n"        // long
                + "</config>\n"
        );
        try {
            SimpleConfig.bind(bean, reader);
        } catch (IOException ioe) {
            fail ("unexpected " + ioe);
        } catch (CoreXmlException e) {
            fail ("unexpected " + e);
        }
        int hex44 = 0x44;
        long hex17500 = 0x17500;
        assertEquals(hex44,     bean.getX());
        assertEquals(hex17500,  bean.getL());
        // other values should be defaulted
        assertFalse(            bean.getB());           // boolean
        assertEquals(0  ,       bean.getC());           // char
        assertEquals(null,      bean.getS());           // String
        assertEquals(0.0,       bean.getY(), 0.001);    // double
        assertEquals(0.0,       bean.getZ(), 0.001);    // float
    } 
        
    public void testBadTags () {
        StringReader reader = new StringReader (
                  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<config>\n"
                + "  <q>0x44</q>\n"           
                + "  <r>0x17500</r>\n"      
                + "</config>\n"
        );
        try {
            SimpleConfig.bind(bean, reader);
            fail ("expected CoreXmlException!");
        } catch (IOException ioe) {
            fail ("unexpected " + ioe);
        } catch (CoreXmlException e) {
            /* test was successful */
        }
    } 
    public void testBadValues () {
        StringReader reader = new StringReader (
                  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<config>\n"
                + "  <x>froggyBoy</x>\n"           
                + "</config>\n"
        );
        try {
            SimpleConfig.bind(bean, reader);
            fail ("expected NumberFormatException!");
        } catch (IOException ioe) {
            fail ("unexpected " + ioe);
        } catch (CoreXmlException e) {
            fail ("unexpected " + e);
        } catch (NumberFormatException e) {
            /* test was successful */
        }
    } 
}
