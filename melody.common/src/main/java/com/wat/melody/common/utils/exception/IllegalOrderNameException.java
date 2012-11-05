package com.wat.melody.common.utils.exception;

public class IllegalOrderNameException extends MelodyException {

	private static final long serialVersionUID = -1223213213225787878L;

	public IllegalOrderNameException() {
		super();
	}

	public IllegalOrderNameException(String msg) {
		super(msg);
	}

	public IllegalOrderNameException(String msg, Throwable cause) {
		super(msg, cause);
	}

}