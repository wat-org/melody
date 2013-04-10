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
import com.wat.melody.api.IShareProperties;
import com.wat.melody.api.ITask;
import com.wat.melody.api.ITaskContainer;
import com.wat.melody.api.ITaskContext;
import com.wat.melody.api.ITopLevelTask;
import com.wat.melody.api.Messages;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.annotation.NestedElement;
import com.wat.melody.api.event.State;
import com.wat.melody.api.exception.ExpressionSyntaxException;
import com.wat.melody.api.exception.TaskException;
import com.wat.melody.api.exception.TaskFactoryAttributeException;
import com.wat.melody.api.exception.TaskFactoryException;
import com.wat.melody.api.exception.TaskFactoryNestedElementException;
import com.wat.melody.common.files.IFileBased;
import com.wat.melody.common.properties.PropertiesSet;
import com.wat.melody.common.xml.Doc;
import com.wat.melody.xpathextensions.XPathExpander;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class TaskFactory {

	public static final String BOOLEAN_PATTERN = "(?i)^\\s*(true|y(es)?|o(n)?)\\s*$";

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
	 */
	public static boolean implementsInterface(Class<?> c, Class<?> base) {
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
	 * Tests if the given subject class extends the required class, at any
	 * parent degrees.
	 * </p>
	 * 
	 * @param c
	 *            is the subject class.
	 * @param base
	 *            is the required class.
	 * 
	 * @return <code>true</code> if the given subject class extends the required
	 *         class, or <code>false</code> if not.
	 */
	public static boolean heritsClass(Class<?> c, Class<?> base) {
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
	 * <i> The setter's method should respect the following rules :<BR/>
	 * * must be <code>public</code>,<BR/>
	 * * must not be <code>abstract</code>,<BR/>
	 * * must have an {@link Attribute} annotation whose name is equal to the
	 * attribute name (no case match),<BR/>
	 * * must have 1 argument,<BR/>
	 * * the argument type must be public,<BR/>
	 * * the argument type must not be an abstract,<BR/>
	 * * the argument type must not be an array,<BR/>
	 * * the argument type must not be an interface.<BR/>
	 * </i>
	 * </p>
	 * 
	 * @param c
	 *            is the class where the search is done.
	 * @param sAttrName
	 *            is the name of searched attribute.
	 * 
	 * @return the setter's method which matches the given attribute name, or
	 *         <code>null</code> if no setter's method which matches the given
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
	 * <i> The add's method should respect the following rules :<BR/>
	 * * must be <code>public</code>,<BR/>
	 * * must not be <code>abstract</code>,<BR/>
	 * * must have an {@link NestedElement} annotation whose name is equal to
	 * the attribute name (no case match) and whose type is equal to
	 * {@link NestedElement.Type#ADD},<BR/>
	 * * must not return <code>void</code>,<BR/>
	 * * must have 1 argument,<BR/>
	 * * the argument type must be public,<BR/>
	 * * the argument type must not be an abstract,<BR/>
	 * * the argument type must not be an interface,<BR/>
	 * * the argument type must not be an enumeration,<BR/>
	 * * the argument type must not be an primitive,<BR/>
	 * * the argument type must not be an array,<BR/>
	 * * the argument type must have a public no-arg constructor.<BR/>
	 * </i>
	 * </p>
	 * 
	 * @param c
	 *            is the class where the search is done.
	 * @param sAttrName
	 *            is the name of searched attribute.
	 * 
	 * @return the add's method which matches the given attribute name, or
	 *         <code>null</code> if no add method which matches the given
	 *         attribute name can be found in the given class.
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
	 * <i> The create's method should respect the following rules :<BR/>
	 * * must be <code>public</code>,<BR/>
	 * * must not be an <code>abstract</code>,<BR/>
	 * * must have an {@link NestedElement} annotation whose name is equal to
	 * the attribute name (no case match) and whose type is equal to
	 * {@link NestedElement.Type#CREATE},<BR/>
	 * * must not return <code>void</code>,<BR/>
	 * * must have 0 argument.<BR/>
	 * </i>
	 * </p>
	 * 
	 * @param c
	 *            is the class where the search is done.
	 * @param sAttrName
	 *            is the name of searched attribute.
	 * 
	 * @return the create's method which matches the given attribute name, or
	 *         <code>null</code> if no add method which matches the given
	 *         attribute name can be found in the given class.
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

	private ProcessorManager moProcessorManager;
	private IRegisteredTasks moRegisteredTasks;

	/**
	 * <p>
	 * Create a new <code>TaskFactory</code> instance, which allow to create new
	 * <code>ITask</code> from Elements Nodes found in the Sequence Descriptor.
	 * </p>
	 * <p>
	 * Register all Tasks found in all jars loaded in the ClassPath
	 * </p>
	 * 
	 * @throws TaskFactoryException
	 *             if a Task registration fails (possible cause : multiple
	 *             classes that implements <code>Task</code> are found with the
	 *             same name).
	 * @throws IOException
	 *             if an IO error occurred while reading jars loaded in the
	 *             ClassPath.
	 * 
	 */
	public TaskFactory(ProcessorManager p) {
		setProcessorManager(p);
		setRegisteredTasks(new RegisteredTasks());
	}

	private ProcessorManager getProcessorManager() {
		return moProcessorManager;
	}

	private ProcessorManager setProcessorManager(ProcessorManager p) {
		if (p == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Processor.");
		}
		ProcessorManager previous = getProcessorManager();
		moProcessorManager = p;
		return previous;
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

	/**
	 * <p>
	 * Create a new Task, based on the given element node.
	 * </p>
	 * 
	 * <p>
	 * <i> * Will expand attribute values * During expansion process, the given
	 * PropertiesSet will be used. * PropertiesSet contains all variables
	 * name/values that can be used in expression. </i>
	 * </p>
	 * 
	 * @param n
	 *            is an element node, which represent the Task to create.
	 * @param ps
	 *            is a PropertiesSet, which will be used during expansion.
	 * @param p
	 *            is the parent Task.
	 * 
	 * @return a new object that implement Task.
	 * 
	 * @throws TaskFactoryException
	 *             if the node name doesn't match any registered task.
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
	 *             if a the given {@link Node} is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if a the {@link PropertiesSet} is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if a the given {@link Node} is not an XML Element.
	 * 
	 */
	public synchronized ITask newTask(Node n, PropertiesSet ps)
			throws TaskFactoryException {
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

		Class<? extends ITask> c = findTaskClass(n);
		validateTaskHierarchy(c, n.getParentNode());
		// Duplicate the PropertiesSet, so the Task can work with its own
		// PropertiesSet
		// Doesn't apply to ITask which implements IShareProperties
		PropertiesSet ownPs = implementsInterface(c, IShareProperties.class) ? ps
				: ps.copy();
		ITaskContext tc = new TaskContext(n, ownPs, getProcessorManager());
		CoreThread.currentCoreThread().pushContext(tc);

		ITask t = null;
		try {
			t = c.newInstance();
		} catch (InstantiationException | IllegalAccessException Ex) {
			throw new RuntimeException("Unexpected error while creating a "
					+ "Task by reflection. "
					+ "Because a public no-argument constructor exists "
					+ "(see 'registerTasks'), such error cannot happened. "
					+ "Source code has certainly been modified and a "
					+ "bug have been introduced.", Ex);
		}

		setAllMembers(t, n.getAttributes(), n.getNodeName(), ownPs);
		setAllNestedElements(t, n.getChildNodes(), n.getNodeName(), ownPs);
		try {
			t.validate();
		} catch (TaskException Ex) {
			throw new TaskFactoryException(Ex);
		}
		return t;
	}

	private void validateTaskHierarchy(Class<?> c, Node parentNode)
			throws TaskFactoryException {
		Class<?> p = null;
		if (parentNode != null
				&& parentNode.getNodeType() != Node.DOCUMENT_NODE) {
			try {
				p = findTaskClass(parentNode);
			} catch (TaskFactoryException Ex) {
				throw new RuntimeException("Unexpected error while finding "
						+ "the Task Class which match '" + parentNode + "'. "
						+ "Because this Task is a parent Task, meaning that "
						+ "it had already been created, such error cannot "
						+ "happened. "
						+ "Source code has certainly been modified and a bug "
						+ "have been introduced.", Ex);
			}
		}
		String sName = c.getName().toLowerCase();
		String sPName = parentNode.getNodeName().toLowerCase();
		if (implementsInterface(c, ITopLevelTask.class) && p != null) {
			throw new TaskFactoryException(Messages.bind(
					Messages.TaskFactoryEx_TOPLEVEL_ERROR, sName));
		} else if (implementsInterface(c, IFirstLevelTask.class) && p != null
				&& !implementsInterface(p, ITopLevelTask.class)) {
			throw new TaskFactoryException(Messages.bind(
					Messages.TaskFactoryEx_FIRSTLEVEL_ERROR, sName, sPName));
		} else if (!implementsInterface(c, IFirstLevelTask.class) && p != null
				&& implementsInterface(p, ITopLevelTask.class)) {
			throw new TaskFactoryException(Messages.bind(
					Messages.TaskFactoryEx_CHILD_ERROR, sName, sPName));
		}
	}

	private void setAllMembers(Object base, NamedNodeMap attrs,
			String sNodeName, PropertiesSet ps) throws TaskFactoryException {
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
					sAttrVal = XPathExpander.expand(attr.getNodeValue(),
							getProcessorManager().getResourcesDescriptor()
									.getDocument().getFirstChild(), ps);
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
			String sNodeName, PropertiesSet ps) throws TaskFactoryException {
		if (nestedNodes == null) {
			return;
		}
		for (int i = 0; i < nestedNodes.getLength(); i++) {
			Node n = nestedNodes.item(i);
			if (n.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			if (addNestedElement(base, n, ps)
					|| createNestedElement(base, n, ps)
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

	private boolean addNestedElement(Object base, Node n, PropertiesSet ps)
			throws TaskFactoryException {
		Method m = findAddMethod(base.getClass(), n.getNodeName());
		if (m == null) {
			return false;
		}
		try {
			Object o;
			try {
				o = m.getParameterTypes()[0].newInstance();
			} catch (InstantiationException | IllegalAccessException Ex) {
				throw new RuntimeException("Unexpected error while adding a "
						+ "nested element by reflection. "
						+ "Source code has certainly been modified and a bug "
						+ "have been introduced.", Ex);
			}

			setAllMembers(o, n.getAttributes(), n.getNodeName(), ps);
			setAllNestedElements(o, n.getChildNodes(), n.getNodeName(), ps);

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

	private boolean createNestedElement(Object base, Node n, PropertiesSet ps)
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

			setAllMembers(o, n.getAttributes(), n.getNodeName(), ps);
			setAllNestedElements(o, n.getChildNodes(), n.getNodeName(), ps);
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
			((ITaskContainer) base).addNode(n);
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
			o = sAttrVal.matches(BOOLEAN_PATTERN);
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
		if (!(param == Path.class) && !heritsClass(param, File.class)
				&& !implementsInterface(param, IFileBased.class)) {
			return null;
		}
		// make an absolute path, relative to the sequence descriptor basedir
		if (!new File(sAttrVal).isAbsolute()) {
			File sBaseDir = getProcessorManager().getSequenceDescriptor()
					.getBaseDir();
			try {
				sAttrVal = new File(sBaseDir, sAttrVal).getCanonicalPath();
			} catch (IOException Ex) {
				throw new RuntimeException("IO error while get the Canonical "
						+ "Path of '" + sAttrVal + "'.", Ex);
			}
		}
		if (param == Path.class) {
			return Paths.get(sAttrVal);
		}
		return createNewObject(param, sAttrVal);
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