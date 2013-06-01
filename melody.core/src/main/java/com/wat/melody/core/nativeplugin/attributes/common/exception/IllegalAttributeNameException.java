package com.wat.melody.core.nativeplugin.attributes.common.exception;

import com.wat.melody.api.exception.TaskException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalAttributeNameException extends TaskException {

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