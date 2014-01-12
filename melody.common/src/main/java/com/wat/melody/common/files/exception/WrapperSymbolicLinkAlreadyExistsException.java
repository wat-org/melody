package com.wat.melody.common.files.exception;

import java.nio.file.Path;

import com.wat.melody.common.ex.HiddenException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class WrapperSymbolicLinkAlreadyExistsException extends
		WrapperFileAlreadyExistsException {

	private static final long serialVersionUID = -1876755634535768757L;

	public WrapperSymbolicLinkAlreadyExistsException(String file) {
		super(file, "Symbolic link already exists.");
	}

	public WrapperSymbolicLinkAlreadyExistsException(Path file) {
		this(file.toString());
	}

	public WrapperSymbolicLinkAlreadyExistsException(String file,
			Throwable cause) {
		this(file);
		initCause(new HiddenException(cause));
	}

	public WrapperSymbolicLinkAlreadyExistsException(Path file, Throwable cause) {
		this(file.toString(), cause);
	}

}