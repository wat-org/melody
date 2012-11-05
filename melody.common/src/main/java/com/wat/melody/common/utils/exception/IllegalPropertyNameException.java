package com.wat.melody.common.utils.exception;

public class IllegalPropertyNameException extends MelodyException {

	private static final long serialVersionUID = -2309872605328438369L;

	public IllegalPropertyNameException() {
		super();
	}

	public IllegalPropertyNameException(String msg) {
		super(msg);
	}

	public IllegalPropertyNameException(Throwable cause) {
		super(cause);
	}

	public IllegalPropertyNameException(String msg, Throwable cause) {
		super(msg, cause);
	}

}