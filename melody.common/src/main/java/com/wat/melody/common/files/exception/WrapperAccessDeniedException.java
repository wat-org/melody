package com.wat.melody.common.files.exception;

import java.nio.file.AccessDeniedException;
import java.nio.file.Path;

import com.wat.melody.common.ex.HiddenException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class WrapperAccessDeniedException extends AccessDeniedException {

	private static final long serialVersionUID = 10355023166519261L;

	public WrapperAccessDeniedException(String file) {
		super("'" + file + "'", null, "Access denied.");
	}

	public WrapperAccessDeniedException(Path file) {
		this(file.toString());
	}

	public WrapperAccessDeniedException(String file, Throwable cause) {
		this(file);
		initCause(new HiddenException(cause));
	}

	public WrapperAccessDeniedException(Path file, Throwable cause) {
		this(file.toString(), cause);
	}

}