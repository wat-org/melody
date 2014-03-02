package com.wat.melody.cloud.protectedarea.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalProtectedAreaIdException extends MelodyException {

	private static final long serialVersionUID = -241435343654765631L;

	public IllegalProtectedAreaIdException(String msg) {
		super(msg);
	}

	public IllegalProtectedAreaIdException(Throwable cause) {
		super(cause);
	}

	public IllegalProtectedAreaIdException(String msg, Throwable cause) {
		super(msg, cause);
	}

}