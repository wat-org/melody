package com.wat.melody.common.network.exception;

import com.wat.melody.common.utils.exception.MelodyException;

public class IllegalHostException extends MelodyException {

	private static final long serialVersionUID = -1287076452458742159L;

	public IllegalHostException() {
		super();
	}

	public IllegalHostException(String msg) {
		super(msg);
	}

	public IllegalHostException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
