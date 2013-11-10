package com.wat.melody.common.cifs.transfer.exception;

import java.nio.file.NoSuchFileException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class WrapperNoSuchShareException extends NoSuchFileException {

	private static final long serialVersionUID = -7653435467595576765L;

	public WrapperNoSuchShareException(String file) {
		super("'" + file + "'", null, "No such share.");
	}

	public WrapperNoSuchShareException(String file, Throwable cause) {
		this(file);
		initCause(cause);
	}

}