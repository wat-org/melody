package com.wat.melody.common.order.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalOrderNameSetException extends MelodyException {

	private static final long serialVersionUID = -1658406468403640602L;

	public IllegalOrderNameSetException(String msg) {
		super(msg);
	}

	public IllegalOrderNameSetException(Throwable cause) {
		super(cause);
	}

	public IllegalOrderNameSetException(String msg, Throwable cause) {
		super(msg, cause);
	}

}