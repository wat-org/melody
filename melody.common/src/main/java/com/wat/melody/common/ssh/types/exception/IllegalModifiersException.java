package com.wat.melody.common.ssh.types.exception;

import com.wat.melody.common.utils.exception.MelodyException;

public class IllegalModifiersException extends MelodyException {

	private static final long serialVersionUID = -6543122347878787772L;

	public IllegalModifiersException() {
		super();
	}

	public IllegalModifiersException(String msg) {
		super(msg);
	}

	public IllegalModifiersException(String msg, Throwable cause) {
		super(msg, cause);
	}

}