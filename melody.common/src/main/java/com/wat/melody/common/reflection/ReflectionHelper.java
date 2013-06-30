package com.wat.melody.common.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class ReflectionHelper {

	/**
	 * <p>
	 * Tests if the given subject class implements the given interface, at any
	 * parent degrees.
	 * </p>
	 * 
	 * @param c
	 *            is the subject class.
	 * @param base
	 *            is the required interface.
	 * 
	 * @return <tt>true</tt> if the given subject class implements the given
	 *         interface, or <tt>false</tt> if not.
	 * 
	 * @throw IllegalArgumentException if the given subject class or the given
	 *        interface is <tt>null</tt>.
	 */
	public static boolean implement(Class<?> c, Class<?> base) {
		if (c == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Class.class.getCanonicalName() + ".");
		}
		if (base == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Class.class.getCanonicalName() + ".");
		}
		for (Class<?> i : c.getInterfaces()) {
			if (i == base) {
				return true;
			}
		}
		// recursive search in the parent class
		if (c.getSuperclass() != null) {
			return implement(c.getSuperclass(), base);
		} else {
			return false;
		}
	}

	/**
	 * <p>
	 * Tests if the given subject class extends the given parent class, at any
	 * parent degrees.
	 * </p>
	 * 
	 * @param c
	 *            is the subject class.
	 * @param base
	 *            is the parent class.
	 * 
	 * @return <tt>true</tt> if the given subject class extends the required
	 *         class, or <tt>false</tt> if not.
	 * 
	 * @throw IllegalArgumentException if the given subject class or the given
	 *        parent class is <tt>null</tt>.
	 */
	public static boolean herit(Class<?> c, Class<?> base) {
		if (c == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Class.class.getCanonicalName() + ".");
		}
		if (base == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Class.class.getCanonicalName() + ".");
		}
		if (c == base) {
			return true;
		}
		// recursive search in the parent class
		if (c.getSuperclass() != null) {
			return herit(c.getSuperclass(), base);
		} else {
			return false;
		}
	}

	/**
	 * @param method
	 *            is a method.
	 * @param annotation
	 *            is an annotation.
	 * 
	 * @return the annotation found in the the given method, at any herited
	 *         degrees, or <tt>null</tt> if it doesn't exists.
	 */
	public static <T extends Annotation> T getAnnotation(Method method,
			Class<T> annotation) {
		Class<?> clazz = method.getDeclaringClass();
		for (Class<?> inter : getAllInheritedParents(clazz)) {
			try {
				Method m = inter.getDeclaredMethod(method.getName(),
						method.getParameterTypes());
				if (m.isAnnotationPresent(annotation)) {
					return m.getAnnotation(annotation);
				}
			} catch (NoSuchMethodException ignored) {
			}
		}
		return null;
	}

	public static List<Class<?>> getAllInheritedParents(Class<?> clazz) {
		List<Class<?>> allParents = new ArrayList<Class<?>>();
		for (Class<?> superClass : getAllSuperClass(clazz)) {
			allParents.add(superClass);
			allParents.addAll(getAllInterfaces(superClass));
		}
		return allParents;
	}

	public static List<Class<?>> getAllSuperClass(Class<?> clazz) {
		List<Class<?>> superClasses = new ArrayList<Class<?>>();
		for (; clazz != Object.class && clazz != null; clazz = clazz
				.getSuperclass()) {
			superClasses.add(clazz);
		}
		return superClasses;
	}

	public static List<Class<?>> getAllSuperssClass(List<Class<?>> classes) {
		for (Class<?> clazz : classes) {
			classes.addAll(getAllSuperClass(clazz));
		}
		return classes;
	}

	public static List<Class<?>> getAllInterfaces(Class<?> clazz) {
		return getAllInterfaces(Arrays.asList(clazz.getInterfaces()));
	}

	public static List<Class<?>> getAllInterfaces(List<Class<?>> classes) {
		for (Class<?> clazz : classes) {
			classes.addAll(getAllInterfaces(clazz));
		}
		return classes;
	}

}