package com.wat.melody.cloud.instance.exception;

import com.wat.melody.common.ex.MelodyException;

public class IllegalInstanceStateException extends MelodyException {

	private static final long serialVersionUID = -768976338086997771L;

	public IllegalInstanceStateException() {
		super();
	}

	public IllegalInstanceStateException(String msg) {
		super(msg);
	}

	public IllegalInstanceStateException(Throwable cause) {
		super(cause);
	}

	public IllegalInstanceStateException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
