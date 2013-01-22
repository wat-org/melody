package com.wat.melody.common.keypair.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class KeyPairRepositoryException extends MelodyException {

	private static final long serialVersionUID = -2133590963141244905L;

	public KeyPairRepositoryException() {
		super();
	}

	public KeyPairRepositoryException(String msg) {
		super(msg);
	}

	public KeyPairRepositoryException(Throwable cause) {
		super(cause);
	}

	public KeyPairRepositoryException(String msg, Throwable cause) {
		super(msg, cause);
	}

}