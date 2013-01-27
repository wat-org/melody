package com.wat.melody.cloud.network.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalNetworkDeviceException extends MelodyException {

	private static final long serialVersionUID = -986356895657997771L;

	public IllegalNetworkDeviceException() {
		super();
	}

	public IllegalNetworkDeviceException(String msg) {
		super(msg);
	}

	public IllegalNetworkDeviceException(Throwable cause) {
		super(cause);
	}

	public IllegalNetworkDeviceException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
