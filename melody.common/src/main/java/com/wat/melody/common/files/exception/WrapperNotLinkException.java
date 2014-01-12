package com.wat.melody.common.files.exception;

import java.nio.file.NotLinkException;
import java.nio.file.Path;

import com.wat.melody.common.ex.HiddenException;

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

	public WrapperNotLinkException(Path file) {
		this(file.toString());
	}

	public WrapperNotLinkException(String file, Throwable cause) {
		this(file);
		initCause(new HiddenException(cause));
	}

	public WrapperNotLinkException(Path file, Throwable cause) {
		this(file.toString(), cause);
	}

}