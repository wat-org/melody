package com.wat.melody.common.files.exception;

import java.nio.file.NotDirectoryException;
import java.nio.file.Path;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class WrapperNotDirectoryException extends NotDirectoryException {

	private static final long serialVersionUID = 6537838801497640742L;

	public WrapperNotDirectoryException(String file) {
		super("'" + file + "'" + ": Not a directory.");
	}

	public WrapperNotDirectoryException(Path file) {
		this(file.toString());
	}

	public WrapperNotDirectoryException(String file, Throwable cause) {
		this(file);
		initCause(cause);
	}

	public WrapperNotDirectoryException(Path file, Throwable cause) {
		this(file.toString(), cause);
	}

}