package com.wat.melody.common.network.exception;

import com.wat.melody.common.utils.exception.MelodyException;

public class IllegalPortException extends MelodyException {

	private static final long serialVersionUID = -6435793678766540892L;

	public IllegalPortException() {
		super();
	}

	public IllegalPortException(String msg) {
		super(msg);
	}

	public IllegalPortException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
