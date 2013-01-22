package com.wat.melody.common.network.exception;

import com.wat.melody.common.ex.MelodyException;

public class IllegalProtocolsException extends MelodyException {

	private static final long serialVersionUID = -2121387111974466632L;

	public IllegalProtocolsException() {
		super();
	}

	public IllegalProtocolsException(String msg) {
		super(msg);
	}

	public IllegalProtocolsException(String msg, Throwable cause) {
		super(msg, cause);
	}

}