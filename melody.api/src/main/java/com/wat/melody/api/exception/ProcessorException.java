package com.wat.melody.api.exception;

import com.wat.melody.common.utils.exception.MelodyException;

public class ProcessorException extends MelodyException {

	private static final long serialVersionUID = -1489516211069804151L;

	public ProcessorException() {
		super();
	}

	public ProcessorException(String msg) {
		super(msg);
	}

	public ProcessorException(String msg, Throwable cause) {
		super(msg, cause);
	}

}