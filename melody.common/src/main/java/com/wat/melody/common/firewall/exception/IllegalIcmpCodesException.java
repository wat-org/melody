package com.wat.melody.common.firewall.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalIcmpCodesException extends MelodyException {

	private static final long serialVersionUID = -1868756534522252549L;

	public IllegalIcmpCodesException() {
		super();
	}

	public IllegalIcmpCodesException(String msg) {
		super(msg);
	}

	public IllegalIcmpCodesException(Throwable cause) {
		super(cause);
	}

	public IllegalIcmpCodesException(String msg, Throwable cause) {
		super(msg, cause);
	}

}