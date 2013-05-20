package com.wat.melody.common.firewall.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalNetworkDeviceNameRefsException extends MelodyException {

	private static final long serialVersionUID = -2698545435678776532L;

	public IllegalNetworkDeviceNameRefsException() {
		super();
	}

	public IllegalNetworkDeviceNameRefsException(String msg) {
		super(msg);
	}

	public IllegalNetworkDeviceNameRefsException(Throwable cause) {
		super(cause);
	}

	public IllegalNetworkDeviceNameRefsException(String msg, Throwable cause) {
		super(msg, cause);
	}

}