package com.wat.melody.common.keypair.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalKeyPairSizeException extends MelodyException {

	private static final long serialVersionUID = -4832211441476933457L;

	public IllegalKeyPairSizeException(String msg) {
		super(msg);
	}

	public IllegalKeyPairSizeException(Throwable cause) {
		super(cause);
	}

	public IllegalKeyPairSizeException(String msg, Throwable cause) {
		super(msg, cause);
	}

}