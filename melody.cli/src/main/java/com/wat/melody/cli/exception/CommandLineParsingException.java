package com.wat.melody.cli.exception;

import com.wat.melody.common.utils.exception.MelodyException;

public class CommandLineParsingException extends MelodyException {

	private static final long serialVersionUID = 1473752778024477252L;

	public CommandLineParsingException() {
		super();
	}

	public CommandLineParsingException(String msg) {
		super(msg);
	}

	public CommandLineParsingException(String msg, Throwable cause) {
		super(msg, cause);
	}

}