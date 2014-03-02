package com.wat.melody.cloud.protectedarea.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class ProtectedAreaException extends MelodyException {

	private static final long serialVersionUID = -876876543234576631L;

	public ProtectedAreaException(String msg) {
		super(msg);
	}

	public ProtectedAreaException(Throwable cause) {
		super(cause);
	}

	public ProtectedAreaException(String msg, Throwable cause) {
		super(msg, cause);
	}

}