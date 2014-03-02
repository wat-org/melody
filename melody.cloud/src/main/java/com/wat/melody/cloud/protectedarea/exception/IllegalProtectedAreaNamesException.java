package com.wat.melody.cloud.protectedarea.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalProtectedAreaNamesException extends MelodyException {

	private static final long serialVersionUID = -243243534576095631L;

	public IllegalProtectedAreaNamesException(String msg) {
		super(msg);
	}

	public IllegalProtectedAreaNamesException(Throwable cause) {
		super(cause);
	}

	public IllegalProtectedAreaNamesException(String msg, Throwable cause) {
		super(msg, cause);
	}

}