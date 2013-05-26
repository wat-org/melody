package com.wat.melody.cloud.network.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class NetworkManagementException extends MelodyException {

	private static final long serialVersionUID = -498763289964378909L;

	public NetworkManagementException(String msg) {
		super(msg);
	}

	public NetworkManagementException(Throwable cause) {
		super(cause);
	}

	public NetworkManagementException(String msg, Throwable cause) {
		super(msg, cause);
	}

}