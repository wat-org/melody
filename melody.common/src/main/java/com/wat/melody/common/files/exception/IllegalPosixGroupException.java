package com.wat.melody.common.files.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalPosixGroupException extends MelodyException {

	private static final long serialVersionUID = -8676564432134334572L;

	public IllegalPosixGroupException(String msg) {
		super(msg);
	}

	public IllegalPosixGroupException(Throwable cause) {
		super(cause);
	}

	public IllegalPosixGroupException(String msg, Throwable cause) {
		super(msg, cause);
	}

}