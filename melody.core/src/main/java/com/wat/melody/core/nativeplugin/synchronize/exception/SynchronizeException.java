package com.wat.melody.core.nativeplugin.synchronize.exception;

import com.wat.melody.api.exception.TaskException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class SynchronizeException extends TaskException {

	private static final long serialVersionUID = -289644684016806644L;

	public SynchronizeException() {
		super();
	}

	public SynchronizeException(String msg) {
		super(msg);
	}

	public SynchronizeException(Throwable cause) {
		super(cause);
	}

	public SynchronizeException(String msg, Throwable cause) {
		super(msg, cause);
	}

}