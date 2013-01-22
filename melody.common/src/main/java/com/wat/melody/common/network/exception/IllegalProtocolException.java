package com.wat.melody.common.network.exception;

import com.wat.melody.common.ex.MelodyException;

public class IllegalProtocolException extends MelodyException {

	private static final long serialVersionUID = -3742148613234532249L;

	public IllegalProtocolException() {
		super();
	}

	public IllegalProtocolException(String msg) {
		super(msg);
	}

	public IllegalProtocolException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
