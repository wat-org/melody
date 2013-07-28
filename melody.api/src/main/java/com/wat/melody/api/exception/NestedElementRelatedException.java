package com.wat.melody.api.exception;

import java.lang.reflect.Method;

import org.w3c.dom.Element;

import com.wat.melody.api.annotation.NestedElement;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class NestedElementRelatedException extends MethodRelatedException {

	private static final long serialVersionUID = -2384841123110684061L;

	public NestedElementRelatedException(Element errorNode, Method errorMethod,
			String msg) {
		super(errorNode, errorMethod, msg);
	}

	public NestedElementRelatedException(Element errorNode, Method errorMethod,
			String msg, Throwable cause) {
		super(errorNode, errorMethod, msg, cause);
	}

	public NestedElementRelatedException(Element errorNode, Method errorMethod,
			Throwable cause) {
		super(errorNode, errorMethod, cause);
	}

	@Override
	protected String getErrorMethodDescription() {
		NestedElement n = getErrorMethod().getAnnotation(NestedElement.class);
		if (n == null || n.description().length() == 0) {
			return null;
		}
		return n.description();
	}

}