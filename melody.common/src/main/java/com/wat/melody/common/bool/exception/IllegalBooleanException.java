package com.wat.melody.common.bool.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalBooleanException extends MelodyException {

	private static final long serialVersionUID = -1979876535323789041L;

	public IllegalBooleanException(String msg) {
		super(msg);
	}

	public IllegalBooleanException(Throwable cause) {
		super(cause);
	}

	public IllegalBooleanException(String msg, Throwable cause) {
		super(msg, cause);
	}

}