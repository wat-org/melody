package com.wat.melody.common.firewall.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalProtocolsException extends MelodyException {

	private static final long serialVersionUID = -2121387111974466632L;

	public IllegalProtocolsException() {
		super();
	}

	public IllegalProtocolsException(String msg) {
		super(msg);
	}

	public IllegalProtocolsException(Throwable cause) {
		super(cause);
	}

	public IllegalProtocolsException(String msg, Throwable cause) {
		super(msg, cause);
	}

}