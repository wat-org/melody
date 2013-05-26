package com.wat.melody.api.exception;

import java.lang.reflect.Method;

import org.w3c.dom.Node;

import com.wat.melody.api.annotation.Attribute;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class AttributeRelatedException extends MethodRelatedException {

	private static final long serialVersionUID = -1384841123179867661L;

	public AttributeRelatedException(Node errorNode, Method errorMethod,
			String msg) {
		super(errorNode, errorMethod, msg);
	}

	public AttributeRelatedException(Node errorNode, Method errorMethod,
			String msg, Throwable cause) {
		super(errorNode, errorMethod, msg, cause);
	}

	public AttributeRelatedException(Node errorNode, Method errorMethod,
			Throwable cause) {
		super(errorNode, errorMethod, cause);
	}

	@Override
	protected String getErrorMethodDescription() {
		Attribute a = getErrorMethod().getAnnotation(Attribute.class);
		if (a == null || a.description().length() == 0) {
			return null;
		}
		return a.description();
	}

}