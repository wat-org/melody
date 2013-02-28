package com.wat.melody.cloud.network.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalNetworkDeviceNameException extends MelodyException {

	private static final long serialVersionUID = -986356895657997771L;

	public IllegalNetworkDeviceNameException() {
		super();
	}

	public IllegalNetworkDeviceNameException(String msg) {
		super(msg);
	}

	public IllegalNetworkDeviceNameException(Throwable cause) {
		super(cause);
	}

	public IllegalNetworkDeviceNameException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
