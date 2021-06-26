package eu.hoefel.utils;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Class for all kind of reflection utils.
 * 
 * @author Udo Hoefel
 */
public final class Reflections {

	/** The maximum number of parameters in an executable. */
	public static final int MAX_NUM_EXECUTABLE_PARAMETERS = 255;

	/**
	 * Allows to determine which kind of corresponding executable should be picked
	 * for the {@link MethodHandle} or {@link Constructor}.
	 * 
	 * @author Udo Hoefel
	 */
	public enum SignatureType {
		/** Picks the most specific, narrow executable that matches. */
		MOST_SPECIFIC,
		
		/** Picks the most generic, wide executable that matches. */
		MOST_GENERIC,
		
		/** Picks any matching executable, nondeterministic. */
		ANY
	}

	/** Hiding any public constructor. */
	private Reflections() {
		throw new IllegalStateException("This is a pure utility class!");
	}

	/**
	 * Gets the most specific constructor matching the given arguments for the given
	 * class and creates a new instance.
	 * 
	 * @param <T>   the type of which you want a new instance
	 * @param clazz the class of which you want a new instance
	 * @param args  the arguments to pass on to the constructor
	 * @return the new instance
	 */
	public static final <T> T newInstance(Class<T> clazz, Object... args) {
		return newInstance(SignatureType.MOST_SPECIFIC, clazz, args);
	}

	/**
	 * Gets the (most specific/generic/any) constructor matching the given arguments
	 * for the given class and creates a new instance.
	 * 
	 * @param <T>   the type of which you want a new instance
	 * @param type  the type of parameter signature aimed for
	 * @param clazz the class of which you want a new instance
	 * @param args  the arguments to pass on to the constructor
	 * @return the new instance
	 */
	public static final <T> T newInstance(SignatureType type, Class<T> clazz, Object... args) {
		Constructor<T> constructor = getConstructor(type, clazz, args);

		// put args in right format -> varargs are problematic, we need to collect them
		// in one object
		boolean singleVarArg = constructor.getParameterCount() == 1 && constructor.getParameters()[0].isVarArgs();
		
		try {
			if (singleVarArg) {
				return constructor.newInstance((Object) args); // the cast is important!
			} else {
				return constructor.newInstance(reshapeArgsForConstructor(constructor, args));
			}
		} catch (InstantiationException e) {
			throw new IllegalArgumentException(
					"%s seems to be an abstract class, which cannot be instantiated.".formatted(clazz.getSimpleName()),
					e);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException(("The found constructor for %s seems to be enforcing Java language "
					+ "access control, making it inaccesible.").formatted(clazz.getSimpleName()), e);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(("The found constructor for %s seems to have i) a different amount "
					+ "of formal parameters (which should not occur as we check this) or ii) a problem unwrapping "
					+ "a primitive (should not occur, we check compatibility including null conversion beforehand),"
					+ "or iii) if the constructor is for an enum, or iv) if (potentially after unwrapping) an "
					+ "argument cannot be converted to its formal constructor parameter type by a method "
					+ "invocation conversion.").formatted(clazz.getSimpleName()), e);
		} catch (InvocationTargetException e) {
			throw new IllegalArgumentException(("The found constructor for %s threw an exception.").formatted(clazz.getSimpleName()), e);
		}
	}

	/**
	 * Reshapes the arguments to be compatible with vararg constructors, i.e. all
	 * arguments for the vararg (potentially several arguments of the given args)
	 * are put in an array of the correct type.
	 * 
	 * @param constructor the constructor
	 * @param args        the given arguments for the constructor. Potentially of
	 *                    different length than the number of parameters of the
	 *                    constructor, as varargs may be used.
	 * @return the objects in the correct shape
	 */
	private static final Object[] reshapeArgsForConstructor(Constructor<?> constructor, Object... args) {
		int numParams = constructor.getParameterCount();
		List<Object> objects = new ArrayList<>();
		for (int i = 0; i < numParams; i++) {
			Parameter p = constructor.getParameters()[i];
			if (p.isVarArgs()) {
				int numVarargEntries = args.length + 1 - numParams;
				var varargs = Array.newInstance(p.getType().getComponentType(), numVarargEntries);
				for (int j = 0; j < numVarargEntries; j++) {
					Array.set(varargs, j, args[j + numParams - 1]);
				}
				objects.add(varargs);
			} else {
				objects.add(args[i]);
			}
		}
		return objects.stream().toArray();
	}

	/**
	 * Gets the constructor for T with the specified signature type (with respect to
	 * the given arguments).
	 * 
	 * @param <T>   the type of which you want the constructor
	 * @param type  the type of signature match the constructor should have
	 * @param clazz the class for which the constructor is the desideratum
	 * @param args  the arguments for the constructor
	 * @return the constructor for T
	 */
	private static final <T> Constructor<T> getConstructor(SignatureType type, Class<T> clazz, Object... args) {
		List<Constructor<T>> matchingConstructors = findPotentiallyMatchingConstructors(clazz, args);
		if (matchingConstructors.isEmpty()) {
			throw new IllegalArgumentException(
					"Found no matching constructor for %s with the given arguments of type %s".formatted(
							clazz.getSimpleName(),
							Stream.of(args)
								  .map(o -> o.getClass().getSimpleName())
								  .toList()));
		}
		
		return switch (type) {
			case MOST_SPECIFIC -> matchingConstructors.get(0);
			case ANY -> matchingConstructors.stream().unordered().findAny().get();
			case MOST_GENERIC -> matchingConstructors.get(matchingConstructors.size() - 1);
		};
	}

	/**
	 * Finds potentially matching constructors.
	 * 
	 * @param <T>   the type of which you want the constructor
	 * @param clazz the class in which to look for constructors
	 * @param args  the arguments for the constructor
	 * @return potentially matching constructors
	 */
	private static final <T> List<Constructor<T>> findPotentiallyMatchingConstructors(Class<T> clazz, Object... args) {
		// for the casting cf the API note of getConstructors
		@SuppressWarnings("unchecked")
		Constructor<T>[] constructors = (Constructor<T>[]) clazz.getConstructors();
		Object[] internalArgs;
		if (args == null) {
			internalArgs = new Object[] { null };
		} else {
			internalArgs = args;
		}
		return Stream.of(constructors)
				.filter(c -> hasCompatibleParameterSignature(c, internalArgs))
				.sorted((e1, e2) -> compareParameterDistance(e1, e2, args))
				.toList();
	}
	
	/**
	 * Checks whether the executable has a compatible (not necessarily identical)
	 * parameter signature.
	 * 
	 * @param m    the executable
	 * @param args the arguments
	 * @return true if the parameter signature is compatible to the arguments
	 */
	private static final boolean hasCompatibleParameterSignature(Executable m, Object[] args) {
		if (m == null) return false;

		Object[] internalArgs = args;
		if (internalArgs == null) internalArgs = new Object[] { null };
		
		Parameter[] params = m.getParameters();
		
		// we do not check the last parameter here as it may be a vararg
		for (int i = 0; i < params.length - 1; i++) {
			if (i >= internalArgs.length || incompatibleTypesWithWidening(params[i], internalArgs[i])) {
				return false;
			}
		}

		Parameter lastParameter = params[params.length - 1];
		if (lastParameter.isVarArgs()) {
			// so we have a vararg there
			Class<?> varargComponent = lastParameter.getType().getComponentType();
			for (int i = params.length - 1; i < internalArgs.length; i++) {
				if (internalArgs[i] == null && varargComponent.isPrimitive()) {
					return false;
				} else if (internalArgs[i] != null 
						&& !Types.isCompatible(varargComponent, internalArgs[i].getClass())) {
					return false;
				}
			}
		} else if (params.length > internalArgs.length) {
			// no varargs -> definitely wrong signature
			return false;
			
		} else {
			for (int i = params.length - 1; i < internalArgs.length; i++) {
				if (incompatibleTypesWithWidening(lastParameter, internalArgs[i])) return false;
			}
		}

		return true;
	}

	/**
	 * Checks whether the given argument is incompatible to the type of the given
	 * parameter, with widening allowed.
	 * 
	 * @param p   the parameter, should be non-null
	 * @param arg the argument, may be null
	 * @return true if the type of the given arg is incompatible with the given
	 *         parameter
	 */
	private static final boolean incompatibleTypesWithWidening(Parameter p, Object arg)  {
		Objects.requireNonNull(p);
		return (arg == null && p.getType().isPrimitive()) // nulls cannot be cast to primitives
				 // if arg is non-null and the types dont match -> incompatible
				// note the small but relevant difference to incompatibleTypesWithoutWidening
				// Types.isCompatible vs. Reflections.isCompatible
				|| (arg != null && !Types.boxedClass(p.getType()).equals(arg.getClass()) && !isCompatible(p, arg));
	}

	/**
	 * Checks whether the parameter is compatible (including widening
	 * conversions) to the given argument.
	 * 
	 * @param parameter the parameter
	 * @param arg       the argument
	 * @return true if compatible
	 */
	private static final boolean isCompatible(Parameter parameter, Object arg) {
		Class<?> paramClass = parameter.getType();
		
		// check widening conversions
		boolean canBeWidened = Types.canBeWidened(paramClass, arg);
		if (canBeWidened) return true;
		
		return Types.isCompatible(paramClass, arg);
	}

	/**
	 * Compares the parameter distance. If the narrowness is considered to be equal
	 * for both executables, a last attempt is done comparing to the given arguments
	 * to see if one matches their type exactly, in which case the executable
	 * matching exactly is considered more narrow.
	 * 
	 * @param m1   the first executable
	 * @param m2   the second executable
	 * @param args the arguments to check in case the narrowness of the executables
	 *             is identical
	 * @return -1 if m is more specific/narrower than m2, 0 if they are equally
	 *         specific/narrow and 1 if m is less specific/wider than m2
	 */
	private static final int compareParameterDistance(Executable m1, Executable m2, Object... args) {
		double score1 = calculateExecutableScore(m1);
		double score2 = calculateExecutableScore(m2);
		
		if (score1 < score2) {
			return -1;
		} else if (score1 == score2) {
			int narrowness = compareNarrowness(m1, m2);
			if (narrowness == 0) {
				// last possibility: either m1 or m2 match exactly
				boolean m1MatchesExactly = hasSameParameterSignature(m1, args);
				boolean m2MatchesExactly = hasSameParameterSignature(m2, args);
				if (m1MatchesExactly && !m2MatchesExactly) {
					return -1;
				} else if (!m1MatchesExactly && m2MatchesExactly) {
					return 1;
				} else {
					throw new IllegalArgumentException("It was not possible to find the most narrow executable, as both "
							+ m1 + " as well as " + m2 + " give the same score for their narrowness and their parameter "
							+ "distance. You will have to call your method/constructor via explicitly given classes.");
				}
			}
			return narrowness;
		} else {
			return 1;
		}
	}
	
	/**
	 * Checks whether the method has the same parameter signature as the given
	 * arguments indicate.
	 * 
	 * @param m    the method
	 * @param args the arguments that should match the executable signature
	 * @return true if the parameter signature and the classes match exactly
	 */
	public static final boolean hasSameParameterSignature(Executable m, Object[] args) {
		if (m == null) return false;

		Object[] internalArgs = args;
		if (internalArgs == null) internalArgs = new Object[] { null };
		
		Parameter[] params = m.getParameters();
		
		// we do not check the last parameter here as it may be a vararg
		for (int i = 0; i < params.length - 1; i++) {
			if (i >= internalArgs.length) {
				// this check is not redundant, as we otherwise check arguments that we do not want to check
			} else if (Types.boxedClass(params[i].getType()) != internalArgs[i].getClass()) {
				return false;
			}
		}

		Parameter lastParameter = params[params.length - 1];
		if (lastParameter.isVarArgs()) {
			// so we have a vararg there
			Class<?> varargComponent = Types.boxedClass(lastParameter.getType().getComponentType());
			for (int i = params.length - 1; i < internalArgs.length; i++) {
				if (varargComponent != internalArgs[i].getClass()) {
					return false;
				}
			}
		} else if (params.length > internalArgs.length) {
			// no varargs -> definitely wrong signature
			return false;
			
		} else {
			for (int i = params.length - 1; i < internalArgs.length; i++) {
				if (Types.boxedClass(params[i].getType()) != internalArgs[i].getClass()) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Compares the narrowness of the given executables.
	 * 
	 * @param m  the first executable
	 * @param m2 the second executable
	 * @return -1 if m is narrower than m2, 0 if they are equally narrow and 1 if m
	 *         is wider than m2
	 */
	private static final int compareNarrowness(Executable m, Executable m2) {
		long score1 = calculateNarrownessScore(m);
		long score2 = calculateNarrownessScore(m2);
		if (score1 < score2) {
			return -1;
		} else if (score1 == score2) {
			return 0;
		} else {
			return 1;
		}
	}

	/**
	 * Calculates the narrowness of an executable. Nonprimitives do not contribute
	 * to the score.
	 * 
	 * @param m the method
	 * @return the score
	 */
	private static final long calculateNarrownessScore(Executable m) {
		Parameter[] params = m.getParameters();
		long score = Long.MAX_VALUE;
		for (int i = 0; i < params.length; i++) {
			Class<?> clazz = Types.elementType(params[i].getType());
			if (clazz.isPrimitive()) {
				int exponent = 0;
				if (clazz == byte.class) {
					exponent = 0; // not necessary, but explicitness is good here
				} else if (clazz == short.class || clazz == char.class) {
					exponent = 1;
				} else if (clazz == int.class) {
					exponent = 2;
				} else if (clazz == long.class) {
					exponent = 3;
				} else if (clazz == float.class) {
					exponent = 4;
				} else if (clazz == double.class) {
					exponent = 5;
				}
				score -= BigDecimal.valueOf(MAX_NUM_EXECUTABLE_PARAMETERS).pow(exponent).longValueExact();
			}
		}
		
		return -score;
	}

	/**
	 * Calculates the score of an executable with respect to its signature.
	 * Lower score corresponds to a more specific executable.
	 * 
	 * @param m the method
	 * @return the score
	 */
	private static final double calculateExecutableScore(Executable m) {
		Parameter[] params = m.getParameters();
		double score = Long.MAX_VALUE;
		final int maxSuperclasses = 50;
		for (int i = 0; i < params.length; i++) {
			if (Types.elementType(params[i].getType()).isPrimitive()) {
				score -= Math.pow(MAX_NUM_EXECUTABLE_PARAMETERS, maxSuperclasses + 1);
			} else {
				int exponent = 0;
				Class<?> paramClass = Types.elementType(params[i].getType());
				while (paramClass.getSuperclass() != null) {
					paramClass = paramClass.getSuperclass();
					exponent++;
				}
				if (exponent > maxSuperclasses) {
					throw new IllegalArgumentException("You are using types that are nested more than "
							+ maxSuperclasses + " levels deep below their corresponding interface or "
							+ "below Object. This is too much for this method to handle.");
				}

				score -= Math.pow(MAX_NUM_EXECUTABLE_PARAMETERS, exponent);
			}
		}
		
		return score;
	}
}
