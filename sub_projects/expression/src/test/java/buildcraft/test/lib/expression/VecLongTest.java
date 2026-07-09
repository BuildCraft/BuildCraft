package buildcraft.test.lib.expression;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import buildcraft.lib.expression.VecLong;

public class VecLongTest {
    // VecLong does not override equals(), so compare its public fields directly.
    private static void assertVec(long ea, long eb, long ec, long ed, VecLong actual) {
        assertEquals("field a", ea, actual.a);
        assertEquals("field b", eb, actual.b);
        assertEquals("field c", ec, actual.c);
        assertEquals("field d", ed, actual.d);
    }

    @Test
    public void testConstructorDefaults() {
        assertVec(1, 0, 0, 0, new VecLong(1));
        assertVec(1, 2, 0, 0, new VecLong(1, 2));
        assertVec(1, 2, 3, 0, new VecLong(1, 2, 3));
        assertVec(0, 0, 0, 0, VecLong.ZERO);
    }

    @Test
    public void testAdd() {
        VecLong a = new VecLong(1, 2, 3, 4);
        assertVec(11, 22, 33, 44, a.add(10, 20, 30, 40));
        // immutability: original unchanged
        assertVec(1, 2, 3, 4, a);
        assertVec(11, 22, 33, 44, a.add(new VecLong(10, 20, 30, 40)));
    }

    @Test
    public void testSub() {
        VecLong a = new VecLong(10, 20, 30, 40);
        assertVec(9, 18, 27, 36, a.sub(1, 2, 3, 4));
        assertVec(9, 18, 27, 36, a.sub(new VecLong(1, 2, 3, 4)));
    }

    @Test
    public void testScaleAndDiv() {
        VecLong a = new VecLong(2, 3, 4, 5);
        assertVec(4, 6, 8, 10, a.scale(2, 2, 2, 2));
        assertVec(4, 6, 8, 10, a.scale(new VecLong(2, 2, 2, 2)));
        assertVec(1, 1, 2, 2, a.div(2, 3, 2, 2));
        assertVec(1, 1, 2, 2, a.div(new VecLong(2, 3, 2, 2)));
    }

    @Test
    public void testDotProducts() {
        VecLong a = new VecLong(1, 2, 3, 4);
        VecLong b = new VecLong(5, 6, 7, 8);
        assertEquals(1 * 5 + 2 * 6, a.dotProduct2(b));
        assertEquals(1 * 5 + 2 * 6 + 3 * 7, a.dotProduct3(b));
        assertEquals(1 * 5 + 2 * 6 + 3 * 7 + 4 * 8, a.dotProduct4(b));
    }

    @Test
    public void testCrossProduct() {
        VecLong a = new VecLong(1, 0, 0, 0);
        VecLong b = new VecLong(0, 1, 0, 0);
        assertVec(0, 0, 1, 1, a.crossProduct(b));
    }

    @Test
    public void testLengthAndDistance() {
        VecLong a = new VecLong(3, 4, 0, 0);
        assertEquals(5.0, a.length(), 0.0001);
        assertEquals(5.0, a.distance(new VecLong(0, 0, 0, 0)), 0.0001);
        assertEquals(0.0, a.distance(a), 0.0001);
    }

    @Test
    public void testToString() {
        assertEquals("{ 1, 2, 3, 4 }", new VecLong(1, 2, 3, 4).toString());
        assertEquals("{ 0, 0, 0, 0 }", VecLong.ZERO.toString());
    }

    @Test
    public void testNegativeValues() {
        VecLong a = new VecLong(-1, -2, -3, -4);
        assertVec(1, 2, 3, 4, a.add(new VecLong(2, 4, 6, 8)));
        // length() is always non-negative (Euclidean magnitude)
        assertEquals(Math.sqrt(30), a.length(), 0.0001);
    }
}
