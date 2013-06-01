/* TestNameGenerator.java */
package org.xlattice.corexml.bind;

import junit.framework.*;
import static org.xlattice.corexml.bind.NameGenerator.*;

public class TestNameGenerator extends TestCase {

    public TestNameGenerator (String name)      throws Exception {
        super(name);
    }
    public void setUp ()                        throws Exception  {
    }

    public void testDehyphenation()             throws Exception {
        assertEquals("gEt",         dehyphenate("g-et"));
        assertEquals("geT",         dehyphenate("ge-t"));
        assertEquals("getFoo",      dehyphenate("get-foo"));
        assertEquals("getFooBar",   dehyphenate("get-foo-bar"));
        try {
            dehyphenate("-get");
            fail("didn't catch hyphen as first character");
        } catch (Exception e) { /* success */ }
        try {
            dehyphenate("get-");
            fail("didn't catch hyphen as last character");
        } catch (Exception e) { /* success */ }
        try {
            dehyphenate("get--foo");
            fail("didn't catch double hyphen");
        } catch (Exception e) { /* success */ }
    }
    public void testFoo()                       throws Exception {
        assertEquals("getFoo",      getterName("foo"));
        assertEquals("setFoo",      setterName("foo"));
        assertEquals("addFoo",      adderName("foo"));
        assertEquals("removeFoo",   removerName("foo"));
        assertEquals("sizeFoos",    sizerName("foo"));
        assertEquals("_foo",        varName("foo"));
        assertEquals("_foos",       collName("foo"));
    }
    public void testID()                       throws Exception {
        assertEquals("getID",       getterName("id"));
        assertEquals("setID",       setterName("id"));
        assertEquals("addID",       adderName("id"));
        assertEquals("removeID",    removerName("id"));
        assertEquals("sizeIDs",     sizerName("id"));
        assertEquals("_id",         varName("id"));
        assertEquals("_ids",        collName("id"));
    }
    public void testLady()                       throws Exception {
        assertEquals("getLady",     getterName("lady"));
        assertEquals("setLady",     setterName("Lady"));
        assertEquals("addLady",     adderName("lady"));
        assertEquals("removeLady",  removerName("lady"));
        assertEquals("sizeLadies",  sizerName("Lady"));
        assertEquals("_lady",       varName("lady"));
        assertEquals("_ladies",     collName("Lady"));
    }
    public void testPlurals()                   throws Exception {
        assertEquals("_clazzes",    collName("clazz"));
    }
}
