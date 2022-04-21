package eu.hoefel.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Strings}.
 * 
 * @author Udo Hoefel
 */
@SuppressWarnings("javadoc")
final class StringsTests {

    @DisplayName("Testing ordinal numeral")
    @Test
    void testOrdinalNumeral() {
        assertEquals("1st", Strings.ordinalNumeral(1));
        assertEquals("2nd", Strings.ordinalNumeral(2));
        assertEquals("3rd", Strings.ordinalNumeral(3));
        assertEquals("4th", Strings.ordinalNumeral(4));
        assertEquals("5th", Strings.ordinalNumeral(5));
        assertEquals("6th", Strings.ordinalNumeral(6));
        assertEquals("7th", Strings.ordinalNumeral(7));
        assertEquals("8th", Strings.ordinalNumeral(8));
        assertEquals("9th", Strings.ordinalNumeral(9));
        assertEquals("10th", Strings.ordinalNumeral(10));
        assertEquals("11th", Strings.ordinalNumeral(11));
        assertEquals("12th", Strings.ordinalNumeral(12));
        assertEquals("13th", Strings.ordinalNumeral(13));
        assertEquals("14th", Strings.ordinalNumeral(14));
        assertEquals("15th", Strings.ordinalNumeral(15));
        assertEquals("22nd", Strings.ordinalNumeral(22));
    }

    @DisplayName("Testing capitalizing")
    @Test
    void testCapitalize() {
        assertEquals("Car", Strings.capitalize("car"));
        assertEquals("Car", Strings.capitalize("Car"));
        assertThrows(NullPointerException.class, () -> Strings.capitalize(null));
    }

    @DisplayName("Testing trimming")
    @Test
    void testTrimming() {
        assertEquals("bla", Strings.trim("bla"));
        assertEquals("bsbk32412f", Strings.trim("   bsbk32412f "));
        assertEquals("2543754", Strings.trim(" 2543754 "));
        assertEquals("gsdfg32765", Strings.trim("  gsdfg32765"));
        assertEquals("131345fjkg", Strings.trim("131345fjkg "));
        assertEquals("131345fjkg", Strings.trim("131345fjkg "));
        assertEquals("131345fjkg", Strings.trim("131345fjkg "));

        assertEquals("131345fjkg", Strings.trim("131345fjkg " + Strings.NON_BREAKABLE_SPACE));
        assertEquals("131345fjkg", Strings.trim("131345fjkg " + Strings.SMALL_NON_BREAKABLE_SPACE));
        assertEquals("131345fjkg", Strings.trim("131345fjkg " + Strings.SMALL_NON_BREAKABLE_SPACE + Strings.NON_BREAKABLE_SPACE));

        String input = "131345fjkg " + Strings.SMALL_NON_BREAKABLE_SPACE;
        assertEquals(input, input.trim());
    }
}
