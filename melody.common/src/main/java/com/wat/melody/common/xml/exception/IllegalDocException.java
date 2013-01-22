package com.wat.melody.common.xml.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalDocException extends MelodyException {

	private static final long serialVersionUID = -6549841621874690892L;

	public IllegalDocException() {
		super();
	}

	public IllegalDocException(String msg) {
		super(msg);
	}

	public IllegalDocException(Throwable cause) {
		super(cause);
	}

	public IllegalDocException(String msg, Throwable cause) {
		super(msg, cause);
	}

}