package com.wat.melody.cloud.network.exception;

import com.wat.melody.common.ex.MelodyException;

public class IllegalManagementMethodNetworkException extends MelodyException {

	private static final long serialVersionUID = -412345389964589949L;

	public IllegalManagementMethodNetworkException() {
		super();
	}

	public IllegalManagementMethodNetworkException(String msg) {
		super(msg);
	}

	public IllegalManagementMethodNetworkException(Throwable cause) {
		super(cause);
	}

	public IllegalManagementMethodNetworkException(String msg, Throwable cause) {
		super(msg, cause);
	}

}