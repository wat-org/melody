package com.wat.melody.common.keypair.exception;

import java.io.IOException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalPassphraseException extends IOException {

	private static final long serialVersionUID = -3879876565434532453L;

	public IllegalPassphraseException(String msg) {
		super(msg);
	}

	public IllegalPassphraseException(Throwable cause) {
		super(cause);
	}

	public IllegalPassphraseException(String msg, Throwable cause) {
		super(msg, cause);
	}

}