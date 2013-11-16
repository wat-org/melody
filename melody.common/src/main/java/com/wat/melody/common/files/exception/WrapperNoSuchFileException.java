package com.wat.melody.common.files.exception;

import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class WrapperNoSuchFileException extends NoSuchFileException {

	private static final long serialVersionUID = -5066686041195576765L;

	public WrapperNoSuchFileException(String file) {
		super("'" + file + "'", null, "No such file.");
	}

	public WrapperNoSuchFileException(Path file) {
		this(file.toString());
	}

	public WrapperNoSuchFileException(String file, Throwable cause) {
		this(file);
		initCause(cause);
	}

	public WrapperNoSuchFileException(Path file, Throwable cause) {
		this(file.toString(), cause);
	}

}