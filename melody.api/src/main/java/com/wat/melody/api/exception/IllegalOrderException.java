package com.wat.melody.api.exception;

import com.wat.melody.common.ex.MelodyException;

public class IllegalOrderException extends MelodyException {

	private static final long serialVersionUID = -1658406468403640602L;

	public IllegalOrderException() {
		super();
	}

	public IllegalOrderException(String msg) {
		super(msg);
	}

	public IllegalOrderException(String msg, Throwable cause) {
		super(msg, cause);
	}

}