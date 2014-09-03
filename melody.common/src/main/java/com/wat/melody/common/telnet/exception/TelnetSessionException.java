package com.wat.melody.common.telnet.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class TelnetSessionException extends MelodyException {

	private static final long serialVersionUID = -325435436569875325L;

	public TelnetSessionException(String msg) {
		super(msg);
	}

	public TelnetSessionException(Throwable cause) {
		super(cause);
	}

	public TelnetSessionException(String msg, Throwable cause) {
		super(msg, cause);
	}

}