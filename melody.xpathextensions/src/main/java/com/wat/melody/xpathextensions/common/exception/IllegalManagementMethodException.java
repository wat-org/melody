package com.wat.melody.xpathextensions.common.exception;

import com.wat.melody.common.utils.exception.MelodyException;

public class IllegalManagementMethodException extends MelodyException {

	private static final long serialVersionUID = -412345389964589949L;

	public IllegalManagementMethodException() {
		super();
	}

	public IllegalManagementMethodException(String msg) {
		super(msg);
	}

	public IllegalManagementMethodException(Throwable cause) {
		super(cause);
	}

	public IllegalManagementMethodException(String msg, Throwable cause) {
		super(msg, cause);
	}

}