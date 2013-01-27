package com.wat.melody.core.nativeplugin.foreach.exception;

import com.wat.melody.api.exception.TaskException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class ForeachException extends TaskException {

	private static final long serialVersionUID = -9171652048964086465L;

	public ForeachException() {
		super();
	}

	public ForeachException(String msg) {
		super(msg);
	}

	public ForeachException(Throwable cause) {
		super(cause);
	}

	public ForeachException(String msg, Throwable cause) {
		super(msg, cause);
	}

}