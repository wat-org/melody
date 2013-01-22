package com.wat.melody.common.files.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalDirectoryException extends MelodyException {

	private static final long serialVersionUID = -1974086942068482641L;

	public IllegalDirectoryException() {
		super();
	}

	public IllegalDirectoryException(String msg) {
		super(msg);
	}

	public IllegalDirectoryException(Throwable cause) {
		super(cause);
	}

	public IllegalDirectoryException(String msg, Throwable cause) {
		super(msg, cause);
	}

}