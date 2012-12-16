package com.wat.melody.cloud.management.exception;

import com.wat.melody.common.utils.exception.MelodyException;

public class ManagementException extends MelodyException {

	private static final long serialVersionUID = -498763289964378909L;

	public ManagementException() {
		super();
	}

	public ManagementException(String msg) {
		super(msg);
	}

	public ManagementException(Throwable cause) {
		super(cause);
	}

	public ManagementException(String msg, Throwable cause) {
		super(msg, cause);
	}

}