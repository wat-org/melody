package com.wat.melody.common.network.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalPortRangeException extends MelodyException {

	private static final long serialVersionUID = -6321459665094698792L;

	public IllegalPortRangeException() {
		super();
	}

	public IllegalPortRangeException(String msg) {
		super(msg);
	}

	public IllegalPortRangeException(Throwable cause) {
		super(cause);
	}

	public IllegalPortRangeException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
