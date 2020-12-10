package eu.hoefel.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.function.Function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Maths} convenience methods.
 * 
 * @author Udo Hoefel
 */
@SuppressWarnings("javadoc")
final class MathsTests {

	@DisplayName("Testing matrix inverse")
	@Test
	void testMatrixInverse() {
		double[][] m = {{2,1,3},{1,3,-3},{-2,4,4}};
		assertArrayEquals(new double[][] {{0.3,0.1,-0.15},{1./40,7./40,9./80},{1./8,-1./8,1./16}}, Maths.inverse(m));
	}

	@DisplayName("Testing approximately equal")
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
	
	@DisplayName("Testing Fornberg weights")
	@Test
	void testFornbergWeights() {
		double[] grid = {-2,-1,0,1,2};
		double[][] expected = {{0,0,1,0,0}, {0.08333333333333333,-0.6666666666666666,0,0.6666666666666666,-0.08333333333333333}};
		
		double[][] weights = Maths.fornbergWeights(0, grid, 1);
		double[] m0Weights = { weights[0][0], weights[1][0], weights[2][0], weights[3][0], weights[4][0] };
		double[] m1Weights = { weights[0][1], weights[1][1], weights[2][1], weights[3][1], weights[4][1] };
		
		// test m=0
		assertArrayEquals(expected[0], m0Weights, 1e-16);
		
		// test m=1
		assertArrayEquals(expected[1], m1Weights, 1e-16);
	}

	@DisplayName("Testing partial derivatives")
	@Test
	void testPartialDerivatives() {
		Function<double[], double[]> f = x -> {
			double[] ret = new double[3];
			ret[0] = x[0] * Math.sin(x[1]) * Math.cos(x[2]);
			ret[1] = x[0] * Math.sin(x[1]) * Math.sin(x[2]);
			ret[2] = Math.tan(x[2]);
			return ret;
		};
		
		double[] position = {1,2,3};
		
		double[] pd_0 = Maths.partialDerivatives(f, 1, position, 0);
		double[] pd_1 = Maths.partialDerivatives(f, 1, position, 1);
		double[] pd_2 = Maths.partialDerivatives(f, 1, position, 2);
		
		double[] expected_pd_0 = {-0.90019762973, 0.1283200602, 0};
		double[] expected_pd_1 = {0.41198224566, -0.05872664492, 0};
		double[] expected_pd_2 = {-0.1283200602, -0.90019762973, 1.0203195169424269};
		
		assertArrayEquals(expected_pd_0, pd_0, 5e-9);
		assertArrayEquals(expected_pd_1, pd_1, 5e-9);
		assertArrayEquals(expected_pd_2, pd_2, 5e-9);
	}
}
