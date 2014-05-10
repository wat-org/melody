package com.wat.melody.api.exception;

import java.lang.reflect.Method;

import org.w3c.dom.Text;

import com.wat.melody.api.annotation.TextContent;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class TextContentRelatedException extends MethodRelatedException {

	private static final long serialVersionUID = -1384841123179867661L;

	public TextContentRelatedException(Text errorNode, Method errorMethod,
			String msg) {
		super(errorNode, errorMethod, msg);
	}

	public TextContentRelatedException(Text errorNode, Method errorMethod,
			String msg, Throwable cause) {
		super(errorNode, errorMethod, msg, cause);
	}

	@Override
	protected String getErrorMethodDescription() {
		TextContent t = getErrorMethod().getAnnotation(TextContent.class);
		if (t == null || t.description().length() == 0) {
			return null;
		}
		return t.description();
	}

}