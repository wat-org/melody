package com.wat.melody.common.xpath.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class ExpressionSyntaxException extends MelodyException {

	private static final long serialVersionUID = -4894096879684300065L;

	public ExpressionSyntaxException() {
		super();
	}

	public ExpressionSyntaxException(String msg) {
		super(msg);
	}

	public ExpressionSyntaxException(Throwable cause) {
		super(cause);
	}

	public ExpressionSyntaxException(String msg, Throwable cause) {
		super(msg, cause);
	}

}