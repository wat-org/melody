package com.wat.melody.common.network.exception;

import com.wat.melody.common.ex.MelodyException;

public class IllegalPortRangeException extends MelodyException {

	private static final long serialVersionUID = -6321459665094698792L;

	public IllegalPortRangeException() {
		super();
	}

	public IllegalPortRangeException(String msg) {
		super(msg);
	}

	public IllegalPortRangeException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
