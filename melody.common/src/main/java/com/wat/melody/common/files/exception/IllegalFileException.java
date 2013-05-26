package com.wat.melody.common.files.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalFileException extends MelodyException {

	private static final long serialVersionUID = -6846065168406584060L;

	public IllegalFileException(String msg) {
		super(msg);
	}

	public IllegalFileException(Throwable cause) {
		super(cause);
	}

	public IllegalFileException(String msg, Throwable cause) {
		super(msg, cause);
	}

}