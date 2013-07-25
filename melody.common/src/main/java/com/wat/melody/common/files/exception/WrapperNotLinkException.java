package com.wat.melody.common.files.exception;

import java.nio.file.NotLinkException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class WrapperNotLinkException extends NotLinkException {

	private static final long serialVersionUID = -2495047459099680769L;

	public WrapperNotLinkException(String file) {
		super("'" + file + "'", null, "Not a link.");
	}

	public WrapperNotLinkException(String file, Throwable cause) {
		this(file);
		initCause(cause);
	}

}