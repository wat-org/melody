package com.wat.melody.api.exception;

import com.wat.melody.common.utils.exception.MelodyException;

public class TaskFactoryException extends MelodyException {

	private static final long serialVersionUID = -8384841123110684061L;

	public TaskFactoryException() {
		super();
	}

	public TaskFactoryException(String msg) {
		super(msg);
	}

	public TaskFactoryException(Throwable cause) {
		super(cause);
	}

	public TaskFactoryException(String msg, Throwable cause) {
		super(msg, cause);
	}

}