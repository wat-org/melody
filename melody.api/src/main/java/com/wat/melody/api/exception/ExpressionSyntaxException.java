package com.wat.melody.api.exception;

import com.wat.melody.common.ex.MelodyException;

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