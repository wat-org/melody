package com.wat.melody.cloud.network.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalNetworkDeviceNameListException extends MelodyException {

	private static final long serialVersionUID = -98652214580843467L;

	public IllegalNetworkDeviceNameListException(String msg) {
		super(msg);
	}

	public IllegalNetworkDeviceNameListException(Throwable cause) {
		super(cause);
	}

	public IllegalNetworkDeviceNameListException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
