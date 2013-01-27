package com.wat.melody.cloud.network.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalNetworkDeviceListException extends MelodyException {

	private static final long serialVersionUID = -98652214580843467L;

	public IllegalNetworkDeviceListException() {
		super();
	}

	public IllegalNetworkDeviceListException(String msg) {
		super(msg);
	}

	public IllegalNetworkDeviceListException(Throwable cause) {
		super(cause);
	}

	public IllegalNetworkDeviceListException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
