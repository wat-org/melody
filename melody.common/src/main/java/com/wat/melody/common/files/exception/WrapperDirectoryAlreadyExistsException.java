package com.wat.melody.common.files.exception;

import java.nio.file.Path;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class WrapperDirectoryAlreadyExistsException extends
		WrapperFileAlreadyExistsException {

	private static final long serialVersionUID = -7654524564768698757L;

	public WrapperDirectoryAlreadyExistsException(String file) {
		super(file, "Directory already exists.");
	}

	public WrapperDirectoryAlreadyExistsException(Path file) {
		this(file.toString());
	}

	public WrapperDirectoryAlreadyExistsException(String file, Throwable cause) {
		this(file);
		initCause(cause);
	}

	public WrapperDirectoryAlreadyExistsException(Path file, Throwable cause) {
		this(file.toString(), cause);
	}

}