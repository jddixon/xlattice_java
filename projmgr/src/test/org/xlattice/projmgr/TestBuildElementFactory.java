/* TestBuildElementFactory.java */
package org.xlattice.projmgr;

import junit.framework.*;

/**
 * @author Jim Dixon
 */

import org.xlattice.corexml.om.*;

public class TestBuildElementFactory 
                        extends org.xlattice.corexml.CoreXmlTestCase {
    private Element   elm;
    private NodeList nodes;
    private Attr      attr;
    private AttrList  attrs;
    
    public TestBuildElementFactory (String name) {
        super(name);
    }
    public void setUp() {
        elm   = null;
        nodes = null;
        attr  = null;
        attrs = null;
    }
    public void tearDown() {
    }
    public void testMakeProperty () {
        elm = BuildElementFactory.makeProperty("foo", "bar");
        attr = elm.getAttrList().get(0);
        assertNotNull(attr);
        assertNull(attr.getPrefix());
        assertEquals("name", attr.getName());
        assertEquals("foo",  attr.getValue());
        attr = elm.getAttrList().get(1);
        assertNotNull(attr);
        assertNull(attr.getPrefix());
        assertEquals("value",attr.getName());
        assertEquals("bar",  attr.getValue());
        
        assertSameSerialization(
                "<property name=\"foo\" value=\"bar\"/>",
                elm.toXml());
    }
    public void testMakeAvailable() {
        String path     = "${lib.dir}";
        String groupId  = "foo";
        String id       = "bar";
        String type     = "jar";
        elm = BuildElementFactory
                    .makeAvailableForDep(path, groupId, id, null, type);
        assertNotNull(elm);
        attrs = elm.getAttrList();
        assertEquals(2, attrs.size());
        assertSameSerialization(
                "<available file=\"${lib.dir}/foo/bar.jar\" "
                    + "property=\"foo-bar-present\"/>", 
                elm.toXml());
    }
    public void testMakeDepGetterTarget() {
        String path     = "${lib.dir}";
        String groupId  = "foo";
        String id       = "bar";
        String version  = "0.15a2";
        String type     = "jar";
        String url      = "http://whoozits.org/bigfoo/";
        elm = BuildElementFactory.makeDepGetterTarget(
                            path, groupId, id, version, type, url);
        assertNotNull(elm);
        assertEquals("target", elm.getName());
        attrs = elm.getAttrList();
        assertEquals(3, attrs.size());
        // subelement "get" will have 4 attributes
                
        assertSameSerialization(
                "<target name=\"get-foo-bar\" unless=\"foo-bar-present\""
                    +                           " depends=\"init\" >"
                    + "<get dest=\"${lib.dir}/foo/bar-0.15a2.jar\""
                    +   " usetimestamp=\"true\" ignoreerrors=\"true\""
                    +   " src=\"http://whoozits.org/bigfoo/bar-0.15a2.jar\" />"
                    + "</target>",
                elm.toXml());
    } // GEEP
    public void testMakeGetDepsTarget() {
        Dependency [] deps = new Dependency [4];
        for (int i = 0; i < deps.length; i++)
            deps[i] = new Dependency();
        deps[0].setGroupId("boys").setArtifactId("tom");
        deps[1].setGroupId("boys").setArtifactId("dick");
        deps[2].setGroupId("boys").setArtifactId("harry");
        deps[3].setGroupId("boys").setArtifactId("joe");
        elm = BuildElementFactory.makeGetDepsTarget(deps);
        assertNotNull(elm);
        assertEquals("target", elm.getName());
        attrs = elm.getAttrList();
        assertEquals(2, attrs.size());

        assertSameSerialization(
                "<target name=\"get-deps\" "
                +   "depends=\"get-boys-tom, get-boys-dick, get-boys-harry, get-boys-joe\" />",
                elm.toXml());
    }
    public void testMakeSectionHeader() {
        nodes = BuildElementFactory.makeSectionHeader ("hello", 18);
        assertNotNull(nodes);
        assertEquals(3, nodes.size());

        assertSameSerialization(
                "<!-- ====================== -->" +
                "<!-- == H E L L O ========= -->" +
                "<!-- ====================== -->",
                nodes.toXml());
        }
}
