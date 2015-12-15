/* TestDecimalVersion.go */

package org.xlattice.util;

import junit.framework.*;

/**
 * @author Jim Dixon
 */

public class TestDecimalVersion extends TestCase {

	private DecimalVersion dv;
	private DecimalVersion dv2;
	private DecimalVersion dv3;

	public TestDecimalVersion(String name) {
		super(name);
	}
	public void setUp() {
		dv = null;
	}
	public void testEmpty() {
		String nullString = null;
		try {
			dv = new DecimalVersion(nullString);
			fail("didn't catch null version string");
		} catch (IllegalArgumentException iae) { /* success */ }
		try {
			dv = new DecimalVersion("");
			fail("didn't catch empty version string");
		} catch (IllegalArgumentException iae) { /* success */ }
		try {
			dv = new DecimalVersion("  \t  ");
			fail("didn't catch whitespace-only version string");
		} catch (IllegalArgumentException iae) { /* success */ }
	}

	public void test4IntConstructor() {
		dv = new DecimalVersion(1,2,3,4);
		String s = dv.toString();
		assertEquals("1.2.3.4", s);
		assertEquals(dv.getA(), 1);
		assertEquals(dv.getB(), 2);
		assertEquals(dv.getC(), 3);
		assertEquals(dv.getD(), 4);
		dv2 = new DecimalVersion(s);
		assertTrue(dv.equals(dv2));
	}
	public void test3IntConstructor() {
		dv = new DecimalVersion(1,2,3);
		String s = dv.toString();
		assertEquals("1.2.3", s);
		assertEquals(dv.getA(), 1);
		assertEquals(dv.getB(), 2);
		assertEquals(dv.getC(), 3);
		assertEquals(dv.getD(), 0);
		dv2 = new DecimalVersion(s);
		assertTrue(dv.equals(dv2));
		dv3 = new DecimalVersion(1,2,3,0);
		assertTrue(dv.equals(dv3));
	}
	public void test2IntConstructor() {
		dv = new DecimalVersion(1,2);
		String s = dv.toString();
		assertEquals("1.2", s);
		assertEquals(dv.getA(), 1);
		assertEquals(dv.getB(), 2);
		assertEquals(dv.getC(), 0);
		assertEquals(dv.getD(), 0);
		dv2 = new DecimalVersion(s);
		assertTrue(dv.equals(dv2));
		dv3 = new DecimalVersion(1,2,0,0);
		assertTrue(dv.equals(dv3));
	}
	public void test1IntConstructor() {
		dv = new DecimalVersion(1);
		String s = dv.toString();
		assertEquals("1.0", s);
		assertEquals(dv.getA(), 1);
		assertEquals(dv.getB(), 0);
		assertEquals(dv.getC(), 0);
		assertEquals(dv.getD(), 0);
		dv2 = new DecimalVersion(s);
		assertTrue(dv.equals(dv2));
		dv3 = new DecimalVersion(1,0,0,0);
		assertTrue(dv.equals(dv3));
	}
}

//func (s *XLSuite) makeVersion(a, b, c, d uint) (dv DecimalVersion) {
//	return DecimalVersion(uint((0xff & a) |
//		((0xff & b) << 8) |
//		((0xff & c) << 16) |
//		((0xff & d) << 24)))
//}
//func (s *XLSuite) TestDecimalVersion(c *C) {
//	if VERBOSITY > 0 {
//		fmt.Println("TEST_DECIMAL_VERSION")
//	}
//	rng := xr.MakeSimpleRNG()
//
//	_ = rng
//
//	// always print at least two decimals
//	dv := s.makeVersion(1, 0, 0, 0)
//	v := dv.String()
//	c.Assert(v, Equals, "1.0")
//	dv2, err := ParseDecimalVersion(v)
//	c.Assert(err, IsNil)
//	c.Assert(dv2, Equals, dv)
//
//	// don't print more if the values are zero
//	dv = s.makeVersion(1, 2, 0, 0)
//	v = dv.String()
//	c.Assert(v, Equals, "1.2")
//	dv2, err = ParseDecimalVersion(v)
//	c.Assert(err, IsNil)
//	c.Assert(dv2, Equals, dv)
//
//	// if the third byte is zero but the fourth isn't, print
//	// both
//	dv = s.makeVersion(1, 2, 0, 4)
//	v = dv.String()
//	c.Assert(v, Equals, "1.2.0.4")
//	dv2, err = ParseDecimalVersion(v)
//	c.Assert(err, IsNil)
//	c.Assert(dv2, Equals, dv)
//
//	// other cases
//	dv = s.makeVersion(1, 2, 3, 0)
//	v = dv.String()
//	c.Assert(v, Equals, "1.2.3")
//	dv2, err = ParseDecimalVersion(v)
//	c.Assert(err, IsNil)
//	c.Assert(dv2, Equals, dv)
//
//	dv = s.makeVersion(1, 2, 3, 4)
//	v = dv.String()
//	c.Assert(v, Equals, "1.2.3.4")
//	dv2, err = ParseDecimalVersion(v)
//	c.Assert(err, IsNil)
//	c.Assert(dv2, Equals, dv)
//
//	for i := 0; i < 8; i++ {
//		n := rng.Uint32()
//		dv := DecimalVersion(n)
//		v = dv.String()
//		dv2, err := ParseDecimalVersion(v)
//		c.Assert(err, IsNil)
//		c.Assert(dv2, Equals, dv)
//	}
//}
