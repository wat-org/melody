package com.wat.melody.common.files.exception;

import java.nio.file.FileAlreadyExistsException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class WrapperFileAlreadyExistsException extends
		FileAlreadyExistsException {

	private static final long serialVersionUID = -3812907544509137671L;

	public WrapperFileAlreadyExistsException(String file) {
		super("'" + file + "'", null, "File already exists.");
	}

	public WrapperFileAlreadyExistsException(String file, Throwable cause) {
		this(file);
		initCause(cause);
	}

}