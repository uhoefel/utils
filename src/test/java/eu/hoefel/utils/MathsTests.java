package eu.hoefel.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Maths} convenience methods.
 * 
 * @author Udo Hoefel
 */
@SuppressWarnings("javadoc")
final class MathsTests {

	@Test
	void testApproxEqual() {
		assertTrue(Maths.approximates(Double.NaN, Double.NaN, 0.0));
		assertTrue(Maths.approximates(Double.POSITIVE_INFINITY, 0.0, Double.POSITIVE_INFINITY));
		assertFalse(Maths.approximates(3, 5, 1));
		assertTrue(Maths.approximates(3, 5, 2));
		assertFalse(Maths.approximates(1, Math.nextUp(1.0), 0.0));
		assertTrue(Maths.approximates(1, Math.nextUp(1.0), Math.nextUp(1.0) - 1));
	}
	
	@DisplayName("Testing compensated sum")
	@Test
	void testCompensatedSum() {
		int n = 1002;
		double[] x = new double[n];
		Arrays.fill(x, 2e-18);
		x[0] = -1e100;
		x[1] = 1e100;
		x[2] = 1.0;
		assertEquals(1.0 + 2e-15, Maths.compensatedSum(x));
	}

	@DisplayName("Testing area cosinus hyperbolicus")
	@Test
	final void testArcosh() {
		assertEquals(0, Maths.arcosh(1));
		assertEquals(1.3169578969248166, Maths.arcosh(2));
	}

	@DisplayName("Testing oddness")
	@Test
	void testOddness() {
		assertTrue(Maths.isOdd(1));
		assertTrue(Maths.isOdd(-13));
		assertFalse(Maths.isOdd(0));
		assertFalse(Maths.isOdd(-2));
		assertFalse(Maths.isOdd(Long.MIN_VALUE));
		assertTrue(Maths.isOdd(2000001));
		assertTrue(Maths.isOdd(Long.MAX_VALUE));
	}

	@DisplayName("Testing evenness")
	@Test
	void testEvenness() {
		assertTrue(Maths.isEven(12));
		assertTrue(Maths.isEven(-6));
		assertTrue(Maths.isEven(0));
		assertTrue(Maths.isEven(-2656));
		assertTrue(Maths.isEven(Long.MIN_VALUE));
		assertFalse(Maths.isEven(233));
		assertFalse(Maths.isEven(Long.MAX_VALUE));
	}
}