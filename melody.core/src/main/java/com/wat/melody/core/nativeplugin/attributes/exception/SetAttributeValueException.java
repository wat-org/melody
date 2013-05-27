package com.wat.melody.core.nativeplugin.attributes.exception;

import com.wat.melody.api.exception.TaskException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class SetAttributeValueException extends TaskException {

	private static final long serialVersionUID = -189644684016806644L;

	public SetAttributeValueException(String msg) {
		super(msg);
	}

	public SetAttributeValueException(Throwable cause) {
		super(cause);
	}

	public SetAttributeValueException(String msg, Throwable cause) {
		super(msg, cause);
	}

}