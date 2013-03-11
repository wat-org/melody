package com.wat.melody.xpathextensions.exception;

import com.wat.melody.api.exception.ExpressionSyntaxException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class XPathExpressionSyntaxException extends ExpressionSyntaxException {

	private static final long serialVersionUID = -6987403216007303202L;

	public XPathExpressionSyntaxException() {
		super();
	}

	public XPathExpressionSyntaxException(String msg) {
		super(msg);
	}

	public XPathExpressionSyntaxException(Throwable cause) {
		super(cause);
	}

	public XPathExpressionSyntaxException(String msg, Throwable cause) {
		super(msg, cause);
	}

}