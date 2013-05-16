package com.wat.melody.core.internal;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.api.IFirstLevelTask;
import com.wat.melody.api.IRegisteredTasks;
import com.wat.melody.api.ITask;
import com.wat.melody.api.ITaskContainer;
import com.wat.melody.api.ITopLevelTask;
import com.wat.melody.api.Melody;
import com.wat.melody.api.Messages;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.annotation.NestedElement;
import com.wat.melody.api.event.State;
import com.wat.melody.api.exception.TaskException;
import com.wat.melody.api.exception.TaskFactoryAttributeException;
import com.wat.melody.api.exception.TaskFactoryException;
import com.wat.melody.api.exception.TaskFactoryNestedElementException;
import com.wat.melody.common.bool.Bool;
import com.wat.melody.common.bool.exception.IllegalBooleanException;
import com.wat.melody.common.files.IFileBased;
import com.wat.melody.common.properties.PropertiesSet;
import com.wat.melody.common.xml.Doc;
import com.wat.melody.common.xpath.exception.ExpressionSyntaxException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class TaskFactory {

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
	 * @return <code>true</code> if the given subject class implements the given
	 *         interface, or <code>false</code> if not.
	 * 
	 * @throw IllegalArgumentException if the given subject class or the given
	 *        interface is <tt>null</tt>.
	 */
	public static boolean implementsInterface(Class<?> c, Class<?> base) {
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
			return implementsInterface(c.getSuperclass(), base);
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
	 * @return <code>true</code> if the given subject class extends the required
	 *         class, or <code>false</code> if not.
	 * 
	 * @throw IllegalArgumentException if the given subject class or the given
	 *        parent class is <tt>null</tt>.
	 */
	public static boolean heritsClass(Class<?> c, Class<?> base) {
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
			return heritsClass(c.getSuperclass(), base);
		} else {
			return false;
		}
	}

	/**
	 * <p>
	 * Find the setter's method in the given class which matches the given
	 * attribute name.
	 * </p>
	 * 
	 * <p>
	 * The setter's method should respect the following rules :
	 * <ul>
	 * <li>must be <code>public</code> ;</li>
	 * <li>must not be <code>abstract</code> ;</li>
	 * <li>must have an {@link Attribute} annotation whose name is equal to the
	 * given attribute name (no case match) ;</li>
	 * <li>must have 1 argument ;</li>
	 * <li>the argument type must be public ;</li>
	 * <li>the argument type must not be an abstract ;</li>
	 * <li>the argument type must not be an array ;</li>
	 * <li>the argument type must not be an interface ;</li>
	 * </ul>
	 * </p>
	 * 
	 * @param c
	 *            is the class where the search is done.
	 * @param sAttrName
	 *            is the name of searched attribute.
	 * 
	 * @return the setter's method which matches the given attribute name, or
	 *         <tt>null</tt> if no setter's method which matches the given
	 *         attribute name can be found in the given class.
	 * 
	 * @throws TaskFactoryException
	 *             if the method decorated by the annotation {@link Attribute}
	 *             doesn't respect specifications.
	 */
	private static Method findSetMethod(Class<?> c, String sAttrName)
			throws TaskFactoryException {
		// For all method of the task
		for (Method m : c.getMethods()) {
			// Look for the annotation TaskAttrbiute
			Attribute a = m.getAnnotation(Attribute.class);
			if (a == null || !a.name().equalsIgnoreCase(sAttrName)) {
				continue;
			}
			// Validate that the method respect TaskAttribute specifications
			if (!Modifier.isPublic(m.getModifiers())
					|| Modifier.isAbstract(m.getModifiers())
					|| m.getParameterTypes().length != 1) {
				throw new TaskFactoryException(Messages.bind(
						Messages.TaskFactoryEx_ATTR_SPEC_CONFLICT,
						new Object[] { m.getName(), c.getCanonicalName(),
								a.name() }));
			}

			Class<?> param = m.getParameterTypes()[0];
			if (!Modifier.isPublic(param.getModifiers())
					|| (Modifier.isAbstract(param.getModifiers()) && !(param
							.isEnum() || param.isPrimitive()))
					|| param.isInterface() || param.isArray()) {
				throw new TaskFactoryException(Messages.bind(
						Messages.TaskFactoryEx_ATTR_SPEC_CONFLICT,
						new Object[] { m.getName(), c.getCanonicalName(),
								a.name() }));
			}
			// setter's method found
			return m;
		}
		return null;
	}

	/**
	 * <p>
	 * Find the add's method in the given class which matches the given
	 * attribute name.
	 * </p>
	 * 
	 * <p>
	 * The add's method should respect the following rules :
	 * <ul>
	 * <li>must be <code>public</code> ;</li>
	 * <li>must not be <code>abstract</code> ;</li>
	 * <li>must have an {@link NestedElement} annotation whose name is equal to
	 * the given attribute name (no case match) and whose type is equal to
	 * {@link NestedElement.Type#ADD} ;</li>
	 * <li>must not return <code>void</code> ;</li>
	 * <li>must have 1 argument ;</li>
	 * <li>the argument type must be public ;</li>
	 * <li>the argument type must not be an abstract ;</li>
	 * <li>the argument type must not be an interface ;</li>
	 * <li>the argument type must not be an enumeration ;</li>
	 * <li>the argument type must not be an primitive ;</li>
	 * <li>the argument type must not be an array ;</li>
	 * <li>the argument type must have a public no-arg constructor ;</li>
	 * </ul>
	 * </p>
	 * 
	 * @param c
	 *            is the class where the search is done.
	 * @param sAttrName
	 *            is the name of searched attribute.
	 * 
	 * @return the add's method which matches the given attribute name, or
	 *         <tt>null</tt> if no add method which matches the given attribute
	 *         name can be found in the given class.
	 * 
	 * @throws TaskFactoryException
	 *             if the method decorated by the annotation
	 *             {@link NestedElement} doesn't respect specifications.
	 */
	private static Method findAddMethod(Class<?> c, String sNodeName)
			throws TaskFactoryException {
		// For all method of the task
		for (Method m : c.getMethods()) {
			// Look for the annotation TaskNestedElement
			NestedElement a = m.getAnnotation(NestedElement.class);
			if (a == null || !a.name().equalsIgnoreCase(sNodeName)
					|| a.type() != NestedElement.Type.ADD) {
				continue;
			}
			// Validate that the method respect TaskNestedElement specifications
			if (!Modifier.isPublic(m.getModifiers())
					|| Modifier.isAbstract(m.getModifiers())
					|| m.getReturnType() != Void.TYPE
					|| m.getParameterTypes().length != 1) {
				throw new TaskFactoryException(Messages.bind(
						Messages.TaskFactoryEx_ADD_NE_SPEC_CONFLICT,
						new Object[] { m.getName(), c.getCanonicalName(),
								a.name() }));
			}

			Class<?> param = m.getParameterTypes()[0];
			if (!Modifier.isPublic(param.getModifiers())
					|| Modifier.isAbstract(param.getModifiers())
					|| param.isInterface() || param.isEnum()
					|| param.isPrimitive() || param.isArray()) {
				throw new TaskFactoryException(Messages.bind(
						Messages.TaskFactoryEx_ADD_NE_SPEC_CONFLICT,
						new Object[] { m.getName(), c.getCanonicalName(),
								a.name() }));
			}

			for (Constructor<?> ct : param.getConstructors()) {
				if (ct.getParameterTypes().length == 0
						&& Modifier.isPublic(ct.getModifiers())) {
					// add's method found
					return m;
				}
			}
			throw new TaskFactoryException(Messages.bind(
					Messages.TaskFactoryEx_ADD_NE_SPEC_CONFLICT, new Object[] {
							m.getName(), c.getCanonicalName(), a.name() }));
		}
		return null;
	}

	/**
	 * <p>
	 * Find the create's method in the given class which matches the given
	 * attribute name.
	 * </p>
	 * 
	 * <p>
	 * The create's method should respect the following rules :
	 * <ul>
	 * <li>must be <code>public</code> ;</li>
	 * <li>must not be an <code>abstract</code> ;</li>
	 * <li>must have an {@link NestedElement} annotation whose name is equal to
	 * the given attribute name (no case match) and whose type is equal to
	 * {@link NestedElement.Type#CREATE} ;</li>
	 * <li>must not return <code>void</code> ;</li>
	 * <li>must have 0 argument ;</li>
	 * </ul>
	 * </p>
	 * 
	 * @param c
	 *            is the class where the search is done.
	 * @param sAttrName
	 *            is the name of searched attribute.
	 * 
	 * @return the create's method which matches the given attribute name, or
	 *         <tt>null</tt> if no add method which matches the given attribute
	 *         name can be found in the given class.
	 * 
	 * @throws TaskFactoryException
	 *             if the method decorated by the annotation
	 *             {@link NestedElement} doesn't respect specifications.
	 */
	private static Method findCreateMethod(Class<?> c, String sNodeName)
			throws TaskFactoryException {
		// For all method of the task
		for (Method m : c.getMethods()) {
			// Look for the annotation TaskNestedElement
			NestedElement a = m.getAnnotation(NestedElement.class);
			if (a == null || !a.name().equalsIgnoreCase(sNodeName)
					|| a.type() != NestedElement.Type.CREATE) {
				continue;
			}
			if (!Modifier.isPublic(m.getModifiers())
					|| Modifier.isAbstract(m.getModifiers())
					|| m.getReturnType() == Void.TYPE
					|| m.getParameterTypes().length != 0) {
				throw new TaskFactoryException(Messages.bind(
						Messages.TaskFactoryEx_CREATE_NE_SPEC_CONFLICT,
						new Object[] { m.getName(), c.getCanonicalName(),
								a.name() }));
			}
			// create's method found
			return m;
		}
		return null;
	}

	private IRegisteredTasks moRegisteredTasks;

	/**
	 * <p>
	 * Create a new {@link TaskFactory} instance, which allow to create new
	 * {@link ITask} from Elements Nodes found in the Sequence Descriptor.
	 * </p>
	 */
	public TaskFactory() {
		setRegisteredTasks(new RegisteredTasks());
	}

	public IRegisteredTasks getRegisteredTasks() {
		return moRegisteredTasks;
	}

	public IRegisteredTasks setRegisteredTasks(IRegisteredTasks rg) {
		if (rg == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid IRegisteredTasks.");
		}
		IRegisteredTasks previous = getRegisteredTasks();
		moRegisteredTasks = rg;
		return previous;
	}

	/**
	 * <p>
	 * Identify the Class which correspond to the given node. This node
	 * represents an {@link ITask}, in its native {@link Node} format
	 * </p>
	 * 
	 * @param n
	 *            is an element node, which represent an {@link ITask}, in its
	 *            native {@link Node} format.
	 * 
	 * @return the Class which correspond to the given node.
	 * 
	 * @throws TaskFactoryException
	 *             if the element node name doesn't match any registered task.
	 * @throws TaskFactoryException
	 *             if the Task hierarchy is not valid.
	 * @throws IllegalArgumentException
	 *             if a the given {@link Node} is <tt>null</tt>.
	 * @throws IllegalArgumentException
	 *             if a the given {@link Node} is not an XML Element.
	 * 
	 */
	public Class<? extends ITask> identifyTask(Node n)
			throws TaskFactoryException {
		if (n == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Node (an "
					+ ITask.class.getCanonicalName() + ").");
		}
		if (n.getNodeType() != Node.ELEMENT_NODE) {
			throw new IllegalArgumentException(n.getNodeType()
					+ ": Not accepted. " + "Must be a valid XML Elment node.");
		}

		Class<? extends ITask> c = findTaskClass(n);
		validateTaskHierarchy(c, n.getParentNode());
		return c;
	}

	/**
	 * <p>
	 * Search for the Registered Task Class which match the given Node.
	 * </p>
	 * 
	 * @param n
	 *            is the {@link Node} which represent an {@link ITask} in it
	 *            native format.
	 * 
	 * @return a {@link Class} object, which can be use to instantiate an
	 *         {@link ITask}.
	 * 
	 * @throws TaskFactoryException
	 *             if the given {@link Node} is doesn't represent a valid
	 *             {@link ITask}.
	 */
	private Class<? extends ITask> findTaskClass(Node n)
			throws TaskFactoryException {
		String sSimpleName = n.getNodeName();
		if (!getRegisteredTasks().contains(sSimpleName)) {
			throw new TaskFactoryException(Messages.bind(
					Messages.TaskFactoryEx_UNDEF_TASK, sSimpleName));
		}
		return getRegisteredTasks().get(sSimpleName);
	}

	private void validateTaskHierarchy(Class<?> c, Node parentNode)
			throws TaskFactoryException {
		Class<?> p = null;
		if (parentNode != null
				&& parentNode.getNodeType() != Node.DOCUMENT_NODE) {
			try {
				p = findTaskClass(parentNode);
			} catch (TaskFactoryException Ex) {
				throw new RuntimeException("Unexpected error while searching "
						+ "the Task Class which match the Node ["
						+ Doc.getNodeLocation(parentNode) + "]. "
						+ "Because this Task already been created, "
						+ "such error cannot happened. "
						+ "Source code has certainly been modified and a bug "
						+ "have been introduced.", Ex);
			}
		}
		if (implementsInterface(c, ITopLevelTask.class) && p != null) {
			throw new TaskFactoryException(Messages.bind(
					Messages.TaskFactoryEx_MUST_BE_TOPLEVEL, c.getName()));
		} else if (!implementsInterface(c, ITopLevelTask.class) && p == null) {
			throw new TaskFactoryException(Messages.bind(
					Messages.TaskFactoryEx_CANNOT_BE_TOPLEVEL, c.getName()));
		} else if (implementsInterface(c, IFirstLevelTask.class) && p != null
				&& !implementsInterface(p, ITopLevelTask.class)) {
			throw new TaskFactoryException(Messages.bind(
					Messages.TaskFactoryEx_MUST_BE_FIRSTLEVEL, c.getName(),
					p.getName()));
		} else if (!implementsInterface(c, IFirstLevelTask.class) && p != null
				&& implementsInterface(p, ITopLevelTask.class)) {
			throw new TaskFactoryException(Messages.bind(
					Messages.TaskFactoryEx_CANNOT_BE_FIRSTLEVEL, c.getName(),
					p.getName()));
		}
	}

	/**
	 * <p>
	 * Create a new Task, based on the given element node.
	 * </p>
	 * 
	 * <p>
	 * <ul>
	 * <li>Will expand attribute values ;</li>
	 * <li>During expansion process, the given PropertiesSet will be used ;</li>
	 * <li>PropertiesSet contains all variables name/values that can be used in
	 * expression ;</li>
	 * </ul>
	 * </p>
	 * 
	 * @param c
	 *            is the class of the Task to create.
	 * @param n
	 *            is an element node, which represent the Task to create, in its
	 *            native {@link Node} format.
	 * @param ps
	 *            is a PropertiesSet, which will be used during attribute's
	 *            value's expansion.
	 * 
	 * @return a new object that implement Task.
	 * 
	 * @throws TaskFactoryException
	 *             if an attribute which is not supported by the task is found.
	 * @throws TaskFactoryException
	 *             if an nested element which is not supported by the task is
	 *             found.
	 * @throws TaskFactoryException
	 *             if an attribute's value's expansion encounter errors.
	 * @throws TaskFactoryException
	 *             if, while trying to set a Task's attribute, the parameter of
	 *             the setter doesn't have a public constructor which accept a
	 *             sole String argument.
	 * @throws TaskFactoryException
	 *             if, while setting a task member, the task generate an error.
	 * @throws TaskFactoryException
	 *             if a error occurred while creating a nested element.
	 * @throws TaskFactoryException
	 *             if, while validating the task, the task generate an error.
	 * @throws IllegalArgumentException
	 *             if a the given {@link Class} is <tt>null</tt>.
	 * @throws IllegalArgumentException
	 *             if a the given {@link Node} is <tt>null</tt>.
	 * @throws IllegalArgumentException
	 *             if a the {@link PropertiesSet} is <tt>null</tt>.
	 * @throws IllegalArgumentException
	 *             if a the given {@link Node} is not an XML Element.
	 * 
	 */
	public ITask newTask(Class<? extends ITask> c, Node n, PropertiesSet ps)
			throws TaskFactoryException {
		if (c == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Class.class.getCanonicalName() + ".");
		}
		if (n == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Node (an "
					+ ITask.class.getCanonicalName() + ").");
		}
		if (ps == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ PropertiesSet.class.getCanonicalName() + ".");
		}
		if (n.getNodeType() != Node.ELEMENT_NODE) {
			throw new IllegalArgumentException(n.getNodeType()
					+ ": Not accepted. " + "Must be a valid XML Elment node.");
		}

		ITask t = null;
		try {
			t = c.getConstructor().newInstance();
		} catch (NoSuchMethodException | InstantiationException
				| IllegalAccessException | ClassCastException Ex) {
			throw new RuntimeException("Unexpected error while creating a "
					+ "Task '" + c.getCanonicalName() + "' by reflection. "
					+ "Because a public no-argument constructor exists "
					+ "(see 'registerTasks'), such error cannot happened. "
					+ "Source code has certainly been modified and a "
					+ "bug have been introduced.", Ex);
		} catch (InvocationTargetException Ex) {
			throw new RuntimeException("Task '" + c.getCanonicalName()
					+ "' creation fails, generating the error below. ",
					Ex.getCause());
		}

		synchronized (n.getOwnerDocument()) {
			setAllMembers(t, n.getAttributes(), n.getNodeName());
			setAllNestedElements(t, n.getChildNodes(), n.getNodeName());
		}
		try {
			t.validate();
		} catch (TaskException Ex) {
			throw new TaskFactoryException(Ex);
		}
		return t;
	}

	private void setAllMembers(Object base, NamedNodeMap attrs, String sNodeName)
			throws TaskFactoryException {
		if (attrs == null) {
			return;
		}
		for (int i = 0; i < attrs.getLength(); i++) {
			Node attr = attrs.item(i);
			String sAttrName = attr.getNodeName();
			Method m = findSetMethod(base.getClass(), sAttrName);
			if (m != null) {
				String sAttrVal = null;
				try {
					sAttrVal = Melody.getContext().expand(attr.getNodeValue());
				} catch (ExpressionSyntaxException Ex) {
					throw new TaskFactoryAttributeException(attr, m,
							Messages.bind(Messages.TaskFactoryEx_EXPAND_ATTR,
									sAttrName, State.FAILED), Ex);
				} catch (Throwable Ex) {
					throw new TaskFactoryAttributeException(attr, m,
							Messages.bind(Messages.TaskFactoryEx_EXPAND_ATTR,
									sAttrName, State.CRITICAL), Ex);
				}
				setMember(base, m, m.getParameterTypes()[0], attr, sAttrVal);
			} else {
				throw new TaskFactoryException(Messages.bind(
						Messages.TaskFactoryEx_INVALID_ATTR, sAttrName,
						Doc.getNodeLocation(attr)));
			}
		}
		detectsUndefinedMandatoryAttributes(base.getClass(), attrs);
	}

	private void detectsUndefinedMandatoryAttributes(Class<?> base,
			NamedNodeMap attrs) throws TaskFactoryException {
		// Detect if all mandatory attribute are defined
		for (Method m : base.getMethods()) {
			// Look for the annotation TaskAttrbiute
			Attribute a = m.getAnnotation(Attribute.class);
			if (a != null && a.mandatory()) {
				boolean found = false;
				for (int i = 0; i < attrs.getLength(); i++) {
					if (a.name().equalsIgnoreCase(attrs.item(i).getNodeName())) {
						found = true;
						break;
					}
				}
				if (!found) {
					throw new TaskFactoryException(Messages.bind(
							Messages.TaskFactoryEx_MANDATORY_ATTR_NOT_FOUND,
							a.name()));
				}
			}
		}
	}

	private void setAllNestedElements(Object base, NodeList nestedNodes,
			String sNodeName) throws TaskFactoryException {
		if (nestedNodes == null) {
			return;
		}
		for (int i = 0; i < nestedNodes.getLength(); i++) {
			Node n = nestedNodes.item(i);
			if (n.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			if (addNestedElement(base, n) || createNestedElement(base, n)
					|| addNodeNestedElement(base, n)) {
				continue;
			}
			throw new TaskFactoryException(Messages.bind(
					Messages.TaskFactoryEx_INVALID_NE, n.getNodeName(),
					Doc.getNodeLocation(n)));
		}
		detectsUndefinedMandatoryNestedElements(base.getClass(), nestedNodes);
	}

	private void detectsUndefinedMandatoryNestedElements(Class<?> base,
			NodeList nestedNodes) throws TaskFactoryException {
		for (Method m : base.getMethods()) {
			NestedElement a = m.getAnnotation(NestedElement.class);
			if (a != null && a.mandatory()) {
				boolean found = false;
				for (int i = 0; i < nestedNodes.getLength(); i++) {
					if (a.name().equalsIgnoreCase(
							nestedNodes.item(i).getNodeName())) {
						found = true;
						break;
					}
				}
				if (!found) {
					throw new TaskFactoryException(Messages.bind(
							Messages.TaskFactoryEx_MANDATORY_NE_NOT_FOUND,
							a.name()));
				}
			}
		}
	}

	private boolean addNestedElement(Object base, Node n)
			throws TaskFactoryException {
		Method m = findAddMethod(base.getClass(), n.getNodeName());
		if (m == null) {
			return false;
		}
		try {
			Object o;
			try {
				o = m.getParameterTypes()[0].getConstructor().newInstance();
			} catch (NoSuchMethodException | InstantiationException
					| IllegalAccessException | ClassCastException Ex) {
				throw new RuntimeException("Unexpected error while adding a "
						+ "nested element by reflection. "
						+ "Source code has certainly been modified and a "
						+ "bug have been introduced.", Ex);
			} catch (InvocationTargetException Ex) {
				throw new TaskFactoryException(Ex.getCause());
			}

			setAllMembers(o, n.getAttributes(), n.getNodeName());
			setAllNestedElements(o, n.getChildNodes(), n.getNodeName());

			try {
				m.invoke(base, o);
			} catch (IllegalAccessException | IllegalArgumentException Ex) {
				throw new RuntimeException("Unexpected error while adding a "
						+ "nested element by reflection. "
						+ "Source code has certainly been modified and a bug "
						+ "have been introduced.", Ex);
			} catch (InvocationTargetException Ex) {
				throw new TaskFactoryException(Ex.getCause());
			}
		} catch (TaskFactoryException Ex) {
			throw new TaskFactoryNestedElementException(n, m, Messages.bind(
					Messages.TaskFactoryEx_SET_NE, n.getNodeName()
							.toLowerCase(), State.FAILED), Ex);
		} catch (Throwable Ex) {
			throw new TaskFactoryNestedElementException(n, m, Messages.bind(
					Messages.TaskFactoryEx_SET_NE, n.getNodeName()
							.toLowerCase(), State.CRITICAL), Ex);
		}

		return true;
	}

	private boolean createNestedElement(Object base, Node n)
			throws TaskFactoryException {
		Method m = findCreateMethod(base.getClass(), n.getNodeName());
		if (m == null) {
			return false;
		}
		try {
			Object o = null;
			try {
				o = m.invoke(base);
			} catch (IllegalAccessException | IllegalArgumentException Ex) {
				throw new RuntimeException("Unexpected error while creating a "
						+ "nested element by reflection. "
						+ "Source code has certainly been modified and a bug "
						+ "have been introduced.", Ex);
			} catch (InvocationTargetException Ex) {
				throw new TaskFactoryException(Ex.getCause());
			}

			setAllMembers(o, n.getAttributes(), n.getNodeName());
			setAllNestedElements(o, n.getChildNodes(), n.getNodeName());
		} catch (TaskFactoryException Ex) {
			throw new TaskFactoryNestedElementException(n, m, Messages.bind(
					Messages.TaskFactoryEx_SET_NE, n.getNodeName()
							.toLowerCase(), State.FAILED), Ex);
		} catch (Throwable Ex) {
			throw new TaskFactoryNestedElementException(n, m, Messages.bind(
					Messages.TaskFactoryEx_SET_NE, n.getNodeName()
							.toLowerCase(), State.CRITICAL), Ex);
		}

		return true;
	}

	private boolean addNodeNestedElement(Object base, Node n)
			throws TaskFactoryException {
		if (!implementsInterface(base.getClass(), ITaskContainer.class)) {
			return false;
		}
		try {
			((ITaskContainer) base).registerInnerTask(n);
		} catch (TaskException Ex) {
			throw new TaskFactoryException(Ex);
		}
		return true;
	}

	private void setMember(Object base, Method m, Class<?> param, Node attr,
			String sAttrVal) throws TaskFactoryException {
		String sAttrName = attr.getNodeName();
		Object o = null;
		try {
			o = createNewPrimitiveType(param, sAttrVal, base, sAttrName);
			if (o == null) {
				o = createNewEnumConstant(param, sAttrVal, base, sAttrName);
			}
			if (o == null) {
				o = createNewFile(param, sAttrVal);
			}
			if (o == null) {
				o = createNewObject(param, sAttrVal);
			}
		} catch (TaskFactoryException Ex) {
			throw new TaskFactoryAttributeException(attr, m,
					Messages.bind(Messages.TaskFactoryEx_CREATE_ATTR,
							sAttrName, State.FAILED), Ex);
		} catch (Throwable Ex) {
			throw new TaskFactoryAttributeException(attr, m, Messages.bind(
					Messages.TaskFactoryEx_CREATE_ATTR, sAttrName,
					State.CRITICAL), Ex);
		}

		try {
			m.invoke(base, o);
		} catch (IllegalAccessException | IllegalArgumentException Ex) {
			throw new RuntimeException("Unexpected error while setting an "
					+ "attribute by reflection. "
					+ "Source code has certainly been modified and a bug "
					+ "have been introduced.", Ex);
		} catch (InvocationTargetException Ex) {
			throw new TaskFactoryAttributeException(attr, m, Messages.bind(
					Messages.TaskFactoryEx_SET_ATTR, sAttrName, State.FAILED),
					Ex.getCause());
		} catch (Throwable Ex) {
			throw new TaskFactoryAttributeException(attr, m,
					Messages.bind(Messages.TaskFactoryEx_SET_ATTR, sAttrName,
							State.CRITICAL), Ex);
		}
	}

	private Object createNewPrimitiveType(Class<?> param, String sAttrVal,
			Object base, String sAttrName) throws TaskFactoryException {
		if (!param.isPrimitive()) {
			return null;
		}

		Object o = null;
		if (param == Boolean.TYPE) {
			try {
				o = Bool.parseString(sAttrVal);
			} catch (IllegalBooleanException e) {
				throw new TaskFactoryException(Messages.bind(
						Messages.TaskFactoryEx_CONVERT_ATTR, sAttrVal,
						Boolean.class.getSimpleName()));
			}
		} else if (param == Character.TYPE) {
			if (sAttrVal != null && sAttrVal.length() == 1) {
				o = sAttrVal.charAt(0);
			} else {
				throw new TaskFactoryException(Messages.bind(
						Messages.TaskFactoryEx_CONVERT_ATTR, sAttrVal,
						Character.class.getSimpleName()));
			}
		} else if (param == Byte.TYPE) {
			try {
				o = Byte.parseByte(sAttrVal);
			} catch (NumberFormatException Ex) {
				throw new TaskFactoryException(Messages.bind(
						Messages.TaskFactoryEx_CONVERT_ATTR, sAttrVal,
						Byte.class.getSimpleName()));
			}
		} else if (param == Short.TYPE) {
			try {
				o = Short.parseShort(sAttrVal);
			} catch (NumberFormatException Ex) {
				throw new TaskFactoryException(Messages.bind(
						Messages.TaskFactoryEx_CONVERT_ATTR, sAttrVal,
						Short.class.getSimpleName()));
			}
		} else if (param == Integer.TYPE) {
			try {
				o = Integer.parseInt(sAttrVal);
			} catch (NumberFormatException Ex) {
				throw new TaskFactoryException(Messages.bind(
						Messages.TaskFactoryEx_CONVERT_ATTR, sAttrVal,
						Integer.class.getSimpleName()));
			}
		} else if (param == Long.TYPE) {
			try {
				o = Long.parseLong(sAttrVal);
			} catch (NumberFormatException Ex) {
				throw new TaskFactoryException(Messages.bind(
						Messages.TaskFactoryEx_CONVERT_ATTR, sAttrVal,
						Long.class.getSimpleName()));
			}
		} else if (param == Float.TYPE) {
			try {
				o = Float.parseFloat(sAttrVal);
			} catch (NumberFormatException Ex) {
				throw new TaskFactoryException(Messages.bind(
						Messages.TaskFactoryEx_CONVERT_ATTR, sAttrVal,
						Float.class.getSimpleName()));
			}
		} else if (param == Double.TYPE) {
			try {
				o = Double.parseDouble(sAttrVal);
			} catch (NumberFormatException Ex) {
				throw new TaskFactoryException(Messages.bind(
						Messages.TaskFactoryEx_CONVERT_ATTR, sAttrVal,
						Double.class.getSimpleName()));
			}
		}
		return o;
	}

	private Object createNewEnumConstant(Class<?> param, String sAttrVal,
			Object base, String sAttrName) throws TaskFactoryException {
		if (!param.isEnum()) {
			return null;
		}

		for (Object v : param.getEnumConstants()) {
			if (v.toString().equalsIgnoreCase(sAttrVal)) {
				return v;
			}
		}
		throw new TaskFactoryException(Messages.bind(
				Messages.TaskFactoryEx_CONVERT_ATTR_TO_ENUM, sAttrVal,
				Arrays.asList(param.getEnumConstants())));
	}

	private Object createNewFile(Class<?> param, String sAttrVal)
			throws TaskFactoryException {
		if (!implementsInterface(param, Path.class)
				&& !heritsClass(param, File.class)
				&& !implementsInterface(param, IFileBased.class)) {
			return null;
		}
		// make an absolute path, relative to the sequence descriptor basedir
		if (!new File(sAttrVal).isAbsolute()) {
			File sBaseDir = Melody.getContext().getProcessorManager()
					.getSequenceDescriptor().getBaseDir();
			try {
				sAttrVal = new File(sBaseDir, sAttrVal).getCanonicalPath();
			} catch (IOException Ex) {
				throw new RuntimeException("IO error while get the Canonical "
						+ "Path of '" + sAttrVal + "'.", Ex);
			}
		}
		if (param == Path.class) {
			return Paths.get(sAttrVal);
		} else {
			return createNewObject(param, sAttrVal);
		}
	}

	private Object createNewObject(Class<?> param, String sAttrVal)
			throws TaskFactoryException {
		try {
			return param.getConstructor(String.class).newInstance(sAttrVal);
		} catch (NoSuchMethodException | InstantiationException
				| IllegalAccessException Ex) {
			throw new TaskFactoryException(Messages.bind(
					Messages.TaskFactoryEx_NO_CONSTRUCTOR_MATCH,
					param.getCanonicalName()));
		} catch (InvocationTargetException Ex) {
			throw new TaskFactoryException(Ex.getCause());
		}
	}

}