package com.wat.melody.api.exception;

import java.lang.reflect.Method;

import org.w3c.dom.Node;

import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.common.xml.Doc;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class TaskFactoryAttributeException extends TaskFactoryException {

	private static final long serialVersionUID = -1384841123110684061L;

	private Node moErrorNode = null;
	private Method moErrorMethod = null;

	public TaskFactoryAttributeException(Node errorNode, Method errorMethod) {
		super();
		setErrorNode(errorNode);
		setErrorMethod(errorMethod);
	}

	public TaskFactoryAttributeException(Node errorNode, Method errorMethod,
			String msg) {
		super(msg);
		setErrorNode(errorNode);
		setErrorMethod(errorMethod);
	}

	public TaskFactoryAttributeException(Node errorNode, Method errorMethod,
			String msg, Throwable cause) {
		super(msg, cause);
		setErrorNode(errorNode);
		setErrorMethod(errorMethod);
	}

	public TaskFactoryAttributeException(Node errorNode, Method errorMethod,
			Throwable cause) {
		super(cause);
		setErrorNode(errorNode);
		setErrorMethod(errorMethod);
	}

	public Node getErrorNode() {
		return moErrorNode;
	}

	private void setErrorNode(Node errorNode) {
		if (errorNode == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Node.class.getCanonicalName() + ".");
		}
		moErrorNode = errorNode;
	}

	public Method getErrorMethod() {
		return moErrorMethod;
	}

	private void setErrorMethod(Method errorMethod) {
		if (errorMethod == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Method.class.getCanonicalName()
					+ ".");
		}
		moErrorMethod = errorMethod;
	}

	private static String getDescription(Method m) {
		Attribute a = m.getAnnotation(Attribute.class);
		if (a == null || a.description().length() == 0) {
			return null;
		}
		return a.description();
	}

	private static String formatDescription(String desc) {
		if (desc == null) {
			return "";
		}
		desc = "\n--------- DOC / HELP / TIP ---------\n" + desc;
		return desc.replaceAll("\n", "\n  ");
	}

	@Override
	public String getMessage() {
		String desc = formatDescription(getDescription(getErrorMethod()));
		return "[" + Doc.getNodeLocation(getErrorNode()) + "] "
				+ super.getMessage() + desc;
	}

}