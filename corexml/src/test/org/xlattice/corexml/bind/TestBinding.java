/* TestBinding.java */
package org.xlattice.corexml.bind;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import junit.framework.*;
import org.xlattice.corexml.CoreXmlException;
import org.xlattice.corexml.CoreXmlTestCase;

/**
 *
 * @author Jim Dixon
 */
public class TestBinding extends CoreXmlTestCase {

    Mapping map;
    AttrBinding aBoolBinding,   aCharBinding, aFloatBinding,
                aDoubleBinding, aByteBinding, aShortBinding,
                aIntBinding,    aLongBinding, aStringBinding;
    SubElBinding vBoolBinding,  vCharBinding, vFloatBinding,
                vDoubleBinding, vByteBinding, vShortBinding,
                vIntBinding,    vLongBinding, vStringBinding;
    Prims prims;

    public final static String primsClassName = 
                            "org.xlattice.corexml.bind.TestBinding$Prims";
    public class Prims {
        // to be bound to attribute values
        boolean aBool;
        char    aChar;
        float   aFloat;
        double  aDouble;
        byte    aByte;
        short   aShort;
        int     aInt;
        long    aLong;
        String  aString;
        // to be bound to element values
        boolean vBool;
        char    vChar;
        float   vFloat;
        double  vDouble;
        byte    vByte;
        short   vShort;
        int     vInt;
        long    vLong;
        String  vString;
        Prims () {}
        public boolean isABool()   { return aBool; }
        public char    getAChar()  { return aChar; }
        public float   getAFloat() { return aFloat; }
        public double  getADouble(){ return aDouble; }
        public byte    getAByte()  { return aByte; }
        public short   getAShort() { return aShort; }
        public int     getAInt()   { return aInt; }
        public long    getALong()  { return aLong; }
        public String  getAString(){ return aString; }
        public void setABool   (boolean value) { aBool   = value; }
        public void setAChar   (char value)    { aChar   = value; }
        public void setAFloat  (float value)   { aFloat  = value; }
        public void setADouble (double value)  { aDouble = value; }
        public void setAByte   (byte value)    { aByte   = value; }
        public void setAShort  (short value)   { aShort  = value; }
        public void setAInt    (int value)     { aInt    = value; }
        public void setALong   (long value)    { aLong   = value; }
        public void setAString (String value)  { aString = value; }

        public boolean isVBool()  { return vBool; }
        public char    getVChar()  { return vChar; }
        public float   getVFloat() { return vFloat; }
        public double  getVDouble(){ return vDouble; }
        public byte    getVByte()  { return vByte; }
        public short   getVShort() { return vShort; }
        public int     getVInt()   { return vInt; }
        public long    getVLong()  { return vLong; }
        public String  getVString(){ return vString; }
        public void setVBool   (boolean value) { vBool   = value; }
        public void setVChar   (char value)    { vChar   = value; }
        public void setVFloat  (float value)   { vFloat  = value; }
        public void setVDouble (double value)  { vDouble = value; }
        public void setVByte   (byte value)    { vByte   = value; }
        public void setVShort  (short value)   { vShort  = value; }
        public void setVInt    (int value)     { vInt    = value; }
        public void setVLong   (long value)    { vLong   = value; }
        public void setVString (String value)  { vString = value; }
    }
    
    public TestBinding (String name) {
        super(name);
    }

    public void setUp () {
        prims = null;
        map   = null;
        aBoolBinding = aCharBinding = aFloatBinding = aDoubleBinding 
                     = aByteBinding = aShortBinding = aIntBinding 
                     = aLongBinding = aStringBinding = null;
        vBoolBinding = vCharBinding = vFloatBinding = vDoubleBinding 
                     = vByteBinding = vShortBinding = vIntBinding 
                     = vLongBinding = vStringBinding;
    }

    public void tearDown() {
    }

    /** test static indexes */
    public void testIndices() {
        assertEquals(Binding.STRING,  Binding.getTypeIndex(String.class));
        assertEquals(Binding.BOOLEAN, Binding.getTypeIndex(Boolean.TYPE));
        assertEquals(Binding.CHAR,    Binding.getTypeIndex(Character.TYPE));
        assertEquals(Binding.INT,     Binding.getTypeIndex(Integer.TYPE));
    }
    private void buildMapping()             throws CoreXmlException {
        map = new Mapping ("prims", primsClassName);
        aBoolBinding   = new AttrBinding("aBool");   map.add(aBoolBinding);
        aCharBinding   = new AttrBinding("aChar");   map.add(aCharBinding);
        aFloatBinding  = new AttrBinding("aFloat");  map.add(aFloatBinding);
        aDoubleBinding = new AttrBinding("aDouble"); map.add(aDoubleBinding);
        aByteBinding   = new AttrBinding("aByte");   map.add(aByteBinding);
        aShortBinding  = new AttrBinding("aShort");  map.add(aShortBinding);
        aIntBinding    = new AttrBinding("aInt");    map.add(aIntBinding);
        aLongBinding   = new AttrBinding("aLong");   map.add(aLongBinding);
        aStringBinding = new AttrBinding("aString"); map.add(aStringBinding);
        
        vBoolBinding   = new SubElBinding("vBool");   map.add(vBoolBinding);
        vCharBinding   = new SubElBinding("vChar");   map.add(vCharBinding);
        vFloatBinding  = new SubElBinding("vFloat");  map.add(vFloatBinding);
        vDoubleBinding = new SubElBinding("vDouble"); map.add(vDoubleBinding);
        vByteBinding   = new SubElBinding("vByte");   map.add(vByteBinding);
        vShortBinding  = new SubElBinding("vShort");  map.add(vShortBinding);
        vIntBinding    = new SubElBinding("vInt");    map.add(vIntBinding);
        vLongBinding   = new SubElBinding("vLong");   map.add(vLongBinding);
        vStringBinding = new SubElBinding("vString"); map.add(vStringBinding);
        map.join();
    }

    public static final String defaultCharAsString 
                                        = Character.toString((char)0);

    public void testDefaultBindings ()              throws Exception {
        buildMapping();
        assertNotNull(map);
        assertEquals (9, map.getAttrBag().size());
        assertEquals (9, map.getOrdering().size());
        prims = new Prims();
        assertEquals("false", aBoolBinding.getField(prims));
        assertEquals(defaultCharAsString, 
                                        aCharBinding.getField(prims));
        assertEquals("0.0", aFloatBinding.getField(prims));
        assertEquals("0.0", aDoubleBinding.getField(prims));
        assertEquals("0",   aByteBinding.getField(prims));
        assertEquals("0",   aShortBinding.getField(prims));
        assertEquals("0",   aIntBinding.getField(prims));
        assertEquals("0",   aLongBinding.getField(prims));
        assertEquals(null,  aStringBinding.getField(prims));

        assertEquals("false", vBoolBinding.getField(prims));
        assertEquals(defaultCharAsString, 
                                        vCharBinding.getField(prims));
        assertEquals("0.0", vFloatBinding.getField(prims));
        assertEquals("0.0", vDoubleBinding.getField(prims));
        assertEquals("0",   vByteBinding.getField(prims));
        assertEquals("0",   vShortBinding.getField(prims));
        assertEquals("0",   vIntBinding.getField(prims));
        assertEquals("0",   vLongBinding.getField(prims));
        assertEquals(null,  vStringBinding.getField(prims));
    }
    public void testSetGet ()               throws Exception {
        buildMapping();
        prims = new Prims();
        aBoolBinding.setField   (prims, "true");
        aCharBinding.setField   (prims, "z");
        aFloatBinding.setField  (prims, "472");
        aDoubleBinding.setField (prims, "-47");
        aByteBinding.setField   (prims, "97");
        aShortBinding.setField  (prims, "1147");
        aIntBinding.setField    (prims, "4082");
        aLongBinding.setField   (prims, "-5000000000");
        aStringBinding.setField (prims, "tommy boy");

        assertEquals("true",    aBoolBinding.getField(prims));
        assertEquals("z",       aCharBinding.getField(prims));
        assertEquals("472.0",   aFloatBinding.getField(prims));
        assertEquals("-47.0",   aDoubleBinding.getField(prims));
        assertEquals("97",      aByteBinding.getField(prims));
        assertEquals("1147",    aShortBinding.getField(prims));
        assertEquals("4082",        aIntBinding.getField(prims));
        assertEquals("-5000000000", aLongBinding.getField(prims));
        assertEquals("tommy boy",   aStringBinding.getField(prims));

        vBoolBinding.setField   (prims, "true");
        vCharBinding.setField   (prims, "z");
        vFloatBinding.setField  (prims, "47");
        vDoubleBinding.setField (prims, "47");
        vByteBinding.setField   (prims, "47");
        vShortBinding.setField  (prims, "47");
        vIntBinding.setField    (prims, "47");
        vLongBinding.setField   (prims, "47");
        vStringBinding.setField (prims, "jack the lad");

        assertEquals("true",vBoolBinding.getField(prims));
        assertEquals("z",   vCharBinding.getField(prims));
        assertEquals("47.0",vFloatBinding.getField(prims));
        assertEquals("47.0",vDoubleBinding.getField(prims));
        assertEquals("47",  vByteBinding.getField(prims));
        assertEquals("47",  vShortBinding.getField(prims));
        assertEquals("47",  vIntBinding.getField(prims));
        assertEquals("47",  vLongBinding.getField(prims));
        assertEquals("jack the lad",  vStringBinding.getField(prims));
    }
}
