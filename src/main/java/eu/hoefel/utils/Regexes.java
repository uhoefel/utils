package eu.hoefel.utils;

import java.util.regex.Pattern;

/**
 * Compiled patterns that are helpful in increasing performance in e.g. the
 * eu.hoefel.units library.
 * 
 * @author Udo Hoefel
 */
public final class Regexes {

	/** Hiding any public constructor. */
	private Regexes() {
	    throw new IllegalStateException("This is a pure utility class!");
	}

	/** Compiled regex pattern for splitting at "^". */
	public static final Pattern EXPONENT_SPLITTER = Pattern.compile("\\^");

	/** Compiled regex pattern for splitting at spaces. */
	public static final Pattern SPACE_SPLITTER = Pattern.compile("\\s+");

	/**
	 * Compiled regex pattern for splitting at "e". Useful e.g. for splitting
	 * numbers in scientific notation.
	 */
	public static final Pattern E_SPLITTER = Pattern.compile("e");

	/** See the documentation of {@link Double#valueOf(String)}. */
	private static final String IS_FLOATING_NUMBER_REGEXP = "[\\x00-\\x20]*[+-]?(((((\\p{Digit}+)(\\.)?((\\p{Digit}+)?)([eE][+-]?(\\p{Digit}+))?)|(\\.((\\p{Digit}+))([eE][+-]?(\\p{Digit}+))?)|(((0[xX](\\p{XDigit}+)(\\.)?)|(0[xX](\\p{XDigit}+)?(\\.)(\\p{XDigit}+)))[pP][+-]?(\\p{Digit}+)))[fFdD]?))[\\x00-\\x20]*";

	/** Compiled regex pattern for identifying floating point numbers. */
	static final Pattern IS_FLOATING_NUMBER = Pattern.compile(IS_FLOATING_NUMBER_REGEXP);
}
