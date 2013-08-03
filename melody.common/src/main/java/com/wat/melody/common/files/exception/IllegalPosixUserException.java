package com.wat.melody.common.files.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalPosixUserException extends MelodyException {

	private static final long serialVersionUID = -4365476587542352572L;

	public IllegalPosixUserException(String msg) {
		super(msg);
	}

	public IllegalPosixUserException(Throwable cause) {
		super(cause);
	}

	public IllegalPosixUserException(String msg, Throwable cause) {
		super(msg, cause);
	}

}