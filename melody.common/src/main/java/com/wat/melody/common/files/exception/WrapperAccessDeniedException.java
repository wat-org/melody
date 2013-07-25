package com.wat.melody.common.files.exception;

import java.nio.file.AccessDeniedException;

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

	public WrapperAccessDeniedException(String file, Throwable cause) {
		this(file);
		initCause(cause);
	}

}