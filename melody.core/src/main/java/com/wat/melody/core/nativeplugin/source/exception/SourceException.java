package com.wat.melody.core.nativeplugin.source.exception;

import com.wat.melody.api.exception.TaskException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class SourceException extends TaskException {

	private static final long serialVersionUID = -5023456789876543876L;

	public SourceException(String msg) {
		super(msg);
	}

	public SourceException(Throwable cause) {
		super(cause);
	}

	public SourceException(String msg, Throwable cause) {
		super(msg, cause);
	}

}