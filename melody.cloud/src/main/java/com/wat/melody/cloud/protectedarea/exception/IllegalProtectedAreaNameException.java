package com.wat.melody.cloud.protectedarea.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalProtectedAreaNameException extends MelodyException {

	private static final long serialVersionUID = -876576598678645631L;

	public IllegalProtectedAreaNameException(String msg) {
		super(msg);
	}

	public IllegalProtectedAreaNameException(Throwable cause) {
		super(cause);
	}

	public IllegalProtectedAreaNameException(String msg, Throwable cause) {
		super(msg, cause);
	}

}