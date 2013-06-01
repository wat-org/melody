package com.wat.melody.common.properties.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalPropertiesSetException extends MelodyException {

	private static final long serialVersionUID = 7894651384798200660L;

	public IllegalPropertiesSetException(String msg) {
		super(msg);
	}

	public IllegalPropertiesSetException(Throwable cause) {
		super(cause);
	}

	public IllegalPropertiesSetException(String msg, Throwable cause) {
		super(msg, cause);
	}

}