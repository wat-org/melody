package com.wat.melody.common.keypair.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class KeyPairRepositoryPathException extends MelodyException {

	private static final long serialVersionUID = -2133590963141244905L;

	public KeyPairRepositoryPathException() {
		super();
	}

	public KeyPairRepositoryPathException(String msg) {
		super(msg);
	}

	public KeyPairRepositoryPathException(Throwable cause) {
		super(cause);
	}

	public KeyPairRepositoryPathException(String msg, Throwable cause) {
		super(msg, cause);
	}

}