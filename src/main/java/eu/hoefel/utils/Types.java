package eu.hoefel.utils;

import java.lang.reflect.Array;

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
	 * <code>arrayType(double.class, 1)</code> yields <code>double[].class</code><br>
	 * <code>arrayType(String[].class, 1)</code> yields <code>String[][].class</code>
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
		return Array.newInstance(elementType,new int[dimension]).getClass();
	}

	/**
	 * Box the object, if it is a primitive (array).
	 * 
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
		     if (clazz == double.class)  return Double.class;
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
	         if (clazz == Double.class)    return double.class;
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
	 * Checks whether the arg can be narrowed to the paramClass.
	 * 
	 * @param paramClass the class to check if arg can be narrowed to
	 * @param arg        the argument
	 * @return true if arg can be narrowed to paramClass
	 */
	public static final boolean canBeNarrowed(Class<?> paramClass, Object arg) {
		Class<?> boxedClass = Types.boxedClass(paramClass);
		return     (arg instanceof Short s      && boxedClass == Byte.class      && s >= Byte.MIN_VALUE      && s <= Byte.MAX_VALUE)
				|| (arg instanceof Short s      && boxedClass == Character.class && s >= Character.MIN_VALUE && s <= Character.MAX_VALUE)
				|| (arg instanceof Character  c && boxedClass == Byte.class      && c >= Byte.MIN_VALUE      && c <= Byte.MAX_VALUE)
				|| (arg instanceof Character  c && boxedClass == Short.class     && c >= Short.MIN_VALUE     && c <= Short.MAX_VALUE)
				|| (arg instanceof Integer i    && boxedClass == Byte.class      && i >= Byte.MIN_VALUE      && i <= Byte.MAX_VALUE)
				|| (arg instanceof Integer i    && boxedClass == Short.class     && i >= Short.MIN_VALUE     && i <= Short.MAX_VALUE)
				|| (arg instanceof Integer i    && boxedClass == Character.class && i >= Character.MIN_VALUE && i <= Character.MAX_VALUE)
				|| (arg instanceof Long l       && boxedClass == Byte.class      && l >= Byte.MIN_VALUE      && l <= Byte.MAX_VALUE)
				|| (arg instanceof Long l       && boxedClass == Short.class     && l >= Short.MIN_VALUE     && l <= Short.MAX_VALUE)
				|| (arg instanceof Long l       && boxedClass == Character.class && l >= Character.MIN_VALUE && l <= Character.MAX_VALUE)
				|| (arg instanceof Long l       && boxedClass == Integer.class   && l >= Integer.MIN_VALUE   && l <= Integer.MAX_VALUE)
				|| (arg instanceof Float f      && boxedClass == Byte.class      && f >= Byte.MIN_VALUE      && f <= Byte.MAX_VALUE)
				|| (arg instanceof Float f      && boxedClass == Short.class     && f >= Short.MIN_VALUE     && f <= Short.MAX_VALUE)
				|| (arg instanceof Float f      && boxedClass == Character.class && f >= Character.MIN_VALUE && f <= Character.MAX_VALUE)
				|| (arg instanceof Float f      && boxedClass == Integer.class   && f >= Integer.MIN_VALUE   && f <= Integer.MAX_VALUE)
				|| (arg instanceof Float f      && boxedClass == Long.class      && f >= Long.MIN_VALUE      && f <= Long.MAX_VALUE)
				|| (arg instanceof Double d     && boxedClass == Byte.class      && d >= Byte.MIN_VALUE      && d <= Byte.MAX_VALUE)
				|| (arg instanceof Double d     && boxedClass == Short.class     && d >= Short.MIN_VALUE     && d <= Short.MAX_VALUE)
				|| (arg instanceof Double d     && boxedClass == Character.class && d >= Character.MIN_VALUE && d <= Character.MAX_VALUE)
				|| (arg instanceof Double d     && boxedClass == Integer.class   && d >= Integer.MIN_VALUE   && d <= Integer.MAX_VALUE)
				|| (arg instanceof Double d     && boxedClass == Long.class      && d >= Long.MIN_VALUE      && d <= Long.MAX_VALUE)
				|| (arg instanceof Double d     && boxedClass == Float.class     && d >= Float.MIN_VALUE     && d <= Float.MAX_VALUE);
	}

	/**
	 * Checks whether arg can be widened to the paramClass.
	 * 
	 * @param paramClass the class to check if arg can be widened to
	 * @param arg        the argument
	 * @return true if arg can be widened to paramClass
	 */
	public static final boolean canBeWidened(Class<?> paramClass, Object arg) {
		Class<?> boxedClass = Types.boxedClass(paramClass);
		return     (arg instanceof Float     &&  boxedClass == Double.class) 
				|| (arg instanceof Long      && (boxedClass == Double.class || boxedClass == Float.class))
				|| (arg instanceof Integer   && (boxedClass == Double.class || boxedClass == Float.class || boxedClass == Long.class))
				|| (arg instanceof Character && (boxedClass == Double.class || boxedClass == Float.class || boxedClass == Long.class || boxedClass == Integer.class))
				|| (arg instanceof Short     && (boxedClass == Double.class || boxedClass == Float.class || boxedClass == Long.class || boxedClass == Integer.class))
				|| (arg instanceof Byte      && (boxedClass == Double.class || boxedClass == Float.class || boxedClass == Long.class || boxedClass == Integer.class || boxedClass == Short.class));
	}

	/**
	 * Checks whether the two classes are compatible. This does not do narrowing/widening checks!
	 * 
	 * @param clazz1 the first class
	 * @param clazz2 the second class
	 * @return true if the given classes are compatible
	 */
	public static final boolean isCompatible(Class<?> clazz1, Class<?> clazz2) {
		return (clazz1 == clazz2 || clazz1.isAssignableFrom(clazz2) || Types.boxedClass(clazz1).isAssignableFrom(clazz2));
	}
}
