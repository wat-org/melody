package com.wat.melody.common.network.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalPortException extends MelodyException {

	private static final long serialVersionUID = -6435793678766540892L;

	public IllegalPortException(String msg) {
		super(msg);
	}

	public IllegalPortException(Throwable cause) {
		super(cause);
	}

	public IllegalPortException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
