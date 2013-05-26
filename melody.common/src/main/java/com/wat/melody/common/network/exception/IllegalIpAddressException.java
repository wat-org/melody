package com.wat.melody.common.network.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalIpAddressException extends MelodyException {

	private static final long serialVersionUID = -2134468800998532632L;

	public IllegalIpAddressException(String msg) {
		super(msg);
	}

	public IllegalIpAddressException(Throwable cause) {
		super(cause);
	}

	public IllegalIpAddressException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
