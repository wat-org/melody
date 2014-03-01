package com.wat.melody.common.xml.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalAttributeNameException extends MelodyException {

	private static final long serialVersionUID = -979879876565343689L;

	public IllegalAttributeNameException(String msg) {
		super(msg);
	}

	public IllegalAttributeNameException(Throwable cause) {
		super(cause);
	}

	public IllegalAttributeNameException(String msg, Throwable cause) {
		super(msg, cause);
	}

}