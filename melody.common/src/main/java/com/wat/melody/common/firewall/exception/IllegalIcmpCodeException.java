package com.wat.melody.common.firewall.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalIcmpCodeException extends MelodyException {

	private static final long serialVersionUID = -1879868754653432549L;

	public IllegalIcmpCodeException() {
		super();
	}

	public IllegalIcmpCodeException(String msg) {
		super(msg);
	}

	public IllegalIcmpCodeException(Throwable cause) {
		super(cause);
	}

	public IllegalIcmpCodeException(String msg, Throwable cause) {
		super(msg, cause);
	}

}