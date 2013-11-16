package com.wat.melody.common.files.exception;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class WrapperFileAlreadyExistsException extends
		FileAlreadyExistsException {

	private static final long serialVersionUID = -3812907544509137671L;

	protected WrapperFileAlreadyExistsException(String file, String reason) {
		super("'" + file + "'", null, reason);
	}

	public WrapperFileAlreadyExistsException(String file) {
		this(file, "File already exists.");
	}

	public WrapperFileAlreadyExistsException(Path file) {
		this(file.toString());
	}

	protected WrapperFileAlreadyExistsException(Path file, String reason) {
		this(file.toString(), reason);
	}

	public WrapperFileAlreadyExistsException(String file, Throwable cause) {
		this(file);
		initCause(cause);
	}

	public WrapperFileAlreadyExistsException(Path file, Throwable cause) {
		this(file.toString(), cause);
	}

}