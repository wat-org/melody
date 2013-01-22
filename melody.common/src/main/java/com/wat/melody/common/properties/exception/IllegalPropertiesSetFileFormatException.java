package com.wat.melody.common.properties.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalPropertiesSetFileFormatException extends MelodyException {

	private static final long serialVersionUID = 7894651384798200660L;

	public IllegalPropertiesSetFileFormatException() {
		super();
	}

	public IllegalPropertiesSetFileFormatException(String msg) {
		super(msg);
	}

	public IllegalPropertiesSetFileFormatException(Throwable cause) {
		super(cause);
	}

	public IllegalPropertiesSetFileFormatException(String msg, Throwable cause) {
		super(msg, cause);
	}

}