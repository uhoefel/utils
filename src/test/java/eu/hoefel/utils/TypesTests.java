package eu.hoefel.utils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test the convenience methods in {@link Types}.
 * 
 * @author Udo Hoefel
 */
@SuppressWarnings("javadoc")
final class TypesTests {

    @DisplayName("Testing array type calculation")
    @Test
    void testArrayType() {
        assertEquals(Object[][][].class, Types.arrayType(Object.class, 3));
        assertEquals(double[].class, Types.arrayType(double.class, 1));
        assertEquals(Object.class, Types.arrayType(Object.class, 0));
        assertEquals(Boolean[][].class, Types.arrayType(Boolean.class, 2));
        assertEquals(float[][][][].class, Types.arrayType(float[][].class, 2));
        assertEquals(int.class, Types.arrayType(int.class, 0));
    }

    @DisplayName("Testing getting the array dimensions")
    @Test
    void testGetArrayDimensions() {
        assertEquals(3, Types.dimension(new Object[0][0][0].getClass()));
        assertEquals(3, Types.dimension(new double[1][2][3].getClass()));
        assertEquals(1, Types.dimension(new int[0].getClass()));
        assertEquals(0, Types.dimension(Double.valueOf(3).getClass()));
    }

    @DisplayName("Boxing/Unboxing of primitives")
    @Test
    void testUnBox() {
        double val1 = 3.0;
        double[] val2 = {1.0, 2.0};
        boolean val3 = true;

        Double box1 = Types.box(val1);
        double unbox1 = Types.unbox(box1);
        Double[] box2 = Types.box(val2);
        double[] unbox2 = Types.unbox(box2);
        Boolean box3 = Types.box(val3);
        boolean unbox3 = Types.unbox(box3);

        assert(box1.getClass().equals(Double.class));
        assert(box2.getClass().equals(Double[].class));
        assert(unbox2.getClass().equals(double[].class));
        assert(box3.getClass().equals(Boolean.class));

        assertEquals(val1, box1.doubleValue());
        assertEquals(val1, unbox1);
        assertArrayEquals(val2, unbox2);
        assertEquals(val3, box3.booleanValue());
        assertEquals(val3, unbox3);
    }

    @DisplayName("Testing extended classForName")
    @Test
    void testClassforName() {
        assertEquals(int.class, Types.classForName("int"));
        assertEquals(Types.class, Types.classForName("eu.hoefel.utils.Types"));
        assertEquals(Types[][][][].class, Types.classForName("eu.hoefel.utils.Types[][][][]"));
        assertEquals(float[][].class, Types.classForName("float[][]"));
        assertEquals(Character.class, Types.classForName("Character"));
        assertEquals(Double[].class, Types.classForName("Double[]"));
    }
}
