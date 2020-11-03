/**
 * This module provides all kinds of utility classes.
 * <p>
 * The {@link eu.hoefel.utils} is the package containing the utility classes.
 * 
 * @author Udo Hoefel
 */
module eu.hoefel.jatex {
	exports eu.hoefel.utils;
	
	opens eu.hoefel.utils to org.junit.platform.commons;

	requires java.logging;
	requires commons.math3;
}