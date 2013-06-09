package com.wat.melody.cloud.network.activation.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalNetworkActivationProtocolException extends MelodyException {

	private static final long serialVersionUID = -412345389964589949L;

	public IllegalNetworkActivationProtocolException(String msg) {
		super(msg);
	}

	public IllegalNetworkActivationProtocolException(Throwable cause) {
		super(cause);
	}

	public IllegalNetworkActivationProtocolException(String msg, Throwable cause) {
		super(msg, cause);
	}

}