package com.wat.melody.common.network.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalDirectionsException extends MelodyException {

	private static final long serialVersionUID = -2133143253890955249L;

	public IllegalDirectionsException() {
		super();
	}

	public IllegalDirectionsException(String msg) {
		super(msg);
	}

	public IllegalDirectionsException(Throwable cause) {
		super(cause);
	}

	public IllegalDirectionsException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
