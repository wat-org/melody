package com.wat.melody.common.files.exception;

import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Path;

import com.wat.melody.common.ex.HiddenException;

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

	public WrapperDirectoryNotEmptyException(Path file) {
		this(file.toString());
	}

	public WrapperDirectoryNotEmptyException(String file, Throwable cause) {
		this(file);
		initCause(new HiddenException(cause));
	}

	public WrapperDirectoryNotEmptyException(Path file, Throwable cause) {
		this(file.toString(), cause);
	}

}