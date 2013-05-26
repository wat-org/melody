package com.wat.melody.api.exception;

import java.lang.reflect.Method;

import org.w3c.dom.Node;

import com.wat.melody.common.xml.exception.SimpleNodeRelatedException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class MethodRelatedException extends SimpleNodeRelatedException {

	private static final long serialVersionUID = -4564655764234325761L;

	private Method _errorMethod = null;

	public MethodRelatedException(Node errorNode, Method errorMethod, String msg) {
		super(errorNode, msg);
		setErrorMethod(errorMethod);
	}

	public MethodRelatedException(Node errorNode, Method errorMethod,
			Throwable cause) {
		super(errorNode, cause);
		setErrorMethod(errorMethod);
	}

	public MethodRelatedException(Node errorNode, Method errorMethod,
			String msg, Throwable cause) {
		super(errorNode, msg, cause);
		setErrorMethod(errorMethod);
	}

	public Method getErrorMethod() {
		return _errorMethod;
	}

	private void setErrorMethod(Method errorMethod) {
		if (errorMethod == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Method.class.getCanonicalName()
					+ ".");
		}
		_errorMethod = errorMethod;
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
		String desc = formatDescription(getErrorMethodDescription());
		return super.getMessage() + desc;
	}

	protected abstract String getErrorMethodDescription();

}