package com.wat.melody.cloud.network.activation.exception;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalNetworkActivationDatasException extends
		NetworkActivationException {

	private static final long serialVersionUID = -8757644333245365457L;

	public IllegalNetworkActivationDatasException(String msg) {
		super(msg);
	}

	public IllegalNetworkActivationDatasException(Throwable cause) {
		super(cause);
	}

	public IllegalNetworkActivationDatasException(String msg, Throwable cause) {
		super(msg, cause);
	}

}