package com.wat.melody.common.typedef.exception;

import com.wat.melody.common.utils.exception.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalGroupIDException extends MelodyException {

	private static final long serialVersionUID = -8676564432134334572L;

	public IllegalGroupIDException() {
		super();
	}

	public IllegalGroupIDException(String msg) {
		super(msg);
	}

	public IllegalGroupIDException(String msg, Throwable cause) {
		super(msg, cause);
	}

}