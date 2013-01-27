package com.wat.melody.common.network.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalIpRangeException extends MelodyException {

	private static final long serialVersionUID = -2378988735678776532L;

	public IllegalIpRangeException() {
		super();
	}

	public IllegalIpRangeException(String msg) {
		super(msg);
	}

	public IllegalIpRangeException(Throwable cause) {
		super(cause);
	}

	public IllegalIpRangeException(String msg, Throwable cause) {
		super(msg, cause);
	}

}