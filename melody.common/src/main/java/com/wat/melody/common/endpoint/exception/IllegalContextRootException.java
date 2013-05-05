package com.wat.melody.common.endpoint.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalContextRootException extends MelodyException {

	private static final long serialVersionUID = -6546534524259879041L;

	public IllegalContextRootException() {
		super();
	}

	public IllegalContextRootException(String msg) {
		super(msg);
	}

	public IllegalContextRootException(Throwable cause) {
		super(cause);
	}

	public IllegalContextRootException(String msg, Throwable cause) {
		super(msg, cause);
	}

}