package com.wat.melody.common.firewall.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalIcmpTypesException extends MelodyException {

	private static final long serialVersionUID = -1986875463253265549L;

	public IllegalIcmpTypesException(String msg) {
		super(msg);
	}

	public IllegalIcmpTypesException(Throwable cause) {
		super(cause);
	}

	public IllegalIcmpTypesException(String msg, Throwable cause) {
		super(msg, cause);
	}

}