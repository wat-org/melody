package com.wat.melody.common.network.exception;

import com.wat.melody.common.ex.MelodyException;

public class IllegalAccessException extends MelodyException {

	private static final long serialVersionUID = -3214321446579432249L;

	public IllegalAccessException() {
		super();
	}

	public IllegalAccessException(String msg) {
		super(msg);
	}

	public IllegalAccessException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
