package com.wat.melody.cli.exception;

import com.wat.melody.common.utils.exception.MelodyException;

public class Log4JConfigurationException extends MelodyException {

	private static final long serialVersionUID = 1593821304849804204L;

	public Log4JConfigurationException() {
		super();
	}

	public Log4JConfigurationException(String msg) {
		super(msg);
	}

	public Log4JConfigurationException(String msg, Throwable cause) {
		super(msg, cause);
	}

}