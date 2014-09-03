package com.wat.melody.plugin.telnet.common.exception;

import com.wat.melody.api.exception.TaskException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class TelnetException extends TaskException {

	private static final long serialVersionUID = -6525342456468570465L;

	public TelnetException(String msg) {
		super(msg);
	}

	public TelnetException(Throwable cause) {
		super(cause);
	}

	public TelnetException(String msg, Throwable cause) {
		super(msg, cause);
	}

}