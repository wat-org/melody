package com.wat.melody.cloud.management.exception;

import com.wat.melody.common.utils.exception.MelodyException;

public class IllegalManagementInfosException extends MelodyException {

	private static final long serialVersionUID = -973435679964589949L;

	public IllegalManagementInfosException() {
		super();
	}

	public IllegalManagementInfosException(String msg) {
		super(msg);
	}

	public IllegalManagementInfosException(Throwable cause) {
		super(cause);
	}

	public IllegalManagementInfosException(String msg, Throwable cause) {
		super(msg, cause);
	}

}