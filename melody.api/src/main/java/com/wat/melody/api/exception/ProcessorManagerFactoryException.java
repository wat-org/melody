package com.wat.melody.api.exception;

import com.wat.melody.common.ex.MelodyException;

public class ProcessorManagerFactoryException extends MelodyException {

	private static final long serialVersionUID = -5014191251258746878L;

	public ProcessorManagerFactoryException() {
		super();
	}

	public ProcessorManagerFactoryException(String msg) {
		super(msg);
	}

	public ProcessorManagerFactoryException(Throwable cause) {
		super(cause);
	}

	public ProcessorManagerFactoryException(String msg, Throwable cause) {
		super(msg, cause);
	}

}