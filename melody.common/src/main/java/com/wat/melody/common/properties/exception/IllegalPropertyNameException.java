package com.wat.melody.common.properties.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalPropertyNameException extends MelodyException {

	private static final long serialVersionUID = -2309872605328438369L;

	public IllegalPropertyNameException(String msg) {
		super(msg);
	}

	public IllegalPropertyNameException(Throwable cause) {
		super(cause);
	}

	public IllegalPropertyNameException(String msg, Throwable cause) {
		super(msg, cause);
	}

}