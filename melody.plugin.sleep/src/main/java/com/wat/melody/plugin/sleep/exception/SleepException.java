package com.wat.melody.plugin.sleep.exception;

import com.wat.melody.api.exception.TaskException;

/**
 * <p>
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class SleepException extends TaskException {

	private static final long serialVersionUID = -3249870968709874655L;

	public SleepException() {
		super();
	}

	public SleepException(String msg) {
		super(msg);
	}

	public SleepException(Throwable cause) {
		super(cause);
	}

	public SleepException(String msg, Throwable cause) {
		super(msg, cause);
	}

}