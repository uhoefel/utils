/**
 * This module provides all kinds of utility classes.
 * <p>
 * The {@link eu.hoefel.utils} is the package containing the utility classes.
 * 
 * @author Udo Hoefel
 */
module eu.hoefel.utils {
    exports eu.hoefel.utils;

    opens eu.hoefel.utils to org.junit.platform.commons;

    requires static org.junit.jupiter.api;
}