package com.wat.melody.common.filter.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalFilterException extends MelodyException {

	private static final long serialVersionUID = -1879447098410368880L;

	public IllegalFilterException(String msg) {
		super(msg);
	}

	public IllegalFilterException(Throwable cause) {
		super(cause);
	}

	public IllegalFilterException(String msg, Throwable cause) {
		super(msg, cause);
	}

}