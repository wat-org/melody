package com.wat.melody.cloud.network.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalManagementMethodNetworkException extends MelodyException {

	private static final long serialVersionUID = -412345389964589949L;

	public IllegalManagementMethodNetworkException(String msg) {
		super(msg);
	}

	public IllegalManagementMethodNetworkException(Throwable cause) {
		super(cause);
	}

	public IllegalManagementMethodNetworkException(String msg, Throwable cause) {
		super(msg, cause);
	}

}