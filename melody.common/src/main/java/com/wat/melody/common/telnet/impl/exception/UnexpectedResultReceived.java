package com.wat.melody.common.telnet.impl.exception;

import java.io.IOException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class UnexpectedResultReceived extends IOException {

	private static final long serialVersionUID = -657646543654875325L;

	public UnexpectedResultReceived(String msg) {
		super(msg);
	}

	public UnexpectedResultReceived(Throwable cause) {
		super(cause);
	}

	public UnexpectedResultReceived(String msg, Throwable cause) {
		super(msg, cause);
	}

}