package com.wat.melody.common.order.exception;

import com.wat.melody.common.ex.MelodyException;

public class IllegalOrderNameSetException extends MelodyException {

	private static final long serialVersionUID = -1658406468403640602L;

	public IllegalOrderNameSetException() {
		super();
	}

	public IllegalOrderNameSetException(String msg) {
		super(msg);
	}

	public IllegalOrderNameSetException(String msg, Throwable cause) {
		super(msg, cause);
	}

}