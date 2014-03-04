package com.wat.melody.cloud.protectedarea.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalProtectedAreaDatasException extends MelodyException {

	private static final long serialVersionUID = -895742421546875536L;

	public IllegalProtectedAreaDatasException(String msg) {
		super(msg);
	}

	public IllegalProtectedAreaDatasException(Throwable cause) {
		super(cause);
	}

	public IllegalProtectedAreaDatasException(String msg, Throwable cause) {
		super(msg, cause);
	}

}