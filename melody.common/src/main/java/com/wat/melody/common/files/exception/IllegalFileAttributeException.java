package com.wat.melody.common.files.exception;

import java.nio.file.FileSystemException;

import com.wat.melody.common.ex.ConsolidatedException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalFileAttributeException extends FileSystemException {

	private static final long serialVersionUID = 10355023166519261L;

	public IllegalFileAttributeException(String file, String reason) {
		super("'" + file + "'", null, reason);
	}

	public IllegalFileAttributeException(String file, String reason,
			Throwable cause) {
		this(file, reason);
		initCause(cause);
	}

	public IllegalFileAttributeException(ConsolidatedException cause) {
		super("");
		initCause(cause);
	}

}