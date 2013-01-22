package com.wat.melody.cloud.instance.exception;

import com.wat.melody.common.ex.MelodyException;

public class IllegalInstanceTypeException extends MelodyException {

	private static final long serialVersionUID = 127998078020202109L;

	public IllegalInstanceTypeException() {
		super();
	}

	public IllegalInstanceTypeException(String msg) {
		super(msg);
	}

	public IllegalInstanceTypeException(Throwable cause) {
		super(cause);
	}

	public IllegalInstanceTypeException(String msg, Throwable cause) {
		super(msg, cause);
	}

}