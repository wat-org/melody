package com.wat.melody.core.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import com.wat.melody.api.Messages;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.annotation.NestedElement;
import com.wat.melody.api.annotation.NestedElement.Type;
import com.wat.melody.api.annotation.TextContent;
import com.wat.melody.api.exception.TaskFactoryException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.reflection.ReflectionHelper;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class TaskFactoryCache {

	private Map<Class<?>, Map<String, Method>> _setCache;
	private Map<Class<?>, Map<String, Method>> _textCache;
	private Map<Class<?>, Map<String, Method>> _addCache;
	private Map<Class<?>, Map<String, Method>> _createCache;

	public TaskFactoryCache() {
		_setCache = new HashMap<Class<?>, Map<String, Method>>();
		_textCache = new HashMap<Class<?>, Map<String, Method>>();
		_addCache = new HashMap<Class<?>, Map<String, Method>>();
		_createCache = new HashMap<Class<?>, Map<String, Method>>();
	}

	public Method getSetMethod(Class<?> c, String attrName)
			throws TaskFactoryException {
		if (!contains(c)) {
			put(c);
		}
		return _setCache.get(c).get(attrName.toUpperCase());
	}

	public Method getTextMethod(Class<?> c) throws TaskFactoryException {
		if (!contains(c)) {
			put(c);
		}
		return _textCache.get(c).get("t");
	}

	public Method getAddMethod(Class<?> c, String elmtName)
			throws TaskFactoryException {
		if (!contains(c)) {
			put(c);
		}
		return _addCache.get(c).get(elmtName.toUpperCase());
	}

	public Method getCreateMethod(Class<?> c, String elmtName)
			throws TaskFactoryException {
		if (!contains(c)) {
			put(c);
		}
		return _createCache.get(c).get(elmtName.toUpperCase());
	}

	private boolean contains(Class<?> c) {
		return _setCache.containsKey(c);
	}

	private void put(Class<?> c) throws TaskFactoryException {
		Map<String, Method> setEntry = _setCache.get(c);
		if (setEntry == null) {
			_setCache.put(c, setEntry = new HashMap<String, Method>());
		}
		Map<String, Method> textEntry = _textCache.get(c);
		if (textEntry == null) {
			_textCache.put(c, textEntry = new HashMap<String, Method>());
		}
		Map<String, Method> addEntry = _addCache.get(c);
		if (addEntry == null) {
			_addCache.put(c, addEntry = new HashMap<String, Method>());
		}
		Map<String, Method> createEntry = _createCache.get(c);
		if (createEntry == null) {
			_createCache.put(c, createEntry = new HashMap<String, Method>());
		}
		for (Method m : c.getMethods()) {
			Attribute a = ReflectionHelper.getAnnotation(m, Attribute.class);
			if (a != null) {
				validateSetMethod(c, a, m);
				if (setEntry.containsKey(a.name().toUpperCase())) {
					throw new TaskFactoryException(Msg.bind(
							Messages.TaskFactoryEx_ATTR_DUPLICATE, a.name()
									.toUpperCase(), setEntry.get(a.name()
									.toUpperCase()), m, Attribute.class
									.getCanonicalName()));
				}
				setEntry.put(a.name().toUpperCase(), m);
				continue;
			}
			TextContent t = ReflectionHelper
					.getAnnotation(m, TextContent.class);
			if (t != null) {
				validateTextMethod(c, t, m);
				if (textEntry.containsKey("t")) {
					throw new TaskFactoryException(Msg.bind(
							Messages.TaskFactoryEx_TEXT_DUPLICATE,
							textEntry.get("t"), m,
							TextContent.class.getCanonicalName()));
				}
				textEntry.put("t", m);
				continue;
			}
			NestedElement e = ReflectionHelper.getAnnotation(m,
					NestedElement.class);
			if (e != null && e.type() == Type.ADD) {
				validateAddMethod(c, e, m);
				if (addEntry.containsKey(e.name().toUpperCase())) {
					throw new TaskFactoryException(Msg.bind(
							Messages.TaskFactoryEx_NE_DUPLICATE, e.name()
									.toUpperCase(), addEntry.get(e.name()
									.toUpperCase()), m, NestedElement.class
									.getCanonicalName()));
				}
				if (createEntry.containsKey(e.name().toUpperCase())) {
					throw new TaskFactoryException(Msg.bind(
							Messages.TaskFactoryEx_NE_DUPLICATE, e.name()
									.toUpperCase(), createEntry.get(e.name()
									.toUpperCase()), m, NestedElement.class
									.getCanonicalName()));
				}
				addEntry.put(e.name().toUpperCase(), m);
				continue;
			}
			if (e != null && e.type() == Type.CREATE) {
				validateCreateMethod(c, e, m);
				if (addEntry.containsKey(e.name().toUpperCase())) {
					throw new TaskFactoryException(Msg.bind(
							Messages.TaskFactoryEx_NE_DUPLICATE, e.name()
									.toUpperCase(), addEntry.get(e.name()
									.toUpperCase()), m, NestedElement.class
									.getCanonicalName()));
				}
				if (createEntry.containsKey(e.name().toUpperCase())) {
					throw new TaskFactoryException(Msg.bind(
							Messages.TaskFactoryEx_NE_DUPLICATE, e.name()
									.toUpperCase(), createEntry.get(e.name()
									.toUpperCase()), m, NestedElement.class
									.getCanonicalName()));
				}
				createEntry.put(e.name().toUpperCase(), m);
				continue;
			}
		}
	}

	/**
	 * <p>
	 * Validate that the method respect {@link Attribute} specifications.
	 * </p>
	 * 
	 * The setter's method should respect the following specifications :
	 * <ul>
	 * <li>must be <tt>public</tt> ;</li>
	 * <li>must not be <tt>abstract</tt> ;</li>
	 * <li>must have an {@link Attribute} annotation whose name is equal to the
	 * given attribute name (no case match) ;</li>
	 * <li>must have 1 argument ;</li>
	 * <li>the argument type must be public ;</li>
	 * <li>the argument type must not be an abstract ;</li>
	 * <li>the argument type must not be an array ;</li>
	 * <li>the argument type must not be an interface ;</li>
	 * </ul>
	 * 
	 * @param c
	 * @param a
	 * @param m
	 * 
	 * @throws TaskFactoryException
	 *             if the method decorated by the annotation {@link Attribute}
	 *             doesn't respect specifications.
	 */
	private void validateSetMethod(Class<?> c, Attribute a, Method m)
			throws TaskFactoryException {
		if (!Modifier.isPublic(m.getModifiers())
				|| Modifier.isAbstract(m.getModifiers())
				|| m.getParameterTypes().length != 1) {
			throw new TaskFactoryException(Msg.bind(
					Messages.TaskFactoryEx_ATTR_SPEC_CONFLICT, m.getName(),
					c.getCanonicalName(), a.name()));
		}

		Class<?> param = m.getParameterTypes()[0];
		if (!Modifier.isPublic(param.getModifiers())
				|| (Modifier.isAbstract(param.getModifiers()) && !(param
						.isEnum() || param.isPrimitive()))
				|| param.isInterface() || param.isArray()) {
			throw new TaskFactoryException(Msg.bind(
					Messages.TaskFactoryEx_ATTR_SPEC_CONFLICT, m.getName(),
					c.getCanonicalName(), a.name()));
		}
	}

	/**
	 * <p>
	 * Validate that the method respect {@link TextContent} specifications.
	 * </p>
	 * 
	 * The setter's method should respect the following specifications :
	 * <ul>
	 * <li>must be <tt>public</tt> ;</li>
	 * <li>must not be <tt>abstract</tt> ;</li>
	 * <li>must have an {@link TextContent} annotation ;</li>
	 * <li>must have 1 argument ;</li>
	 * <li>the argument type must be public ;</li>
	 * <li>the argument type must not be an abstract ;</li>
	 * <li>the argument type must not be an array ;</li>
	 * <li>the argument type must not be an interface ;</li>
	 * </ul>
	 * 
	 * @param c
	 * @param t
	 * @param m
	 * 
	 * @throws TaskFactoryException
	 *             if the method decorated by the annotation {@link Attribute}
	 *             doesn't respect specifications.
	 */
	private void validateTextMethod(Class<?> c, TextContent t, Method m)
			throws TaskFactoryException {
		if (!Modifier.isPublic(m.getModifiers())
				|| Modifier.isAbstract(m.getModifiers())
				|| m.getParameterTypes().length != 1) {
			throw new TaskFactoryException(Msg.bind(
					Messages.TaskFactoryEx_TEXT_SPEC_CONFLICT, m.getName(),
					c.getCanonicalName()));
		}

		Class<?> param = m.getParameterTypes()[0];
		if (!Modifier.isPublic(param.getModifiers())
				|| (Modifier.isAbstract(param.getModifiers()) && !(param
						.isEnum() || param.isPrimitive()))
				|| param.isInterface() || param.isArray()) {
			throw new TaskFactoryException(Msg.bind(
					Messages.TaskFactoryEx_TEXT_SPEC_CONFLICT, m.getName(),
					c.getCanonicalName()));
		}
	}

	/**
	 * <p>
	 * Validate that the Add method respect {@link NestedElement}
	 * specifications.
	 * </p>
	 * 
	 * The add's method should respect the following specifications :
	 * <ul>
	 * <li>must be <tt>public</tt> ;</li>
	 * <li>must not be <tt>abstract</tt> ;</li>
	 * <li>must have an {@link NestedElement} annotation whose name is equal to
	 * the given attribute name (no case match) and whose type is equal to
	 * {@link NestedElement.Type#ADD} ;</li>
	 * <li>must not return <tt>void</tt> ;</li>
	 * <li>must have 1 argument ;</li>
	 * <li>the argument type must be public ;</li>
	 * <li>the argument type must not be an abstract ;</li>
	 * <li>the argument type must not be an interface ;</li>
	 * <li>the argument type must not be an enumeration ;</li>
	 * <li>the argument type must not be an primitive ;</li>
	 * <li>the argument type must not be an array ;</li>
	 * <li>the argument type must have a public no-arg constructor ;</li>
	 * </ul>
	 * 
	 * @param c
	 * @param a
	 * @param m
	 * 
	 * @throws TaskFactoryException
	 *             if the method decorated by the annotation
	 *             {@link NestedElement} doesn't respect specifications.
	 */
	private void validateAddMethod(Class<?> c, NestedElement a, Method m)
			throws TaskFactoryException {
		if (!Modifier.isPublic(m.getModifiers())
				|| Modifier.isAbstract(m.getModifiers())
				|| m.getReturnType() != Void.TYPE
				|| m.getParameterTypes().length != 1) {
			throw new TaskFactoryException(Msg.bind(
					Messages.TaskFactoryEx_ADD_NE_SPEC_CONFLICT, m, a.name()));
		}

		Class<?> param = m.getParameterTypes()[0];
		if (!Modifier.isPublic(param.getModifiers())
				|| Modifier.isAbstract(param.getModifiers())
				|| param.isInterface() || param.isEnum() || param.isPrimitive()
				|| param.isArray()) {
			throw new TaskFactoryException(Msg.bind(
					Messages.TaskFactoryEx_ADD_NE_SPEC_CONFLICT, m, a.name()));
		}

		for (Constructor<?> ct : param.getConstructors()) {
			if (ct.getParameterTypes().length == 0
					&& Modifier.isPublic(ct.getModifiers())) {
				// add's method correct
				return;
			}
		}
		throw new TaskFactoryException(Msg.bind(
				Messages.TaskFactoryEx_ADD_NE_SPEC_CONFLICT, m, a.name()));
	}

	/**
	 * <p>
	 * Validate that the Create method respect {@link NestedElement}
	 * specifications.
	 * </p>
	 * 
	 * The create's method should respect the following specifications :
	 * <ul>
	 * <li>must be <tt>public</tt> ;</li>
	 * <li>must not be an <tt>abstract</tt> ;</li>
	 * <li>must have an {@link NestedElement} annotation whose name is equal to
	 * the given attribute name (no case match) and whose type is equal to
	 * {@link NestedElement.Type#CREATE} ;</li>
	 * <li>must not return <tt>void</tt> ;</li>
	 * <li>must have 0 argument ;</li>
	 * </ul>
	 * 
	 * @param c
	 * @param a
	 * @param m
	 * 
	 * @throws TaskFactoryException
	 *             if the method decorated by the annotation
	 *             {@link NestedElement} doesn't respect specifications.
	 */
	private void validateCreateMethod(Class<?> c, NestedElement a, Method m)
			throws TaskFactoryException {
		if (!Modifier.isPublic(m.getModifiers())
				|| Modifier.isAbstract(m.getModifiers())
				|| m.getReturnType() == Void.TYPE
				|| m.getParameterTypes().length != 0) {
			throw new TaskFactoryException(
					Msg.bind(Messages.TaskFactoryEx_CREATE_NE_SPEC_CONFLICT, m,
							a.name()));
		}
	}

}