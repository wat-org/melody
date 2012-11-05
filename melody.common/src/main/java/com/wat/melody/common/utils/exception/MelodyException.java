package com.wat.melody.common.utils.exception;

public class MelodyException extends Exception {

	private static final long serialVersionUID = -1184066155132415814L;

	public MelodyException() {
		super();
	}

	public MelodyException(String msg) {
		super(msg);
	}

	public MelodyException(Throwable cause) {
		super(null, cause);
	}

	public MelodyException(String msg, Throwable cause) {
		super(msg, cause);
	}

}