package com.wat.melody.common.timeout.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalTimeoutException extends MelodyException {

	private static final long serialVersionUID = -5876554487532240905L;

	public IllegalTimeoutException(String msg) {
		super(msg);
	}

	public IllegalTimeoutException(Throwable cause) {
		super(cause);
	}

	public IllegalTimeoutException(String msg, Throwable cause) {
		super(msg, cause);
	}

}