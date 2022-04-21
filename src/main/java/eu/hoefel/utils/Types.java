package eu.hoefel.utils;

import java.lang.reflect.Array;
import java.util.Objects;

/**
 * Convenience methods with respect to types.
 * 
 * @author Udo Hoefel
 */
public final class Types {

    /** Hiding any public constructor. */
    private Types() {
        throw new IllegalStateException("This is a pure utility class!");
    }

    /**
     * Gets the class of an element (i.e. the component type of the corresponding
     * lowest dimensional array type) of the given class if the given class is an
     * array. If the given class is not an array the given class is returned as-is.
     * <p>
     * Example code:
     * <p>
     * <code>elementType(double[][].class)</code> will yield
     * <code>double.class</code><br>
     * <code>elementType(String.class)</code> will yield <code>String.class</code>
     * 
     * @param clazz the class to get the element type for
     * @return the element type
     */
    public static final Class<?> elementType(Class<?> clazz) {
        if (clazz.isArray()) {
            return elementType(clazz.getComponentType());
        }

        return clazz;
    }

    /**
     * Gets the (array) dimension of the given class.
     * <p>
     * Example code:
     * <p>
     * <code>elementType(double[][].class)</code> will yield <code>2</code>
     * <code>elementType(String.class)</code> will yield <code>0</code>
     * 
     * @param clazz the class to get the (array) dimension for
     * @return the array dimension, or 0 if not an array
     */
    public static final int dimension(Class<?> clazz) {
        if (clazz.isArray()) {
            return dimension(clazz.getComponentType()) + 1;
        }

        return 0;
    }

    /**
     * Returns the class of an array of the elements of the given type. e.g.
     * <p>
     * <code>arrayType(double.class, 1)</code> yields
     * <code>double[].class</code><br>
     * <code>arrayType(String[].class, 2)</code> yields
     * <code>String[][][].class</code>
     * <p>
     * 
     * @param elementType the element type of the array class to be created
     * @param dimension   the dimension
     * @return the class of the array
     */
    public static Class<?> arrayType(Class<?> elementType, int dimension) {
        if (dimension == 0) {
            return elementType;
        }
        return Array.newInstance(elementType, new int[dimension]).getClass();
    }

    /**
     * Box the object, if it is a primitive (array).
     * 
     * @param <T>    the type of the boxed object, so e.g. if object is of type
     *               int[], the returned object will be of type Integer[]
     * @param object the object to be boxed if it is a primitive (array)
     * @return the (boxed) object
     */
    @SuppressWarnings("unchecked")
    public static final <T> T box(Object object) {
        if (object.getClass().isArray()) {
            int dimension = Types.dimension(object.getClass());
            Class<?> elementType = Types.elementType(object.getClass());
            if (elementType.isPrimitive()) {
                elementType = boxedClass(elementType);
            } else {
                return (T) object;
            }
            Class<?> componentType = arrayType(elementType, dimension - 1);
            int length = Array.getLength(object);
            Object ret = Array.newInstance(componentType, length);
            for (int i = 0; i < length; i++) {
                Array.set(ret, i, box(Array.get(object, i)));
            }
            return (T) ret;
        }
        return (T) object;
    }

    /**
     * Unbox the object if it is a primitive array. Unboxing single primitive values
     * does not work, because generics don't work with primitives.
     * 
     * @param <T>    the type of the unboxed object, so e.g. if object is of type
     *               Integer[], the returned object will be of type int[]
     * @param object the object that is unboxed if it is a primitive array
     * @return the (unboxed) object
     */
    @SuppressWarnings("unchecked")
    public static final <T> T unbox(Object object) {
        if (object.getClass().isArray()) {
            int dimension = Types.dimension(object.getClass());
            Class<?> elementType = Types.elementType(object.getClass());
            if (!elementType.isPrimitive()) {
                elementType = unboxedClass(elementType);
            } else {
                return (T) object;
            }
            Class<?> componentType = arrayType(elementType, dimension - 1);
            int length = Array.getLength(object);
            Object ret = Array.newInstance(componentType, length);
            for (int i = 0; i < length; i++) {
                Array.set(ret, i, unbox(Array.get(object, i)));
            }
            return (T) ret;
        }
        return (T) object;
    }

    /**
     * Gets the class that can be used to box primitives. For other classes returns
     * just the given class.
     * 
     * @param clazz the class to determine the corresponding boxing class
     * @return the corresponding boxing class
     */
    public static final Class<?> boxedClass(Class<?> clazz) {
        if      (clazz == double.class)  return Double.class;
        else if (clazz == int.class)     return Integer.class;
        else if (clazz == long.class)    return Long.class;
        else if (clazz == boolean.class) return Boolean.class;
        else if (clazz == float.class)   return Float.class;
        else if (clazz == byte.class)    return Byte.class;
        else if (clazz == short.class)   return Short.class;
        else if (clazz == char.class)    return Character.class;
        else if (clazz == void.class)    return Void.class;
        else                             return clazz;
    }

    /**
     * Gets the class that can be used to unbox boxed primitives. For other classes
     * returns just the given class.
     * 
     * @param clazz the class to determine the corresponding primitive class
     * @return the corresponding primitive class
     */
    public static final Class<?> unboxedClass(Class<?> clazz) {
        if      (clazz == Double.class)    return double.class;
        else if (clazz == Integer.class)   return int.class;
        else if (clazz == Long.class)      return long.class;
        else if (clazz == Boolean.class)   return boolean.class;
        else if (clazz == Float.class)     return float.class;
        else if (clazz == Byte.class)      return byte.class;
        else if (clazz == Short.class)     return short.class;
        else if (clazz == Character.class) return char.class;
        else if (clazz ==  Void.class)     return void.class;
        else                               return clazz;
    }

    /**
     * Checks whether the {@code arg} can be (primitively, or with their wrapper
     * classes) narrowed down to {@code clazz}. This check does check whether
     * {@code arg} is within range of the class to narrow down to (e.g. whether a
     * {@code double} is within range of {@code float}), <em>which is a deviation of
     * the behavior specified in the Java language specification</em> (which also
     * allows values outside the narrowed range, but may put them at another value,
     * e.g. infinity). Even if this check returns true the corresponding narrowing
     * conversion may still lead to a loss in precision.
     * 
     * @param clazz the class to check if {@code arg} can be narrowed to
     * @param arg   the argument
     * @return true if {@code arg} can be narrowed to {@code clazz}
     */
    public static final boolean canBeNarrowed(Class<?> clazz, Object arg) {
        if (clazz == null || arg == null) {
            return false;
        }

        Class<?> boxedClass = Types.boxedClass(clazz);
        return     (arg instanceof Short s1     && boxedClass == Byte.class      && s1 >= Byte.MIN_VALUE      && s1 <= Byte.MAX_VALUE)
                || (arg instanceof Short s2     && boxedClass == Character.class && s2 >= Character.MIN_VALUE && s2 <= Character.MAX_VALUE)
                || (arg instanceof Character c1 && boxedClass == Byte.class      && c1 >= Byte.MIN_VALUE      && c1 <= Byte.MAX_VALUE)
                || (arg instanceof Character c2 && boxedClass == Short.class     && c2 >= Short.MIN_VALUE     && c2 <= Short.MAX_VALUE)
                || (arg instanceof Integer i1   && boxedClass == Byte.class      && i1 >= Byte.MIN_VALUE      && i1 <= Byte.MAX_VALUE)
                || (arg instanceof Integer i2   && boxedClass == Short.class     && i2 >= Short.MIN_VALUE     && i2 <= Short.MAX_VALUE)
                || (arg instanceof Integer i3   && boxedClass == Character.class && i3 >= Character.MIN_VALUE && i3 <= Character.MAX_VALUE)
                || (arg instanceof Long l1      && boxedClass == Byte.class      && l1 >= Byte.MIN_VALUE      && l1 <= Byte.MAX_VALUE)
                || (arg instanceof Long l2      && boxedClass == Short.class     && l2 >= Short.MIN_VALUE     && l2 <= Short.MAX_VALUE)
                || (arg instanceof Long l3      && boxedClass == Character.class && l3 >= Character.MIN_VALUE && l3 <= Character.MAX_VALUE)
                || (arg instanceof Long l4      && boxedClass == Integer.class   && l4 >= Integer.MIN_VALUE   && l4 <= Integer.MAX_VALUE)
                || (arg instanceof Float f1     && boxedClass == Byte.class      && f1 >= Byte.MIN_VALUE      && f1 <= Byte.MAX_VALUE)
                || (arg instanceof Float f2     && boxedClass == Short.class     && f2 >= Short.MIN_VALUE     && f2 <= Short.MAX_VALUE)
                || (arg instanceof Float f3     && boxedClass == Character.class && f3 >= Character.MIN_VALUE && f3 <= Character.MAX_VALUE)
                || (arg instanceof Float f4     && boxedClass == Integer.class   && f4 >= Integer.MIN_VALUE   && f4 <= Integer.MAX_VALUE)
                || (arg instanceof Float f5     && boxedClass == Long.class      && f5 >= Long.MIN_VALUE      && f5 <= Long.MAX_VALUE)
                || (arg instanceof Double d1    && boxedClass == Byte.class      && d1 >= Byte.MIN_VALUE      && d1 <= Byte.MAX_VALUE)
                || (arg instanceof Double d2    && boxedClass == Short.class     && d2 >= Short.MIN_VALUE     && d2 <= Short.MAX_VALUE)
                || (arg instanceof Double d3    && boxedClass == Character.class && d3 >= Character.MIN_VALUE && d3 <= Character.MAX_VALUE)
                || (arg instanceof Double d4    && boxedClass == Integer.class   && d4 >= Integer.MIN_VALUE   && d4 <= Integer.MAX_VALUE)
                || (arg instanceof Double d5    && boxedClass == Long.class      && d5 >= Long.MIN_VALUE      && d5 <= Long.MAX_VALUE)
                || (arg instanceof Double d6    && boxedClass == Float.class     && d6 >= Float.MIN_VALUE     && d6 <= Float.MAX_VALUE);
    }

    /**
     * Checks whether {@code arg} can be widened to {@code clazz}, including
     * reference widening operations.
     * 
     * @param clazz the class to check if {@code arg} can be widened to
     * @param arg   the argument
     * @return true if {@code arg} can be widened to {@code clazz}
     */
    public static final boolean canBeWidened(Class<?> clazz, Object arg) {
        if (clazz == null) {
            return false;
        }

        Class<?> boxedClass = Types.boxedClass(clazz);
        return     (arg instanceof Float     &&  boxedClass == Double.class) 
                || (arg instanceof Long      && (boxedClass == Double.class || boxedClass == Float.class))
                || (arg instanceof Integer   && (boxedClass == Double.class || boxedClass == Float.class || boxedClass == Long.class))
                || (arg instanceof Character && (boxedClass == Double.class || boxedClass == Float.class || boxedClass == Long.class || boxedClass == Integer.class))
                || (arg instanceof Short     && (boxedClass == Double.class || boxedClass == Float.class || boxedClass == Long.class || boxedClass == Integer.class))
                || (arg instanceof Byte      && (boxedClass == Double.class || boxedClass == Float.class || boxedClass == Long.class || boxedClass == Integer.class || boxedClass == Short.class))
                || (arg != null && clazz.isAssignableFrom(arg.getClass()))
                || (arg == null && !clazz.isPrimitive());
    }

    /**
     * Checks whether the two classes are compatible. This does do
     * narrowing/widening checks!
     * 
     * @param clazz the class
     * @param arg   the argument to check if it could be used (incl. autoboxing,
     *              auto-widening and -narrowing conversions) for a parameter of
     *              class {@code clazz}
     * @return true if the given classes are compatible
     */
    public static final boolean isCompatible(Class<?> clazz, Object arg) {
        return (arg != null && boxedClass(clazz) == boxedClass(arg.getClass())) || canBeWidened(clazz, arg)
                || canBeNarrowed(clazz, arg);
    }

    /**
     * Return the java {@link java.lang.Class} object with the specified class name.
     * This is an "extended" {@link java.lang.Class#forName(java.lang.String)}
     * operation.
     * <ul>
     * <li>It is able to return Class objects for primitive types (including arrays)
     * <li>Classes in name space `java.lang` do not need the fully qualified name
     * <li>It does not throw a checked Exception
     * </ul>
     * 
     * @param className the class name, may not be null
     * @return the class corresponding to the given class name
     * @throws IllegalArgumentException if no class can be found
     * @see <a href="https://stackoverflow.com/a/45660967/5599820">stackoverflow
     *      inspiration</a>
     */
    public static final Class<?> classForName(String className) {
        Objects.requireNonNull(className);

        var typeInfo = decomposeTypeInfo(className);

        var elementClass = switch (typeInfo.elementType()) {
            case "boolean" -> boolean.class;
            case "byte"    -> byte.class;
            case "short"   -> short.class;
            case "int"     -> int.class;
            case "long"    -> long.class;
            case "float"   -> float.class;
            case "double"  -> double.class;
            case "char"    -> char.class;
            case "void"    -> void.class;
            default -> parseNonprimitiveType(typeInfo.elementType());
        };

        Class<?> targetClass = elementClass;
        for (int i = 0; i < typeInfo.dim(); i++) {
            targetClass = targetClass.arrayType();
        }
        return targetClass;
    }

    /**
     * Holds decomposed type info, e.g. for a class name "int[]" this record would
     * hold an {@code elementType} "int" and a {@code dim} of 1.
     * 
     * @param elementType the element type of a type, may not be null
     * @param dim         the dimension of array part of a type (may be 0 if not an
     *                    array). May not be negative.
     * @author Udo Hoefel
     */
    private static record DecomposedTypeInfo(String elementType, int dim) {

        /**
         * Constructs a new record for holding information about a type.
         * 
         * @param elementType the element type of a type, may not be null
         * @param dim         the dimension of array part of a type (may be 0 if not an
         *                    array). May not be negative.
         * @throws NullPointerException     if elementType is null
         * @throws IllegalArgumentException if element type is blank or the dimension is
         *                                  negative
         */
        public DecomposedTypeInfo {
            Objects.requireNonNull(elementType);

            if (elementType.isBlank()) {
                throw new IllegalArgumentException("Element type may not be blank!");
            }

            if (dim < 0) {
                throw new IllegalArgumentException("Array dimension needs to be at least 0 (for non-array types)!");
            }
        }
    }

    /**
     * Decomposes the given class name into {@link DecomposedTypeInfo}, providing
     * information about the element type and the array dimension.
     * 
     * @param className the class name, may not be null
     * @return the decomposed type info
     */
    private static DecomposedTypeInfo decomposeTypeInfo(String className) {
        int startArrayInfo = className.indexOf('[');
        if (startArrayInfo == -1) {
            return new DecomposedTypeInfo(className, 0);
        }

        String arrayPart = className.substring(startArrayInfo);
        int dim = 0;
        for (int i = 0; i < arrayPart.length(); i++) {
            if (arrayPart.charAt(i) == '[') {
                dim++;
            }
        }

        return new DecomposedTypeInfo(className.substring(0, startArrayInfo), dim);
    }

    /**
     * Parses a non-primitive type to its corresponding class.
     * 
     * @param className the name of the class. May not be null. Classes in package
     *                  "java.lang" do not need the class name to be fully
     *                  qualified.
     * @return the class corresponding to the given type
     */
    private static Class<?> parseNonprimitiveType(String className) {
        String fqn = className.contains(".") ? className : "java.lang.".concat(className);
        try {
            return Class.forName(fqn);
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException("Class not found: " + fqn);
        }
    }
}
