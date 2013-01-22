package com.wat.melody.common.network.exception;

import com.wat.melody.common.ex.MelodyException;

public class IllegalIpAddressException extends MelodyException {

	private static final long serialVersionUID = -2134468800998532632L;

	public IllegalIpAddressException() {
		super();
	}

	public IllegalIpAddressException(String msg) {
		super(msg);
	}

	public IllegalIpAddressException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
