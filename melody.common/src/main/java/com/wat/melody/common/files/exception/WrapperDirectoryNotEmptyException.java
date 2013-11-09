package com.wat.melody.common.files.exception;

import java.nio.file.DirectoryNotEmptyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class WrapperDirectoryNotEmptyException extends
		DirectoryNotEmptyException {

	private static final long serialVersionUID = 1004490961415527888L;

	public WrapperDirectoryNotEmptyException(String file) {
		super("'" + file + "'" + ": Directory is not empty.");
	}

	public WrapperDirectoryNotEmptyException(String file, Throwable cause) {
		this(file);
		initCause(cause);
	}

}