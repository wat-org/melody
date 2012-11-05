package com.wat.melody.api.exception;

import com.wat.melody.common.utils.exception.IllegalFilterException;

public class IllegalTargetFilterException extends IllegalFilterException {

	private static final long serialVersionUID = -3364876876087402681L;

	public IllegalTargetFilterException() {
		super();
	}

	public IllegalTargetFilterException(String msg) {
		super(msg);
	}

	public IllegalTargetFilterException(Throwable cause) {
		super(cause);
	}

	public IllegalTargetFilterException(String msg, Throwable cause) {
		super(msg, cause);
	}

}