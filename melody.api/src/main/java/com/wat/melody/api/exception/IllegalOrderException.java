package com.wat.melody.api.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalOrderException extends MelodyException {

	private static final long serialVersionUID = -1658406468403640602L;

	public IllegalOrderException(String msg) {
		super(msg);
	}

	public IllegalOrderException(Throwable cause) {
		super(cause);
	}

	public IllegalOrderException(String msg, Throwable cause) {
		super(msg, cause);
	}

}