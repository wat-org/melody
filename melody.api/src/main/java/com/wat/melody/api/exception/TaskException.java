package com.wat.melody.api.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class TaskException extends MelodyException {

	private static final long serialVersionUID = -5014891251658746878L;

	public TaskException() {
		super();
	}

	public TaskException(String msg) {
		super(msg);
	}

	public TaskException(Throwable cause) {
		super(cause);
	}

	public TaskException(String msg, Throwable cause) {
		super(msg, cause);
	}

}