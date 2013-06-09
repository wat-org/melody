package com.wat.melody.cloud.network.activation.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class NetworkActivationException extends MelodyException {

	private static final long serialVersionUID = -498763289964378909L;

	public NetworkActivationException(String msg) {
		super(msg);
	}

	public NetworkActivationException(Throwable cause) {
		super(cause);
	}

	public NetworkActivationException(String msg, Throwable cause) {
		super(msg, cause);
	}

}