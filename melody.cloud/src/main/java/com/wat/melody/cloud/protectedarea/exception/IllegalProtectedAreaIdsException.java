package com.wat.melody.cloud.protectedarea.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalProtectedAreaIdsException extends MelodyException {

	private static final long serialVersionUID = -121421353468769631L;

	public IllegalProtectedAreaIdsException(String msg) {
		super(msg);
	}

	public IllegalProtectedAreaIdsException(Throwable cause) {
		super(cause);
	}

	public IllegalProtectedAreaIdsException(String msg, Throwable cause) {
		super(msg, cause);
	}

}