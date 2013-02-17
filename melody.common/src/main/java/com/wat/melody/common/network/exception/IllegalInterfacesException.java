package com.wat.melody.common.network.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalInterfacesException extends MelodyException {

	private static final long serialVersionUID = -2698545435678776532L;

	public IllegalInterfacesException() {
		super();
	}

	public IllegalInterfacesException(String msg) {
		super(msg);
	}

	public IllegalInterfacesException(Throwable cause) {
		super(cause);
	}

	public IllegalInterfacesException(String msg, Throwable cause) {
		super(msg, cause);
	}

}