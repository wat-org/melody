package com.wat.melody.common.properties.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalPropertyException extends MelodyException {

	private static final long serialVersionUID = -5490876025637520869L;

	public IllegalPropertyException() {
		super();
	}

	public IllegalPropertyException(String msg) {
		super(msg);
	}

	public IllegalPropertyException(Throwable cause) {
		super(cause);
	}

	public IllegalPropertyException(String msg, Throwable cause) {
		super(msg, cause);
	}

}