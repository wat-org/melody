package com.wat.melody.cli.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class CommandLineParsingException extends MelodyException {

	private static final long serialVersionUID = 1473752778024477252L;

	public CommandLineParsingException() {
		super();
	}

	public CommandLineParsingException(String msg) {
		super(msg);
	}

	public CommandLineParsingException(Throwable cause) {
		super(cause);
	}

	public CommandLineParsingException(String msg, Throwable cause) {
		super(msg, cause);
	}

}