package com.wat.melody.core.internal;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.wat.melody.api.IFirstLevelTask;
import com.wat.melody.api.IRegisteredTasks;
import com.wat.melody.api.ITask;
import com.wat.melody.api.ITaskBuilder;
import com.wat.melody.api.ITaskContainer;
import com.wat.melody.api.ITopLevelTask;
import com.wat.melody.api.IUnexpectedAttributes;
import com.wat.melody.api.Melody;
import com.wat.melody.api.Messages;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.annotation.NestedElement;
import com.wat.melody.api.annotation.TextContent;
import com.wat.melody.api.event.State;
import com.wat.melody.api.exception.AttributeRelatedException;
import com.wat.melody.api.exception.NestedElementRelatedException;
import com.wat.melody.api.exception.TaskException;
import com.wat.melody.api.exception.TaskFactoryException;
import com.wat.melody.api.exception.TextContentRelatedException;
import com.wat.melody.common.bool.Bool;
import com.wat.melody.common.bool.exception.IllegalBooleanException;
import com.wat.melody.common.files.IFileBased;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.properties.PropertySet;
import com.wat.melody.common.reflection.ReflectionHelper;
import com.wat.melody.common.xml.DocHelper;
import com.wat.melody.common.xml.exception.SimpleNodeRelatedException;
import com.wat.melody.common.xpath.exception.ExpressionSyntaxException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class TaskFactory {

	private static Logger log = LoggerFactory.getLogger(TaskFactory.class);

	private IRegisteredTasks _registeredTasks;
	private TaskFactoryCache _cache;

	/**
	 * <p>
	 * Create a new {@link TaskFactory} object, which allow to create new
	 * {@link ITask} objects from {@link Element} Nodes found in the Sequence
	 * Descriptor.
	 * </p>
	 */
	public TaskFactory() {
		setRegisteredTasks(new RegisteredTasks());
		setCache(new TaskFactoryCache());
	}

	public IRegisteredTasks getRegisteredTasks() {
		return _registeredTasks;
	}

	public IRegisteredTasks setRegisteredTasks(IRegisteredTasks rts) {
		if (rts == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ IRegisteredTasks.class.getCanonicalName() + ".");
		}
		IRegisteredTasks previous = getRegisteredTasks();
		_registeredTasks = rts;
		return previous;
	}

	public TaskFactoryCache getCache() {
		return _cache;
	}

	public TaskFactoryCache setCache(TaskFactoryCache rts) {
		if (rts == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ TaskFactoryCache.class.getCanonicalName() + ".");
		}
		TaskFactoryCache previous = getCache();
		_cache = rts;
		return previous;
	}

	private Method findSetMethod(Class<?> c, String attrName)
			throws TaskFactoryException {
		return getCache().getSetMethod(c, attrName);
	}

	private Method findTextMethod(Class<?> c) throws TaskFactoryException {
		return getCache().getTextMethod(c);
	}

	private Method findAddMethod(Class<?> c, String elmtName)
			throws TaskFactoryException {
		return getCache().getAddMethod(c, elmtName);
	}

	private Method findCreateMethod(Class<?> c, String elmtName)
			throws TaskFactoryException {
		return getCache().getCreateMethod(c, elmtName);
	}

	/**
	 * <p>
	 * Identify the {@link ITaskBuilder} which correspond to the given task (in
	 * its native {@link Element} format).
	 * </p>
	 * 
	 * @param elmt
	 *            is a task (in its native {@link Element} format).
	 * 
	 * @return an {@link ITaskBuilder}, which can build the given task.
	 * 
	 * @throws TaskFactoryException
	 *             if the given {@link element} is doesn't represent an
	 *             {@link ITask}.
	 * @throws TaskFactoryException
	 *             if the Task hierarchy is not valid.
	 * @throws IllegalArgumentException
	 *             if a the given {@link Element} is <tt>null</tt>.
	 */
	public ITaskBuilder identifyTask(Element elmt, PropertySet ps)
			throws TaskFactoryException {
		if (elmt == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Element.class.getCanonicalName()
					+ ").");
		}

		ITaskBuilder tb = findTaskClass(elmt, ps);
		Class<? extends ITask> c = tb.getTaskClass();
		validateTaskHierarchy(c, elmt.getParentNode(), ps);
		return tb;
	}

	/**
	 * <p>
	 * Search the {@link ITaskBuilder} which corresponds to the given task (in
	 * its native {@link Element} format).
	 * </p>
	 * 
	 * @param elmt
	 *            is a task (in its native {@link Element} format).
	 * 
	 * @return a {@link ITaskBuilder} object, which can be use to instantiate an
	 *         {@link ITask}.
	 * 
	 * @throws TaskFactoryException
	 *             if the given {@link element} is doesn't represent an
	 *             {@link ITask}.
	 */
	private ITaskBuilder findTaskClass(Element elmt, PropertySet ps)
			throws TaskFactoryException {
		String sSimpleName = elmt.getNodeName();
		ITaskBuilder t = getRegisteredTasks().retrieveEligibleTaskBuilder(
				sSimpleName, elmt, ps);
		if (t == null) {
			throw new TaskFactoryException(Msg.bind(
					Messages.TaskFactoryEx_UNDEF_TASK, sSimpleName));
		}
		// some attributes are only useful for 'conditional behavior'.
		// here, all attributes used to find the task are marked as 'already visited'
		t.markEligibleElements(elmt, ps);
		
		return t;
	}

	private void validateTaskHierarchy(Class<?> c, Node parentNode,
			PropertySet ps) throws TaskFactoryException {
		// parentNode can be a DOCUMENT_NODE or an ELEMENT_NODE
		Class<?> p = null;
		if (parentNode != null && parentNode.getNodeType() == Node.ELEMENT_NODE) {
			try {
				p = findTaskClass((Element) parentNode, ps).getTaskClass();
			} catch (TaskFactoryException Ex) {
				throw new RuntimeException("Unexpected error while searching "
						+ "the Task Class which match the Element Node ["
						+ DocHelper.getNodeLocation(parentNode) + "]. "
						+ "Because this Task already been created, "
						+ "such error cannot happened. "
						+ "Source code has certainly been modified and a bug "
						+ "have been introduced.", Ex);
			}
		}
		if (ReflectionHelper.implement(c, ITopLevelTask.class) && p != null) {
			throw new TaskFactoryException(Msg.bind(
					Messages.TaskFactoryEx_MUST_BE_TOPLEVEL, c.getName()));
		} else if (!ReflectionHelper.implement(c, ITopLevelTask.class)
				&& p == null) {
			throw new TaskFactoryException(Msg.bind(
					Messages.TaskFactoryEx_CANNOT_BE_TOPLEVEL, c.getName()));
		} else if (ReflectionHelper.implement(c, IFirstLevelTask.class)
				&& p != null
				&& !ReflectionHelper.implement(p, ITopLevelTask.class)) {
			throw new TaskFactoryException(Msg.bind(
					Messages.TaskFactoryEx_MUST_BE_FIRSTLEVEL, c.getName(),
					p.getName()));
		} else if (!ReflectionHelper.implement(c, IFirstLevelTask.class)
				&& p != null
				&& ReflectionHelper.implement(p, ITopLevelTask.class)) {
			throw new TaskFactoryException(Msg.bind(
					Messages.TaskFactoryEx_CANNOT_BE_FIRSTLEVEL, c.getName(),
					p.getName()));
		}
	}

	/**
	 * <p>
	 * Create a new {@link ITask}, based on the given task.
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
	 *            is the class of the {@link ITask} to create.
	 * @param elmt
	 *            is the task to create (in its native {@link Element} format).
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
	 *             if a the given {@link Element} is <tt>null</tt>.
	 */
	public ITask newTask(ITaskBuilder tb, Element elmt)
			throws TaskFactoryException {
		if (tb == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ITaskBuilder.class.getCanonicalName() + ".");
		}
		if (elmt == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Element.class.getCanonicalName()
					+ ".");
		}

		ITask t = tb.build();

		synchronized (elmt.getOwnerDocument()) {
			setAllMembers(t, elmt.getAttributes());
			setTextContent(t, elmt.getChildNodes());
			setAllNestedElements(t, elmt.getChildNodes());
		}
		try {
			t.validate();
		} catch (TaskException Ex) {
			throw new TaskFactoryException(Ex);
		}
		return t;
	}

	private void setAllMembers(Object base, NamedNodeMap attrs)
			throws TaskFactoryException {
		if (attrs == null) {
			detectsUndefinedMandatoryAttributes(base.getClass(), attrs);
			return;
		}
		for (int i = 0; i < attrs.getLength(); i++) {
			Attr attr = (Attr) attrs.item(i);
			String sAttrName = attr.getNodeName();
			Method m = findSetMethod(base.getClass(), sAttrName);
			if (m != null) {
				String value = attr.getNodeValue();
				try {
					value = Melody.getContext().expand(value);
				} catch (ExpressionSyntaxException Ex) {
					throw new TaskFactoryException(
							new AttributeRelatedException(attr, m, Msg.bind(
									Messages.TaskFactoryEx_EXPAND_ATTR,
									sAttrName, State.FAILED), Ex));
				} catch (Throwable Ex) {
					throw new TaskFactoryException(
							new AttributeRelatedException(attr, m, Msg.bind(
									Messages.TaskFactoryEx_EXPAND_ATTR,
									sAttrName, State.CRITICAL), Ex));
				}
				setMember(base, m, m.getParameterTypes()[0], attr, value);
			} else {
				// when some attributes are only useful for 'conditional behavior'.
				// we have detected them previously. we 
				// don't want such attribute to be consider as invalid.
				// or when this specific task can have unexpected attributes.
				if (attr.getUserData("eligible") == null &&
						!ReflectionHelper.implement(base.getClass(),
								IUnexpectedAttributes.class)){
					throw new TaskFactoryException(
							new SimpleNodeRelatedException(attr, Msg
									.bind(Messages.TaskFactoryMsg_INVALID_ATTR,
											sAttrName)));
					}
			}
		}
		detectsUndefinedMandatoryAttributes(base.getClass(), attrs);
	}

	private void detectsUndefinedMandatoryAttributes(Class<?> base,
			NamedNodeMap attrs) throws TaskFactoryException {
		// Detect if all mandatory attribute are defined
		for (Method m : base.getMethods()) {
			// Look for the annotation TaskAttrbiute
			Attribute a = ReflectionHelper.getAnnotation(m, Attribute.class);
			if (a != null && a.mandatory()) {
				boolean found = false;
				for (int i = 0; i < attrs.getLength(); i++) {
					if (a.name().equalsIgnoreCase(attrs.item(i).getNodeName())) {
						found = true;
						break;
					}
				}
				if (!found) {
					throw new TaskFactoryException(Msg.bind(
							Messages.TaskFactoryEx_MANDATORY_ATTR_NOT_FOUND,
							a.name()));
				}
			}
		}
	}

	private void setTextContent(Object base, NodeList nestedNodes)
			throws TaskFactoryException {
		// Ignore if child elements exists
		if (nestedNodes == null || nestedNodes.getLength() != 1
				|| nestedNodes.item(0).getNodeType() != Node.TEXT_NODE) {
			detectsUndefinedMandatoryText(base.getClass(), nestedNodes);
			return;
		}
		Text text = (Text) nestedNodes.item(0);
		Method m = findTextMethod(base.getClass());
		if (m != null) {
			String value = text.getNodeValue();
			try {
				value = Melody.getContext().expand(value);
			} catch (ExpressionSyntaxException Ex) {
				throw new TaskFactoryException(new TextContentRelatedException(
						text, m, Msg.bind(Messages.TaskFactoryEx_EXPAND_TEXT,
								State.FAILED), Ex));
			} catch (Throwable Ex) {
				throw new TaskFactoryException(new TextContentRelatedException(
						text, m, Msg.bind(Messages.TaskFactoryEx_EXPAND_TEXT,
								State.CRITICAL), Ex));
			}
			setText(base, m, m.getParameterTypes()[0], text, value);
		} else {
			throw new TaskFactoryException(Messages.TaskFactoryEx_INVALID_TEXT);
		}
		detectsUndefinedMandatoryText(base.getClass(), nestedNodes);
	}

	private void detectsUndefinedMandatoryText(Class<?> base,
			NodeList nestedNodes) throws TaskFactoryException {
		Method m = findTextMethod(base);
		if (m == null) {
			return;
		}
		if (!ReflectionHelper.getAnnotation(m, TextContent.class).mandatory()) {
			return;
		}
		if (nestedNodes == null || nestedNodes.getLength() != 1
				|| nestedNodes.item(0).getNodeType() != Node.TEXT_NODE) {
			throw new TaskFactoryException(
					Messages.TaskFactoryEx_MANDATORY_TEXT_NOT_FOUND);

		}
	}

	private void setAllNestedElements(Object base, NodeList nestedNodes)
			throws TaskFactoryException {
		if (nestedNodes == null) {
			detectsUndefinedMandatoryNestedElements(base.getClass(),
					nestedNodes);
			return;
		}
		for (int i = 0; i < nestedNodes.getLength(); i++) {
			if (nestedNodes.item(i).getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			Element n = (Element) nestedNodes.item(i);
			if (addNestedElement(base, n) || createNestedElement(base, n)
					|| registerInnerTask(base, n)) {
				continue;
			}
			log.info(new TaskFactoryException(
					new SimpleNodeRelatedException(n, Msg.bind(
							Messages.TaskFactoryEx_INVALID_NE, n.getNodeName())))
					.getUserFriendlyStackTrace());
		}
		detectsUndefinedMandatoryNestedElements(base.getClass(), nestedNodes);
	}

	private void detectsUndefinedMandatoryNestedElements(Class<?> base,
			NodeList nestedNodes) throws TaskFactoryException {
		for (Method m : base.getMethods()) {
			NestedElement a = ReflectionHelper.getAnnotation(m,
					NestedElement.class);
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
					throw new TaskFactoryException(Msg.bind(
							Messages.TaskFactoryEx_MANDATORY_NE_NOT_FOUND,
							a.name()));
				}
			}
		}
	}

	private boolean addNestedElement(Object base, Element elmt)
			throws TaskFactoryException {
		Method m = findAddMethod(base.getClass(), elmt.getNodeName());
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

			setAllMembers(o, elmt.getAttributes());
			setTextContent(o, elmt.getChildNodes());
			setAllNestedElements(o, elmt.getChildNodes());

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
			throw new TaskFactoryException(new NestedElementRelatedException(
					elmt, m, Msg.bind(Messages.TaskFactoryEx_SET_NE, elmt
							.getNodeName().toLowerCase(), State.FAILED), Ex));
		} catch (Throwable Ex) {
			throw new TaskFactoryException(new NestedElementRelatedException(
					elmt, m, Msg.bind(Messages.TaskFactoryEx_SET_NE, elmt
							.getNodeName().toLowerCase(), State.CRITICAL), Ex));
		}

		return true;
	}

	private boolean createNestedElement(Object base, Element elmt)
			throws TaskFactoryException {
		Method m = findCreateMethod(base.getClass(), elmt.getNodeName());
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
			/*
			 * Verify that the object returned by the Create method is public.
			 */
			if (!Modifier.isPublic(o.getClass().getModifiers())) {
				throw new TaskFactoryException(Msg.bind(
						Messages.TaskFactoryEx_CREATE_NE_SPEC_RT_CONFLICT, m,
						elmt.getNodeName(), o.getClass().getCanonicalName()));
			}

			setAllMembers(o, elmt.getAttributes());
			setTextContent(o, elmt.getChildNodes());
			setAllNestedElements(o, elmt.getChildNodes());
		} catch (TaskFactoryException Ex) {
			throw new TaskFactoryException(new NestedElementRelatedException(
					elmt, m, Msg.bind(Messages.TaskFactoryEx_SET_NE, elmt
							.getNodeName().toLowerCase(), State.FAILED), Ex));
		} catch (Throwable Ex) {
			throw new TaskFactoryException(new NestedElementRelatedException(
					elmt, m, Msg.bind(Messages.TaskFactoryEx_SET_NE, elmt
							.getNodeName().toLowerCase(), State.CRITICAL), Ex));
		}

		return true;
	}

	private boolean registerInnerTask(Object base, Element elmt)
			throws TaskFactoryException {
		if (!ReflectionHelper.implement(base.getClass(), ITaskContainer.class)) {
			return false;
		}
		try {
			((ITaskContainer) base).registerInnerTask(elmt);
		} catch (TaskException Ex) {
			throw new TaskFactoryException(Ex);
		}
		return true;
	}

	private void setMember(Object base, Method m, Class<?> param, Attr attr,
			String attrVal) throws TaskFactoryException {
		String attrName = attr.getNodeName();
		Object o = null;
		try {
			o = createNewPrimitiveType(param, attrVal);
			if (o == null) {
				o = createNewEnumConstant(param, attrVal);
			}
			if (o == null) {
				o = createNewFileBased(param, attrVal);
			}
			if (o == null) {
				o = createNewObject(param, attrVal);
			}
		} catch (TaskFactoryException Ex) {
			throw new TaskFactoryException(new AttributeRelatedException(attr,
					m, Msg.bind(Messages.TaskFactoryEx_CREATE_ATTR, attrName,
							State.FAILED), Ex));
		} catch (Throwable Ex) {
			throw new TaskFactoryException(new AttributeRelatedException(attr,
					m, Msg.bind(Messages.TaskFactoryEx_CREATE_ATTR, attrName,
							State.CRITICAL), Ex));
		}

		try {
			m.invoke(base, o);
		} catch (IllegalAccessException | IllegalArgumentException Ex) {
			throw new RuntimeException("Unexpected error while setting an "
					+ "attribute by reflection. "
					+ "Source code has certainly been modified and a bug "
					+ "have been introduced.", Ex);
		} catch (InvocationTargetException Ex) {
			throw new TaskFactoryException(new AttributeRelatedException(attr,
					m, Msg.bind(Messages.TaskFactoryEx_SET_ATTR, attrName,
							State.FAILED), Ex.getCause()));
		} catch (Throwable Ex) {
			throw new TaskFactoryException(new AttributeRelatedException(attr,
					m, Msg.bind(Messages.TaskFactoryEx_SET_ATTR, attrName,
							State.CRITICAL), Ex));
		}
	}

	private void setText(Object base, Method m, Class<?> param, Text text,
			String textVal) throws TaskFactoryException {
		Object o = null;
		try {
			o = createNewPrimitiveType(param, textVal);
			if (o == null) {
				o = createNewEnumConstant(param, textVal);
			}
			if (o == null) {
				o = createNewFileBased(param, textVal);
			}
			if (o == null) {
				o = createNewObject(param, textVal);
			}
		} catch (TaskFactoryException Ex) {
			throw new TaskFactoryException(new TextContentRelatedException(
					text, m, Msg.bind(Messages.TaskFactoryEx_CREATE_TEXT,
							State.FAILED), Ex));
		} catch (Throwable Ex) {
			throw new TaskFactoryException(new TextContentRelatedException(
					text, m, Msg.bind(Messages.TaskFactoryEx_CREATE_TEXT,
							State.CRITICAL), Ex));
		}

		try {
			m.invoke(base, o);
		} catch (IllegalAccessException | IllegalArgumentException Ex) {
			throw new RuntimeException("Unexpected error while setting an "
					+ "attribute by reflection. "
					+ "Source code has certainly been modified and a bug "
					+ "have been introduced.", Ex);
		} catch (InvocationTargetException Ex) {
			throw new TaskFactoryException(new TextContentRelatedException(
					text, m, Msg.bind(Messages.TaskFactoryEx_SET_TEXT,
							State.FAILED), Ex.getCause()));
		} catch (Throwable Ex) {
			throw new TaskFactoryException(new TextContentRelatedException(
					text, m, Msg.bind(Messages.TaskFactoryEx_SET_TEXT,
							State.CRITICAL), Ex));
		}
	}

	private Object createNewPrimitiveType(Class<?> param, String value)
			throws TaskFactoryException {
		if (!param.isPrimitive()) {
			return null;
		}

		Object o = null;
		if (param == Boolean.TYPE) {
			try {
				o = Bool.parseString(value);
			} catch (IllegalBooleanException e) {
				throw new TaskFactoryException(Msg.bind(
						Messages.TaskFactoryEx_CONVERT_ATTR, value,
						Boolean.class.getSimpleName()));
			}
		} else if (param == Character.TYPE) {
			if (value != null && value.length() == 1) {
				o = value.charAt(0);
			} else {
				throw new TaskFactoryException(Msg.bind(
						Messages.TaskFactoryEx_CONVERT_ATTR, value,
						Character.class.getSimpleName()));
			}
		} else if (param == Byte.TYPE) {
			try {
				o = Byte.parseByte(value);
			} catch (NumberFormatException Ex) {
				throw new TaskFactoryException(Msg.bind(
						Messages.TaskFactoryEx_CONVERT_ATTR, value,
						Byte.class.getSimpleName()));
			}
		} else if (param == Short.TYPE) {
			try {
				o = Short.parseShort(value);
			} catch (NumberFormatException Ex) {
				throw new TaskFactoryException(Msg.bind(
						Messages.TaskFactoryEx_CONVERT_ATTR, value,
						Short.class.getSimpleName()));
			}
		} else if (param == Integer.TYPE) {
			try {
				o = Integer.parseInt(value);
			} catch (NumberFormatException Ex) {
				throw new TaskFactoryException(Msg.bind(
						Messages.TaskFactoryEx_CONVERT_ATTR, value,
						Integer.class.getSimpleName()));
			}
		} else if (param == Long.TYPE) {
			try {
				o = Long.parseLong(value);
			} catch (NumberFormatException Ex) {
				throw new TaskFactoryException(Msg.bind(
						Messages.TaskFactoryEx_CONVERT_ATTR, value,
						Long.class.getSimpleName()));
			}
		} else if (param == Float.TYPE) {
			try {
				o = Float.parseFloat(value);
			} catch (NumberFormatException Ex) {
				throw new TaskFactoryException(Msg.bind(
						Messages.TaskFactoryEx_CONVERT_ATTR, value,
						Float.class.getSimpleName()));
			}
		} else if (param == Double.TYPE) {
			try {
				o = Double.parseDouble(value);
			} catch (NumberFormatException Ex) {
				throw new TaskFactoryException(Msg.bind(
						Messages.TaskFactoryEx_CONVERT_ATTR, value,
						Double.class.getSimpleName()));
			}
		}
		return o;
	}

	private Object createNewEnumConstant(Class<?> param, String value)
			throws TaskFactoryException {
		if (!param.isEnum()) {
			return null;
		}

		for (Object v : param.getEnumConstants()) {
			if (v.toString().equalsIgnoreCase(value)) {
				return v;
			}
		}
		throw new TaskFactoryException(Msg.bind(
				Messages.TaskFactoryEx_CONVERT_ATTR_TO_ENUM, value,
				Arrays.asList(param.getEnumConstants())));
	}

	private Object createNewFileBased(Class<?> param, String value)
			throws TaskFactoryException {
		if (!ReflectionHelper.implement(param, IFileBased.class)) {
			return null;
		}
		// make an absolute path, relative to the sequence descriptor basedir
		if (!new File(value).isAbsolute()) {
			File sBaseDir = Melody.getContext().getProcessorManager()
					.getSequenceDescriptor().getBaseDir();
			try {
				value = new File(sBaseDir, value).getCanonicalPath();
			} catch (IOException Ex) {
				throw new RuntimeException("IO error while get the Canonical "
						+ "Path of '" + value + "'.", Ex);
			}
		}
		return createNewObject(param, value);
	}

	private Object createNewObject(Class<?> param, String value)
			throws TaskFactoryException {
		try {
			return param.getConstructor(String.class).newInstance(value);
		} catch (NoSuchMethodException | InstantiationException
				| IllegalAccessException Ex) {
			throw new TaskFactoryException(Msg.bind(
					Messages.TaskFactoryEx_NO_CONSTRUCTOR_MATCH,
					param.getCanonicalName()));
		} catch (InvocationTargetException Ex) {
			throw new TaskFactoryException(Ex.getCause());
		}
	}

}