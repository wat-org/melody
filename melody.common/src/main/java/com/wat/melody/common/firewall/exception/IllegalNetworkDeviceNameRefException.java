package com.wat.melody.common.firewall.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalNetworkDeviceNameRefException extends MelodyException {

	private static final long serialVersionUID = -2378988735678776532L;

	public IllegalNetworkDeviceNameRefException(String msg) {
		super(msg);
	}

	public IllegalNetworkDeviceNameRefException(Throwable cause) {
		super(cause);
	}

	public IllegalNetworkDeviceNameRefException(String msg, Throwable cause) {
		super(msg, cause);
	}

}