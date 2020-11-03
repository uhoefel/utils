package eu.hoefel.utils;

import java.lang.reflect.Array;

import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;

/**
 * Math related convenience methods.
 * 
 * @author Udo Hoefel
 */
public final class Maths {

	/** Hiding any public constructor. */
	private Maths() {
	    throw new IllegalStateException("This is a pure utility class!");
	}

	/**
	 * Java's maximum array size is slightly smaller than {@link Integer#MAX_VALUE}
	 * due to bytes reserved for the array header. This constant holds the "real"
	 * maximum array size.
	 */
	public static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 2;

	// we represent the borders and special values as strings, we need them for the
	// isFloat(...) etc. checks below

	// byte related min/max values
	private static final String MIN_BYTE = Byte.toString(Byte.MIN_VALUE);
	private static final String MAX_BYTE = Byte.toString(Byte.MAX_VALUE);

	// short related min/max values
	private static final String MIN_SHORT = Short.toString(Short.MIN_VALUE);
	private static final String MAX_SHORT = Short.toString(Short.MAX_VALUE);

	// int related min/max values
	private static final String MIN_INT = Integer.toString(Integer.MIN_VALUE);
	private static final String MAX_INT = Integer.toString(Integer.MAX_VALUE);

	// long related min/max values
	private static final String MIN_LONG = Long.toString(Long.MIN_VALUE);
	private static final String MAX_LONG = Long.toString(Long.MAX_VALUE);

	// float related min/max/special values
	private static final String FLOAT_NAN = Float.toString(Float.NaN);
	private static final String FLOAT_NEG_INF = Float.toString(Float.NEGATIVE_INFINITY);
	private static final String FLOAT_POS_INF = Float.toString(Float.POSITIVE_INFINITY);

	// double min/max/special values
	private static final String DOUBLE_NAN = Double.toString(Double.NaN);
	private static final String DOUBLE_NEG_INF = Double.toString(Double.NEGATIVE_INFINITY);
	private static final String DOUBLE_POS_INF = Double.toString(Double.POSITIVE_INFINITY);

	/** All factorials from 0! to 20!. The factorial of 21 does not fit inside a long.  */
	private static final long[] FACTORIAL = { 1, 1, 2, 6, 24, 120, 720, 5_040, 40_320, 362_880, 3_628_800, 39_916_800,
			479_001_600, 6_227_020_800L, 87_178_291_200L, 1_307_674_368_000L, 20_922_789_888_000L, 355_687_428_096_000L,
			6_402_373_705_728_000L, 121_645_100_408_832_000L, 2_432_902_008_176_640_000L };

	/** The Levi-Civita symbol for 3D. */
	private static final int[][][] LEVI_CIVITA_3D = {{{0,0, 0}, { 0,0,1}, {0,-1,0}},
													 {{0,0,-1}, { 0,0,0}, {1, 0,0}},
													 {{0,1, 0}, {-1,0,0}, {0, 0,0}}};

	/**
	 * Checks whether a string can be parsed to a float.
	 * 
	 * @param s the string to be checked
	 * @return true if parsable, otherwise false
	 */
	public static boolean isFloat(String s) {
		if (s == null) return false;

		// no whitespaces
		s = s.trim();

		// note that we do not use isBlank, as we already have trimmed the whitespace
		// anyway and can avoid some overhead that way
		if (s.isEmpty()) {
			return false;
		} else if (FLOAT_NAN.equals(s) || FLOAT_NEG_INF.equals(s) || FLOAT_POS_INF.equals(s)) {
			return true;
		}
		return Regexes.IS_FLOATING_NUMBER.matcher(s).matches();
	}

	/**
	 * Checks whether a string can be parsed to a double.
	 * 
	 * @param s the string to be checked
	 * @return true if parsable, otherwise false
	 */
	public static boolean isDouble(String s) {
		if (s == null) return false;

		// no whitespaces
		s = s.trim();

		// note that we do not use isBlank, as we already have trimmed the whitespace
		// anyway and can avoid some overhead that way
		if (s.isEmpty()) {
			return false;
		} else if (DOUBLE_NAN.equals(s) || DOUBLE_NEG_INF.equals(s) || DOUBLE_POS_INF.equals(s)) {
			return true;
		}
		return Regexes.IS_FLOATING_NUMBER.matcher(s).matches();
	}

	/**
	 * Checks whether a string can be parsed to a boolean.
	 * 
	 * @param string the string to be checked
	 * @return true if parsable, otherwise false
	 */
	public static final boolean isBoolean(String string) {
		return "true".equalsIgnoreCase(string) || "false".equalsIgnoreCase(string);
	}

	/**
	 * Checks whether a string can be parsed to a character.
	 * 
	 * @param string the string to be checked
	 * @return true if parsable, otherwise false
	 */
	public static final boolean isChar(String string) {
		return string != null && string.length() == 1;
	}

	/**
	 * Checks whether a string represents a valid byte within
	 * [{@link Byte#MIN_VALUE},{@link Byte#MAX_VALUE}].
	 * 
	 * @param s the string to be checked
	 * @return true if parsable, otherwise false
	 */
	public static final boolean isByte(String s) {
		return isNaturalNumberWithinLimit(s, MIN_BYTE, MAX_BYTE);
	}

	/**
	 * Checks whether a string represents a valid natural number within
	 * [{@link Short#MIN_VALUE},{@link Short#MAX_VALUE}].
	 * 
	 * @param s the string to be checked
	 * @return true if parsable, otherwise false
	 */
	public static final boolean isShort(String s) {
		return isNaturalNumberWithinLimit(s, MIN_SHORT, MAX_SHORT);
	}

	/**
	 * Checks whether a string represents a valid natural number within
	 * [{@link Integer#MIN_VALUE},{@link Integer#MAX_VALUE}].
	 * 
	 * @param s the string to be checked
	 * @return true if parsable, otherwise false
	 */
	public static final boolean isInteger(String s) {
		return isNaturalNumberWithinLimit(s, MIN_INT, MAX_INT);
	}

	/**
	 * Checks whether a string represents a valid natural number within
	 * [{@link Long#MIN_VALUE},{@link Long#MAX_VALUE}].
	 * 
	 * @param s the string to be checked
	 * @return true if parsable, otherwise false
	 */
	public static final boolean isLong(String s) {
		return isNaturalNumberWithinLimit(s, MIN_LONG, MAX_LONG);
	}

	/**
	 * Checks (very fast) whether a string is a natural number within given limits.
	 * Note that the minimum needs to be at least one character longer than the
	 * maximum (as the minimum needs a "-", do not use the "+" for the positive
	 * maximum). This method is mainly intended for the {@link #isShort},
	 * {@link #isInteger} and {@link #isLong} checks. Other usecases may be
	 * possible, but are untested. If you change this method, make sure that is not
	 * slower than before, as it is crucial for some high-throughput parsing applications. As a
	 * reference see <a href=
	 * "https://stackoverflow.com/questions/237159/whats-the-best-way-to-check-if-a-string-represents-an-integer-in-java">stackoverflow</a>.
	 * 
	 * @param s   the string to check
	 * @param min the minimum as a String, should be at least one char longer than
	 *            {@code max} due to the prepended "-"
	 * @param max the maximum as a String, should be at least one char shorter than
	 *            {@code min} as it should not have a prepended "+"
	 * @return true if the string is a natural number within the given limits
	 */
	private static final boolean isNaturalNumberWithinLimit(String s, final String min, final String max) {
		if (s == null) return false;

		// no whitespaces
		s = s.trim();

		if (s.isEmpty()) return false;

		int len = s.length();

		// The bottom limit of the range is min which is min.length() chars long.
		// [Note that the upper limit (max) is only max.length() chars long (should be 1
		// char shorter)]. Thus any string with more than min.length() chars, even if it
		// represents a valid number, won't fit into the specified range.
		if (len > min.length()) return false;

		char c = s.charAt(0);
		int i = 0;
		// A plus sign is fine, so e.g. "+11" will return true if within the limits.
		if (c == '-' || c == '+') {
			// A single "+" or "-" is not a valid number.
			if (len == 1) return false;
			i = 1;
		}
		// Check if all chars are digits
		for (; i < len; i++) {
			c = s.charAt(i);
			if (c < '0' || c > '9') return false;
		}
		// By reaching this point we know for sure that the string has at most
		// min.length() chars and that they are all digits (with the potential exception
		// of the first one, which might be a '+' or '-' though). Next we check, for
		// max.length() and min.length() char long strings, if the numbers represented
		// by the them do not surpass the given limits.
		c = s.charAt(0);
		char l;
		String limit = max;
		if (len == min.length() - 1 && c != '-' && c != '+') {
			// Now we are going to compare each char of the String with the char in
			// the limit string that has the same index, so if the string is "ABC" and
			// the limit string is "DEF" then we are going to compare A to D, B to E and so
			// on. c is the current string's char and l is the corresponding limit's char
			// Note that the loop only continues if c == l. Now imagine (for using limits
			// for ints) that our String is "2150000000", 2 == 2 (next), 1 == 1 (next), 5 >
			// 4 and as you can see, because 5 > 4 we can guarantee that the string will
			// represent a bigger integer. Similarly, if our string was "2139999999", when
			// we find out that 3 < 4, we can also guarantee that the number represented
			// will fit in an int.
			for (i = 0; i < len; i++) {
				c = s.charAt(i);
				l = limit.charAt(i);
				if (c > l) return false;
				if (c < l) return true;
			}
		}

		c = s.charAt(0);
		if (len == min.length()) {
			// If the first char is neither '+' nor '-' then min.length() digits represent a
			// bigger integer than max. Remember that max.length() digits is equal to
			// (min.length()-1) digits.
			if (c != '+' && c != '-') return false;

			limit = (c == '-') ? min : "+" + max;
			// Here, the same logic that applied in the previous case is used, except for
			// ignoring the first char.
			for (i = 1; i < len; i++) {
				c = s.charAt(i);
				l = limit.charAt(i);
				if (c > l) return false;
				if (c < l) return true;
			}
		}

		// The given string passed all tests, implying that it must represent a number
		// that fits within the specified limits
		return true;
	}

	/**
	 * Checks whether two values are approximately equal. Should be able to handle
	 * NaNs, and negative and positive infinities.
	 * 
	 * @param a   the first value
	 * @param b   the value to compare against
	 * @param tol the tolerance (absolute)
	 * @return true if equal within tolerance
	 */
	public static final boolean approximates(final double a, final double b, final double tol) {
		return Math.abs(a - b) <= tol || Double.valueOf(a).equals(Double.valueOf(b));
	}

	/**
	 * Calculates the inverse hyperbolic sine of {@code x}.
	 * 
	 * @param x the argument
	 * @return the inverse hyperbolic sine
	 */
	public static final double arsinh(double x) {
		if (x < 0) return -arsinh(-x);
		// k is the number of significant decimal numbers, so for 64 bit double it is 16
		// the size check then needs 10^(k/2)
		int k = 100_000_000;
		if (x < 0.125) {
			// Taylor expansion
			return x - Math.pow(x, 3) / 6 + 3 * Math.pow(x, 5) / 40 - 15 * Math.pow(x, 7) / 336;
		} else if (x > k) {
			return Math.log(2) + Math.log(x);
		} else {
			return Math.log(x + Math.sqrt(x * x - 1));
		}
	}

	/**
	 * Calculates the inverse cotangent of {@code x}.
	 * 
	 * @param x the argument
	 * @return the inverse cotangent
	 */
	public static final double arcosh(double x) {
		// k is the number of significant decimal numbers, so for 64 bit double it is 16
		// the size check then needs 10^(k/2)
		int k = 100_000_000;
		if (x < 1) {
			// undefined
			return Double.NaN;
		} else if (x > k) {
			return Math.log(2) + Math.log(x);
		} else {
			return Math.log(x + Math.sqrt(x * x - 1));
		}
	}

	/**
	 * Swaps {@code i} and {@code j} in {@code a} (hence no return).
	 * 
	 * @param a the array in which to swap the indices
	 * @param i the index of which the corresponding value is to be copied to index
	 *          {@code j}
	 * @param j the index of which the corresponding value is to be copied to index
	 *          {@code i}
	 */
	public static final void swap(boolean[] a, int i, int j) {
		boolean temp = a[i];
	    a[i] = a[j];
	    a[j] = temp;
	}

	/**
	 * Swaps {@code i} and {@code j} in {@code a} (hence no return).
	 * 
	 * @param a the array in which to swap the indices
	 * @param i the index of which the corresponding value is to be copied to index
	 *          {@code j}
	 * @param j the index of which the corresponding value is to be copied to index
	 *          {@code i}
	 */
	public static final void swap(byte[] a, int i, int j) {
		byte temp = a[i];
	    a[i] = a[j];
	    a[j] = temp;
	}

	/**
	 * Swaps {@code i} and {@code j} in {@code a} (hence no return).
	 * 
	 * @param a the array in which to swap the indices
	 * @param i the index of which the corresponding value is to be copied to index
	 *          {@code j}
	 * @param j the index of which the corresponding value is to be copied to index
	 *          {@code i}
	 */
	public static final void swap(short[] a, int i, int j) {
		short temp = a[i];
	    a[i] = a[j];
	    a[j] = temp;
	}

	/**
	 * Swaps {@code i} and {@code j} in {@code a} (hence no return).
	 * 
	 * @param a the array in which to swap the indices
	 * @param i the index of which the corresponding value is to be copied to index
	 *          {@code j}
	 * @param j the index of which the corresponding value is to be copied to index
	 *          {@code i}
	 */
	public static final void swap(int[] a, int i, int j) {
		int temp = a[i];
	    a[i] = a[j];
	    a[j] = temp;
	}

	/**
	 * Swaps {@code i} and {@code j} in {@code a} (hence no return).
	 * 
	 * @param a the array in which to swap the indices
	 * @param i the index of which the corresponding value is to be copied to index
	 *          {@code j}
	 * @param j the index of which the corresponding value is to be copied to index
	 *          {@code i}
	 */
	public static final void swap(long[] a, int i, int j) {
		long temp = a[i];
	    a[i] = a[j];
	    a[j] = temp;
	}

	/**
	 * Swaps {@code i} and {@code j} in {@code a} (hence no return).
	 * 
	 * @param a the array in which to swap the indices
	 * @param i the index of which the corresponding value is to be copied to index
	 *          {@code j}
	 * @param j the index of which the corresponding value is to be copied to index
	 *          {@code i}
	 */
	public static final void swap(float[] a, int i, int j) {
		float temp = a[i];
	    a[i] = a[j];
	    a[j] = temp;
	}

	/**
	 * Swaps {@code i} and {@code j} in {@code a} (hence no return).
	 * 
	 * @param a the array in which to swap the indices
	 * @param i the index of which the corresponding value is to be copied to index
	 *          {@code j}
	 * @param j the index of which the corresponding value is to be copied to index
	 *          {@code i}
	 */
	public static final void swap(double[] a, int i, int j) {
		double temp = a[i];
	    a[i] = a[j];
	    a[j] = temp;
	}

	/**
	 * Swaps {@code i} and {@code j} in {@code a} (hence no return).
	 * 
	 * @param a the array in which to swap the indices
	 * @param i the index of which the corresponding value is to be copied to index
	 *          {@code j}
	 * @param j the index of which the corresponding value is to be copied to index
	 *          {@code i}
	 */
	public static final void swap(char[] a, int i, int j) {
		char temp = a[i];
	    a[i] = a[j];
	    a[j] = temp;
	}

	/**
	 * Swaps {@code i} and {@code j} in {@code a} (hence no return).
	 * 
	 * @param a the array in which to swap the indices
	 * @param i the index of which the corresponding value is to be copied to index
	 *          {@code j}
	 * @param j the index of which the corresponding value is to be copied to index
	 *          {@code i}
	 */
	public static final <T> void swap(T[] a, int i, int j) {
		T temp = a[i];
	    a[i] = a[j];
	    a[j] = temp;
	}

	/**
	 * Calculates the compensated sum, such that magnitudes varying over a wide
	 * range do not produce as large errors due to limited accuracy.
	 * 
	 * @param a     the array to sum
	 * @return the compensated sum
	 * 
	 * @see <a href="https://doi.org/10.1007/s00607-005-0139-x">A Generalized
	 *      Kahan-Babuška-Summation-Algorithm</a>
	 */
	public static final double compensatedSum(double... a) {
		return compensatedSum(a, 2);
	}

	/**
	 * Calculates the compensated sum, such that magnitudes varying over a wide
	 * range do not produce as large errors due to limited accuracy.
	 * 
	 * @param a     the array to sum
	 * @param order the order of the correction (does not seem to have an effect for
	 *              order&gt;2 - I tried to generalize it to arbitrary dimensions,
	 *              potentially I made a mistake somewhere)
	 * @return the compensated sum
	 * 
	 * @see <a href="https://doi.org/10.1007/s00607-005-0139-x">A Generalized
	 *      Kahan-Babuška-Summation-Algorithm</a>
	 */
	private static final double compensatedSum(double[] a, int order) {
		double[] summands = new double[order + 1];
		for (int i = 0; i < a.length; i++) {
			// we need at least first order
			double[] firstOrder = compensatedSumPart(summands[0], a[i]);
			summands[0] = firstOrder[0];

			final double[] tmp = { firstOrder[1], order == 1 ? 0 : summands[2] };
			for (int j = 1; j < order; j++) {
				double[] higherOrder = compensatedSumPart(tmp[1], tmp[0]);
				summands[j] += higherOrder[0];
				tmp[0] = higherOrder[1];
				tmp[1] = summands[j + 1];
			}

			summands[summands.length - 1] += tmp[0];
		}

		return sum(summands);
	}

	/**
	 * Calculates a part of the compensated sum in the
	 * Kahan-Babuška-Summation-Algorithm.
	 * 
	 * @param v1 the "lower" order input (data, 1st order corrections, ...)
	 * @param v2 the "higher" order input (1st order corrections, 2nd order
	 *           corrections, ...)
	 * @return the sum of v1 and v2 in the first index, the correction in the second
	 *         index
	 */
	private static final double[] compensatedSumPart(double v1, double v2) {
		double[] ret = { v1 + v2, 0 };
		if (Math.abs(v1) >= Math.abs(v2)) {
			ret[1] = (v1 - ret[0]) + v2;
		} else {
			ret[1] = (v2 - ret[0]) + v1;
		}
		return ret;
	}

	/**
	 * Sums the elements of an array.
	 * 
	 * @param a the array
	 * @return the sum of the elements of a
	 */
	public static final double sum(double[] a) {
		double ret = 0;
		for (int i = 0; i < a.length; i++) {
			ret += a[i];
		}
		return ret;
	}

	/**
	 * Calculates the determinant of the given matrix.
	 * 
	 * @param m the matrix. Needs to be a square matrix.
	 * @return the determinant
	 */
	public static double determinant(double[][] m) {
		if (m.length != m[0].length) {
			throw new IllegalArgumentException("Determinant can only be calculates for square matrices! "
					+ "The given matrix was of size (%d,%d).".formatted(m.length, m[0].length));
		}

		return switch (m.length) {
			case 1 -> m[0][0];
			case 2 -> m[0][0] * m[1][1] - m[0][1] * m[1][0];
			case 3 -> m[0][0] * (m[1][1] * m[2][2] - m[2][1] * m[1][2]) - m[0][1] * (m[1][0] * m[2][2] - m[2][0] * m[1][2])
					+ m[0][2] * (m[1][0] * m[2][1] - m[2][0] * m[1][1]);
			case 4 -> m[0][3] * m[1][2] * m[2][1] * m[3][0] - m[0][2] * m[1][3] * m[2][1] * m[3][0]
					- m[0][3] * m[1][1] * m[2][2] * m[3][0] + m[0][1] * m[1][3] * m[2][2] * m[3][0]
					+ m[0][2] * m[1][1] * m[2][3] * m[3][0] - m[0][1] * m[1][2] * m[2][3] * m[3][0]
					- m[0][3] * m[1][2] * m[2][0] * m[3][1] + m[0][2] * m[1][3] * m[2][0] * m[3][1]
					+ m[0][3] * m[1][0] * m[2][2] * m[3][1] - m[0][0] * m[1][3] * m[2][2] * m[3][1]
					- m[0][2] * m[1][0] * m[2][3] * m[3][1] + m[0][0] * m[1][2] * m[2][3] * m[3][1]
					+ m[0][3] * m[1][1] * m[2][0] * m[3][2] - m[0][1] * m[1][3] * m[2][0] * m[3][2]
					- m[0][3] * m[1][0] * m[2][1] * m[3][2] + m[0][0] * m[1][3] * m[2][1] * m[3][2]
					+ m[0][1] * m[1][0] * m[2][3] * m[3][2] - m[0][0] * m[1][1] * m[2][3] * m[3][2]
					- m[0][2] * m[1][1] * m[2][0] * m[3][3] + m[0][1] * m[1][2] * m[2][0] * m[3][3]
					+ m[0][2] * m[1][0] * m[2][1] * m[3][3] - m[0][0] * m[1][2] * m[2][1] * m[3][3]
					- m[0][1] * m[1][0] * m[2][2] * m[3][3] + m[0][0] * m[1][1] * m[2][2] * m[3][3];
			default -> new LUDecomposition(new BlockRealMatrix(m)).getDeterminant();
		};
	}

	/**
	 * Checks whether the given number is even.
	 * 
	 * @param number the number
	 * @return true if even
	 */
	public static final boolean isEven(long number) {
		// Zero all the bits but leave the least significant bit unchanged and check if
		// the result is 0. This should be a bit faster than the modulo operator.
		return (number & 1) == 0;
	}

	/**
	 * Checks whether the given number is odd.
	 * 
	 * @param number the number
	 * @return true if odd
	 */
	public static final boolean isOdd(long number) {
		return !isEven(number);
	}

	/**
	 * Gets the Levi Civita value at the given indices. Works for arbitrary
	 * dimensions (within the array size limit).
	 * 
	 * @param indices the indices (starting at 1, not 0)
	 * @return the Levi Civita value, i.e. either 1, -1, or 0
	 */
	public static final int leviCivita(int... indices) {
		// As a reference see https://github.com/JuliaLang/julia/pull/10313/files
		// this code follows the logic of the above

		if (indices.length == 3) {
			boolean valid = indices[0] > 0 && indices[0] <= 3 && indices[1] > 0 && indices[1] <= 3 && indices[2] > 0 && indices[2] <= 3;
			return valid ? LEVI_CIVITA_3D[indices[0] - 1][indices[1] - 1][indices[2] - 1] : 0;
		}

		boolean[] todo = new boolean[indices.length];
		for (int i = 0; i < todo.length; i++) {
			todo[i] = true;
		}

		int current = 0;
		int cycles = 0;
		int flips = 0;

		while (cycles + flips < indices.length) {
			current = findNextTrueIndex(todo, current);
			if (todo[current] ^= true) return 0;
			int j = indices[current] - 1;
			if (j < 0 || j >= indices.length) return 0;
			cycles += 1;
			while (j != current) {
				if (todo[j] ^= true) return 0;
				j = indices[j] - 1;
				if (j < 0 || j >= indices.length) return 0;
				flips += 1;
			}
		}

		return isEven(flips) ? 1 : -1;
	}

	/**
	 * Finds the next index for which the value in the array is true, starting at
	 * the defined {@code startIndex}.
	 * 
	 * @param a          the array
	 * @param startIndex the start index
	 * @return the index of the next true
	 */
	private static final int findNextTrueIndex(boolean[] a, int startIndex) {
		for (int i = startIndex; i < a.length; i++) {
			if (a[i]) return i;
		}
		return 0;
	}

	/**
	 * Gets the factorial for the given number.
	 * 
	 * @param n the number to factorialize. Needs to be &gt;=0 and &lt;21
	 * @return the faculty of n
	 */
	public static final long factorial(int n) {
		if (n < 0) {
			throw new UnsupportedOperationException("You requested the factorial of a negative number (%d). "
					+ "This is not supported.".formatted(n));
		} else if (n > 20) {
			throw new IllegalArgumentException("You requested the factorial of %d, "
					+ "which would yield a result too large to hold even in a long.".formatted(n));
		}

		return FACTORIAL[n];
	}

	/**
	 * Calculates all permutations of the given numbers. Do note that if duplicates
	 * occur they are treated as if they were different numbers, i.e. uniqueness is
	 * not checked. This will be slow for values > 9 as the number of permutations
	 * "explodes".
	 * 
	 * @param numbers the numbers to find the permutations of (typically, you will
	 *                want to have each number occur only once to ensure that there
	 *                are no "duplicate" permutations)
	 * @return the permutations
	 * 
	 * @see <a href="https://doi.org/10.1093/comjnl/6.3.293">Permutations by
	 *      Interchanges (Heap's algorithm)</a>
	 * @see <a href="https://doi.org/10.1145/356689.356692">Permutation Generation
	 *      Methods (Sedgewick's review)</a>
	 */
	public static final int[][] permutations(int... numbers) {
		if (numbers == null || numbers.length == 0) {
			return new int[0][0];
		} else if (numbers.length == 1) {
			return new int[][] { numbers };
		} else if (numbers.length == 2) {
			return new int[][] { { numbers[0], numbers[1] }, { numbers[1], numbers[0] } };
		} else if (numbers.length == 3) {
			return new int[][] {
					{ numbers[0], numbers[1], numbers[2] }, { numbers[0], numbers[2], numbers[1] },
					{ numbers[2], numbers[0], numbers[1] }, { numbers[2], numbers[1], numbers[0] },
					{ numbers[1], numbers[2], numbers[0] }, { numbers[1], numbers[0], numbers[2] } };
		}

		int n = numbers.length;
		int[] c = new int[n];
		long factorial = factorial(n);
		if (factorial > MAX_ARRAY_SIZE) {
			throw new IllegalArgumentException("The number of permutations (" + n + "!) exceeds the maximum array size!");
		}
		int[][] ret = new int[(int) factorial][n];
		ret[0] = numbers.clone();
		int[] a = numbers.clone();
		
		int i = 0;
		int index = 1;
		while (i < n) {
			if (c[i] < i) {
				if (isEven(i)) {
					swap(a, 0, i);
				} else {
					swap(a, c[i], i);
				}
				ret[index++] = a.clone();
				c[i]++;
				i = 0;
			} else {
				c[i++] = 0;
			}
		}
		return ret;
	}

	/**
	 * Calculates all permutations of the given objects. Do note that if duplicates
	 * occur they are treated as if they were different objects, i.e. uniqueness is
	 * not checked. This will be slow for values > 9 as the number of permutations
	 * "explodes".
	 * 
	 * @param objects the objects to find the permutations of (typically, you will
	 *                want to have each object occur only once to ensure that there
	 *                are no "duplicate" permutations)
	 * @return the permutations
	 * 
	 * @see <a href="https://doi.org/10.1093/comjnl/6.3.293">Permutations by
	 *      Interchanges (Heap's algorithm)</a>
	 * @see <a href="https://doi.org/10.1145/356689.356692">Permutation Generation
	 *      Methods (Sedgewick's review)</a>
	 */
	@SuppressWarnings("unchecked")
	public static final <T> T[][] permutations(T... objects) {
		if (objects == null) {
			throw new IllegalArgumentException("Cannot create permutations of null.");
		} else if (objects.length == 0) {
			return (T[][]) Array.newInstance(objects.getClass().getComponentType(), 0, 0);
		}
		int n = objects.length;
		int[][] indices = permutations(linspace(0, n - 1, 1));
		long factorial = (int) factorial(n);
		if (factorial > MAX_ARRAY_SIZE) {
			throw new IllegalArgumentException("The number of permutations (" + n + "!) exceeds the maximum array size!");
		}
		T[][] ret = (T[][]) Array.newInstance(objects.getClass().getComponentType(), (int) factorial, n);
		for (int i = 0; i < ret.length; i++) {
			for (int j = 0; j < ret[i].length; j++) {
				ret[i][j] = objects[indices[i][j]];
			}
		}
		return ret;
	}

	/**
	 * Return evenly spaced numbers over a specified interval.
	 * 
	 * @param x0 the starting value of the sequence
	 * @param x1 the end value of the sequence (inclusive)
	 * @param dx the stepsize
	 * @return evenly spaced numbers ranging from {@code x0} to {@code x1}
	 */
	private static final int[] linspace(int x0, int x1, int dx) {
		int n = (x1 - x0) / dx + 1;		
		int[] f = new int[n];
		for (int i = 0; i < n; i++) {
			f[i] = x0 + i * dx;
		}
		return f;
	}

	/**
	 * Gets a deep copy for the given primitive array.
	 * 
	 * @param <T> the type of the primitive array
	 * @param array the array to copy
	 * @return a deep-copy of the primitive array
	 */
	@SuppressWarnings("unchecked")
	public static final <T> T deepCopyPrimitiveArray(T array) {
		if (array == null) {
			throw new IllegalArgumentException("null is not an array");
		} else {
			Class<?> c = Types.elementType(array.getClass());
			if (c.isPrimitive()) {
				int dim = Types.dimension(array.getClass());
				if (dim == 1) {
					if (array instanceof double[] d) {
						return (T) d.clone();
					} else if (array instanceof int[] integer) {
						return (T) integer.clone();
					} else if (array instanceof long[] lo) {
						return (T) lo.clone();
					} else if (array instanceof boolean[] b) {
						return (T) b.clone();
					} else if (array instanceof float[] f) {
						return (T) f.clone();
					} else if (array instanceof short[] s) {
						return (T) s.clone();
					} else if (array instanceof char[] ch) {
						return (T) ch.clone();
					} else {
						// has to be byte[]
						return (T) ((byte[]) array).clone();
					}
				} else {
					int len = Array.getLength(array);
					T ret = (T) Array.newInstance(array.getClass().getComponentType(), len);
					for (int i = 0; i < len; i++) {
						Array.set(ret, i, deepCopyPrimitiveArray(Array.get(array, i)));
					}
					return ret;
				}
			} else {
				throw new IllegalArgumentException("%s is not a primitive array".formatted(array.getClass().getSimpleName()));
			}
		}
	}
}
