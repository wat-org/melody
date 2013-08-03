package com.wat.melody.common.files.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalPosixPermissionsException extends MelodyException {

	private static final long serialVersionUID = -6543122347878787772L;

	public IllegalPosixPermissionsException(String msg) {
		super(msg);
	}

	public IllegalPosixPermissionsException(Throwable cause) {
		super(cause);
	}

	public IllegalPosixPermissionsException(String msg, Throwable cause) {
		super(msg, cause);
	}

}