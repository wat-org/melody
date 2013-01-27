package com.wat.melody.core.nativeplugin.sequence.exception;

import com.wat.melody.api.exception.TaskException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class SequenceException extends TaskException {

	private static final long serialVersionUID = -987402390468572904L;

	public SequenceException() {
		super();
	}

	public SequenceException(String msg) {
		super(msg);
	}

	public SequenceException(Throwable cause) {
		super(cause);
	}

	public SequenceException(String msg, Throwable cause) {
		super(msg, cause);
	}

}