package com.wat.melody.core.nativeplugin.attributes.exception;

import com.wat.melody.api.exception.TaskException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class RemoveAttributeException extends TaskException {

	private static final long serialVersionUID = -497987968765543312L;

	public RemoveAttributeException(String msg) {
		super(msg);
	}

	public RemoveAttributeException(Throwable cause) {
		super(cause);
	}

	public RemoveAttributeException(String msg, Throwable cause) {
		super(msg, cause);
	}

}