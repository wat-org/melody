package com.wat.melody.common.network.exception;

import com.wat.melody.common.ex.MelodyException;

public class IllegalInterfaceException extends MelodyException {

	private static final long serialVersionUID = -2378988735678776532L;

	public IllegalInterfaceException() {
		super();
	}

	public IllegalInterfaceException(String msg) {
		super(msg);
	}

	public IllegalInterfaceException(String msg, Throwable cause) {
		super(msg, cause);
	}

}